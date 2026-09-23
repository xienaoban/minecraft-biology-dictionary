#!/usr/bin/env python3
"""gradlew-host - run Gradle through the Host Service from inside a container.

Usage (from a project directory that contains this script):

    python gradlew-host.py build
    python gradlew-host.py test
    python gradlew-host.py fabric:build
    python gradlew-host.py neoforge:build
    python gradlew-host.py fabric:runTestServer

The command is sent to Gradle exactly as written, so any whitelisted Gradle
command works unchanged.  This script rejects anything the service whitelist
does not contain; the service validates it again on its own side.
"""

import json
import logging
import os
import sys
import urllib.error
import urllib.request

PROTOCOL_VERSION = 1
DEFAULT_URL = "http://host.docker.internal:11109"
DEFAULT_TIMEOUT = 10

# Kept in sync with the service whitelist to fail fast with a local message.
ALLOWED_TASKS = ("build", "test", "runClient", "runTestServer")
ALLOWED_SUBPROJECTS = ("fabric", "neoforge")

log = logging.getLogger("gradlew-host")


class HostServiceError(Exception):
    """A failure that should be reported to the user without a traceback."""


class _StderrHandler(logging.Handler):
    def emit(self, record):
        print(self.format(record), file=sys.stderr, flush=True)


def configure_logging(verbose):
    handler = _StderrHandler()
    handler.setFormatter(logging.Formatter("[gradlew-host] %(message)s"))
    log.handlers[:] = [handler]
    log.setLevel(logging.DEBUG if verbose else logging.INFO)
    log.propagate = False


def parse_args(argv):
    verbose = False
    rest = []
    for item in argv:
        if item in ("-v", "--verbose"):
            verbose = True
        elif item in ("-h", "--help"):
            print(__doc__.strip())
            sys.exit(0)
        elif item.startswith("-"):
            raise HostServiceError(f"unknown option: {item}")
        else:
            rest.append(item)
    if len(rest) != 1:
        raise HostServiceError(f"expected exactly one Gradle command (allowed: {allowed_description()})")
    command = rest[0]
    if not allowed(command):
        raise HostServiceError(f'"{command}" is not allowed (allowed: {allowed_description()})')
    return command, verbose


def allowed(command):
    if command == "all" or command in ALLOWED_TASKS:
        return True
    subproject, separator, task = command.partition(":")
    if not separator:
        return False
    return subproject in ALLOWED_SUBPROJECTS and task in ALLOWED_TASKS


def allowed_description():
    subprojects = ", ".join(ALLOWED_SUBPROJECTS)
    tasks = ", ".join(ALLOWED_TASKS)
    return f"{tasks}; or <{subprojects}>:<task>; or all"


def base_url():
    url = os.environ.get("HOST_SERVICE_URL", DEFAULT_URL).strip().rstrip("/")
    if "://" not in url:
        url = "http://" + url
    return url


def candidates(here):
    """Container paths that might be the service's dockerRoot.

    The directory this script runs from comes first: it is the most specific
    answer, while generic roots like /project/minecraft only identify the mount
    and would make the service run Gradle in the wrong directory.
    """
    seen = set()
    result = []
    for path in (
        os.path.abspath(here),
        os.path.realpath(here),
        os.path.dirname(os.path.abspath(here)),
        "/project/minecraft",
        "/project",
        "/workspace",
        "/workspaces",
        "/work",
    ):
        if path not in seen:
            seen.add(path)
            result.append(path)
    return [path for path in result if path and path != "/"]


def _opener():
    # Never route host-service traffic through a container proxy.
    return urllib.request.build_opener(urllib.request.ProxyHandler({}))


