"""Gradle capability.

Runs one of a fixed set of Gradle commands through the project's own Gradle
wrapper.  The command is passed to Gradle exactly as requested -- no rewriting,
no quoting, no shell -- so the agent can use plain Gradle syntax:
``build``, ``fabric:build``, ``neoforge:build``, ``fabric:runTestServer``.
This module only decides whether the command is allowed.
"""

import os
import time

from capabilities.base import (
    Capability,
    JobAlreadyRunning,
    JobRun,
    StreamPump,
    decode,
    kill_tree,
    set_binary_mode,
    spawn,
)
from errors import BadRequest, Conflict, Forbidden, NotFound

# Whitelist.  A command is either one of ALLOWED_TASKS, or
# "<subproject>:<task>" where both parts are whitelisted, which is exactly how
# Gradle spells a subproject task.  Add entries here to extend the whitelist.
ALLOWED_SUBPROJECTS = frozenset({"fabric", "neoforge"})
ALLOWED_TASKS = frozenset({"build", "test", "runClient", "runTestServer"})

# "all" is a convenience alias, not a Gradle task: build every known subproject.
ALL_TASKS = tuple(f"{subproject}:build" for subproject in sorted(ALLOWED_SUBPROJECTS))

# Hard ceiling for one Gradle command, so a stuck build cannot hold the job forever.
MAX_BUILD_SECONDS = 1800
# How long a new request waits for a just-finished job to release its slot.
SLOT_GRACE_SECONDS = 5

WRAPPER_NAMES = ("gradlew.bat", "gradlew")

# A subproject name is only ever used as a Gradle task path segment.
_FORBIDDEN_IN_SUBPROJECT = set("\\/:. \t\n\r")


def _allowed_list():
    subprojects = ", ".join(sorted(ALLOWED_SUBPROJECTS))
    tasks = ", ".join(sorted(ALLOWED_TASKS))
    return f"{tasks}; or <{subprojects}>:<task>"