def post_json(url, payload, timeout):
    data = json.dumps(payload).encode("utf-8")
    request = urllib.request.Request(
        url, data=data, headers={"Content-Type": "application/json"}, method="POST"
    )
    try:
        return _opener().open(request, timeout=timeout)
    except urllib.error.HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="replace")
        try:
            parsed = json.loads(detail)
            detail = parsed.get("message") or parsed.get("error") or detail
        except ValueError:
            pass
        raise HostServiceError(f"{url} returned HTTP {exc.code}: {detail.strip()}") from exc
    except urllib.error.URLError as exc:
        raise HostServiceError(
            f"cannot reach the Host Service at {url} ({exc.reason}).\n"
            "  - is it running on the host?\n"
            "  - does the host firewall allow this port?\n"
            "  - override the address with HOST_SERVICE_URL if this container needs another one"
        ) from exc


def identify(url, here):
    """Return (container path, host path) for the current directory.

    HOST_SERVICE_DIRECTORY skips the probe and states the container path
    outright; otherwise the service is asked which candidate root it can map.
    """
    override = os.environ.get("HOST_SERVICE_DIRECTORY", "").strip()
    if override:
        if not override.startswith("/"):
            raise HostServiceError("HOST_SERVICE_DIRECTORY must be an absolute container path")
        return override, None

    response = post_json(f"{url}/probe", {"candidates": candidates(here)}, DEFAULT_TIMEOUT)
    with response:
        payload = json.loads(response.read().decode("utf-8"))
    matched = payload.get("matched")
    if not matched:
        raise HostServiceError(
            f"the Host Service cannot map {here}.\n"
            f"  its container root is {payload.get('dockerRoot')} "
            f"(host root {payload.get('hostRoot')})\n"
            "  fix dockerRoot in the host config.json, or run this from inside that root"
        )
    if matched["docker"] != os.path.abspath(here):
        # A parent matched (usually the mount root): warn instead of silently
        # turning "build this project" into "build something else".
        log.warning(
            "matched %s instead of %s; the service is not mounting that project directory",
            matched["docker"],
            os.path.abspath(here),
        )
    return matched["docker"], matched["host"]


def run(url, directory, command):
    response = post_json(f"{url}/gradle", {"directory": directory, "task": command}, DEFAULT_TIMEOUT)
    exit_code = None
    with response:
        for line in response:
            line = line.strip()
            if not line:
                continue
            try:
                event = json.loads(line.decode("utf-8"))
            except (UnicodeDecodeError, json.JSONDecodeError):
                log.debug("skipping malformed event line")
                continue
            exit_code = handle_event(event, exit_code)
    if exit_code is None:
        raise HostServiceError("the Host Service closed the stream without an exit code")
    return exit_code


def handle_event(event, exit_code):
    kind = event.get("type")
    if kind == "start":
        log.info("project %s (host %s)", event.get("directory"), event.get("hostDirectory"))
        tasks = event.get("tasks") or []
        if len(tasks) > 1:
            log.info("commands: %s", ", ".join(tasks))
    elif kind == "task_start":
        log.info("> %s", event.get("task"))
    elif kind == "task_end":
        log.info("< %s exited with %s", event.get("task"), event.get("exitCode"))
    elif kind in ("stdout", "stderr"):
        stream = sys.stdout if kind == "stdout" else sys.stderr
        stream.write(event.get("data", ""))
        stream.flush()
    elif kind == "end":
        log.info("finished: %s", event.get("reason") or "ok")
        return event.get("exitCode")
    elif kind == "error":
        log.error("%s", event.get("message"))
        return 1
    return exit_code


def main(argv):
    try:
        command, verbose = parse_args(argv)
    except HostServiceError as exc:
        print(f"gradlew-host: {exc}", file=sys.stderr)
        return 2
    configure_logging(verbose)

    here = os.getcwd()
    url = base_url()
    try:
        directory, _host_directory = identify(url, here)
        return run(url, directory, command)
    except HostServiceError as exc:
        log.error("%s", exc)
        return 2
    except KeyboardInterrupt:
        # The stream is gone; the host side notices and cancels the build.
        log.error("interrupted; the build on the host may still be running, "
                  "cancel it with POST %s/gradle/cancel", url)
        return 130


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