class GradleCapability(Capability):
    name = "gradle"
    routes = (("POST", "/gradle"),)
    # Extra endpoints served by a named method instead of a streaming handler.
    extra_routes = (("POST", "/gradle/cancel", "cancel"),)
    streaming = True

    def handle(self, request, context):
        plan = self._plan(request.body, context)
        key = plan["host_directory"]
        try:
            # A slot still held by a job that has just finished is waited for; a
            # slot whose process is alive is a real conflict.
            context.reserve_with_grace(key, SLOT_GRACE_SECONDS)
        except JobAlreadyRunning:
            raise Conflict(
                f"a Gradle job is already running for {plan['directory']}; "
                "wait for it to finish or POST /gradle/cancel"
            ) from None
        events = self._stream(context, plan, request.logging)
        return JobRun(key, events, context, request.logging, plan["task"])

    def _plan(self, body, context):
        """Validate the request and map its directory; never spawns anything."""
        unknown = sorted(set(body) - {"directory", "task", "args"})
        if unknown:
            raise BadRequest(f"unknown request field(s): {', '.join(unknown)}")

        directory = body.get("directory")
        if not isinstance(directory, str) or not directory.strip():
            raise BadRequest('"directory" is required and must be a container path')

        task = self._requested_task(body)
        tasks = self._allowed_tasks(task)

        host_directory = context.path_mapper.resolve(directory)
        if not os.path.isdir(host_directory):
            raise NotFound(f"{directory} is not a directory")

        wrapper = self._wrapper_path(host_directory)
        if wrapper is None:
            raise NotFound(f"no Gradle wrapper (gradlew / gradlew.bat) found in {directory}")

        return {
            "directory": directory,
            "host_directory": host_directory,
            "wrapper": wrapper,
            "task": task,
            "tasks": tasks,
        }

    def _requested_task(self, body):
        task = body.get("task")
        args = body.get("args")
        if task is None and args is None:
            raise BadRequest('"task" or "args" is required')
        if task is not None and args is not None:
            raise BadRequest('"task" and "args" are mutually exclusive')
        if args is not None:
            if not isinstance(args, list) or not args:
                raise BadRequest('"args" must be a non-empty array')
            if len(args) != 1 or not isinstance(args[0], str):
                raise BadRequest('"args" must contain exactly one Gradle command')
            task = args[0]
        if not isinstance(task, str):
            raise BadRequest('"task" must be a string')
        return task.strip()

    def _allowed_tasks(self, task):
        """Return the Gradle tasks to run, or reject the command."""
        if not task:
            raise BadRequest("the Gradle command must not be empty")
        if task == "all":
            return list(ALL_TASKS)
        if task in ALLOWED_TASKS:
            return [task]

        subproject, separator, subtask = task.rpartition(":")
        if separator:
            self._validate_subproject(subproject)
            if subtask not in ALLOWED_TASKS:
                raise Forbidden(
                    f'task "{task}" is not allowed: "{subtask}" is not an allowed task '
                    f"({_allowed_list()})"
                )
            return [task]

        raise Forbidden(f'task "{task}" is not allowed ({_allowed_list()}, all)')

    def _validate_subproject(self, subproject):
        if not subproject:
            raise BadRequest('the Gradle command must not start with ":"')
        if any(char in _FORBIDDEN_IN_SUBPROJECT for char in subproject) or ".." in subproject:
            raise Forbidden(f'"{subproject}" is not a valid subproject name')
        if subproject not in ALLOWED_SUBPROJECTS:
            raise Forbidden(
                f'subproject "{subproject}" is not allowed '
                f"({', '.join(sorted(ALLOWED_SUBPROJECTS))})"
            )
        return subproject

    def _wrapper_path(self, host_directory):
        for name in WRAPPER_NAMES:
            candidate = os.path.join(host_directory, name)
            if os.path.isfile(candidate):
                return candidate
        return None

    def _build_env(self):
        env = os.environ.copy()
        if not env.get("GRADLE_OPTS") and env.get("MOD_GRADLE_OPTS"):
            env["GRADLE_OPTS"] = env["MOD_GRADLE_OPTS"]
        env.pop("JAVA_TOOL_OPTIONS", None)
        # Ask Gradle for plain output; the container side is not a real terminal.
        env["NO_COLOR"] = "1"
        env["TERM"] = "dumb"
        env.setdefault("LC_ALL", "C.UTF-8")
        return env

    def _argv(self, wrapper, tasks):
        if os.name == "nt":
            return [wrapper] + list(tasks)
        return ["./gradlew"] + list(tasks)

    def _stream(self, context, plan, logging):
        host_directory = plan["host_directory"]
        yield {
            "type": "start",
            "task": plan["task"],
            "directory": plan["directory"],
            "hostDirectory": host_directory,
            "tasks": plan["tasks"],
        }

        logging.info("gradle request: %s task=%s", host_directory, plan["task"])
        env = self._build_env()

        for task in plan["tasks"]:
            if context.cancel_requested(host_directory):
                # The cancel arrived after this job reserved its slot but before
                # it spawned anything; nothing to run, nothing to kill.
                logging.info("CANCELLED-BEFORE-SPAWN %s", host_directory)
                yield {"type": "end", "exitCode": 130, "reason": "cancelled"}
                return

            argv = self._argv(plan["wrapper"], [task])
            yield {"type": "task_start", "task": task, "argv": argv}
            logging.info("process started: %s (cwd=%s)", argv, host_directory)
            try:
                process = spawn(argv, host_directory, env)
                # Read both stdout and stderr without a shell, in binary.
                set_binary_mode(process.stdout)
                set_binary_mode(process.stderr)
            except OSError as exc:
                yield {"type": "error", "message": f"failed to start the Gradle wrapper: {exc}"}
                return
            context.set_process(host_directory, process)
            # The cancel can also land between the reservation and this line,
            # where the record still held no process handle to terminate.
            if context.cancel_requested(host_directory):
                logging.info("CANCELLED-AFTER-SPAWN %s pid=%s", host_directory, process.pid)
                kill_tree(process)
            result = {"reason": None}
            try:
                for chunk in self._pump(process, result, lambda: context.cancel_requested(host_directory)):
                    yield chunk
            finally:
                context.set_process(host_directory, None)

            exit_code = process.wait()
            if result["reason"] is None and context.cancel_requested(host_directory):
                result["reason"] = "cancelled"
            logging.info(
                "process exited with code %s (task=%s, reason=%s)",
                exit_code,
                task,
                result["reason"] or "ok",
            )
            yield {"type": "task_end", "task": task, "exitCode": exit_code}

            if result["reason"] == "cancelled":
                yield {"type": "end", "exitCode": 130, "reason": "cancelled"}
                return
            if result["reason"] == "timeout":
                yield {"type": "end", "exitCode": -1, "reason": "timeout"}
                return
            if exit_code != 0:
                yield {"type": "end", "exitCode": exit_code, "reason": "task failed"}
                return

        yield {"type": "end", "exitCode": 0, "reason": "ok"}

    def _pump(self, process, result, is_cancelled=lambda: False):
        deadline = time.monotonic() + MAX_BUILD_SECONDS
        pump = StreamPump(process)
        result["pump"] = pump
        for name, chunk in pump:
            if name == "__idle__":
                if process.poll() is None:
                    if time.monotonic() > deadline:
                        result["reason"] = "timeout"
                        kill_tree(process)
                    elif is_cancelled():
                        # A cancelled job whose wrapper ignores the signal.
                        result["reason"] = "cancelled"
                        kill_tree(process)
                continue
            yield {"type": name, "data": decode(chunk)}
        pump.finish()
        if result["reason"] == "timeout":
            yield {"type": "stderr", "data": "\n[host-service] build timed out and was killed\n"}

    def cancel(self, request, context):
        body = request.body
        unknown = sorted(set(body) - {"directory"})
        if unknown:
            raise BadRequest(f"unknown request field(s): {', '.join(unknown)}")
        directory = body.get("directory")
        if not isinstance(directory, str) or not directory.strip():
            raise BadRequest('"directory" is required and must be a container path')
        host_directory = context.path_mapper.resolve(directory, must_exist=False)
        return {"cancelled": context.cancel(host_directory), "directory": directory}
