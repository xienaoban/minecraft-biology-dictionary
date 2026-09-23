"""End-to-end checks against a running service, using a fake Gradle project."""

import json
import logging
import os
import sys
import tempfile
import threading
import time
import urllib.error
import urllib.request

SERVICE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, SERVICE_DIR)

import server as server_module  # noqa: E402

URL = None  # set by start_service; the port is chosen by the OS

FAKE_GRADLEW = """#!/bin/sh
echo "stdout line 1"
echo "stderr line 1" >&2
sleep ${FAKE_SLEEP:-0.2}
echo "stdout line 2"
sleep ${FAKE_SLEEP:-0.2}
echo "stdout line 3"
exit ${FAKE_EXIT_CODE:-0}
"""

FAILURES = []


def post(path, payload, timeout=30):
    data = json.dumps(payload).encode("utf-8")
    request = urllib.request.Request(
        URL + path, data=data, headers={"Content-Type": "application/json"}, method="POST"
    )
    opener = urllib.request.build_opener(urllib.request.ProxyHandler({}))
    return opener.open(request, timeout=timeout)


def start_service(host_root, config_dir):
    """Start the service on an OS-assigned port so parallel runs cannot collide."""
    global URL
    logging.getLogger("host-service").setLevel(logging.ERROR)
    cfg = {
        "dockerRoot": "/project/minecraft",
        "hostRoot": host_root,
        "server": {"host": "127.0.0.1", "port": 0},
    }
    config_path = os.path.join(config_dir, "config.json")
    with open(config_path, "w", encoding="utf-8") as handle:
        json.dump(cfg, handle)

    server = server_module.build_server(cfg, logging.getLogger("host-service"))
    port = server.server_address[1]
    URL = f"http://127.0.0.1:{port}"
    threading.Thread(target=server.serve_forever, kwargs={"poll_interval": 0.1}, daemon=True).start()
    for _ in range(100):
        try:
            with urllib.request.urlopen(f"{URL}/health", timeout=1):
                return
        except Exception:  # noqa: BLE001
            time.sleep(0.05)
    raise RuntimeError("service did not start")


def expect_http(exc_code, fn, label):
    try:
        fn()
    except urllib.error.HTTPError as exc:
        if exc.code == exc_code:
            print(f"ok   {label} -> {exc.code}")
            return
        FAILURES.append(f"{label}: expected HTTP {exc_code}, got {exc.code}")
        print(f"FAIL {label}: expected HTTP {exc_code}, got {exc.code}")
    except Exception as exc:  # noqa: BLE001
        FAILURES.append(f"{label}: {type(exc).__name__}: {exc}")
        print(f"FAIL {label}: {type(exc).__name__}: {exc}")
    else:
        FAILURES.append(f"{label}: expected HTTP {exc_code}, request succeeded")
        print(f"FAIL {label}: expected HTTP {exc_code}, request succeeded")


def main():
    with tempfile.TemporaryDirectory() as tmp:
        host_root = os.path.join(tmp, "host")
        project = os.path.join(host_root, "mod")
        os.makedirs(project)
        wrapper = os.path.join(project, "gradlew")
        with open(wrapper, "w", encoding="utf-8") as handle:
            handle.write(FAKE_GRADLEW)
        os.chmod(wrapper, 0o755)

        start_service(host_root, tmp)

        def check(name, fn):
            try:
                fn()
            except Exception as exc:  # noqa: BLE001
                FAILURES.append(f"{name}: {type(exc).__name__}: {exc}")
                print(f"FAIL {name}: {type(exc).__name__}: {exc}")
            else:
                print(f"ok   {name}")

        check("health", lambda: _expect(
            json.loads(urllib.request.urlopen(f"{URL}/health", timeout=5).read())["status"], "ok"))

        def probe():
            with post("/probe", {"candidates": ["/project/minecraft/mod"]}) as response:
                payload = json.loads(response.read())
            _expect(payload["matched"]["docker"], "/project/minecraft/mod")
        check("probe matches a candidate", probe)

        def streaming():
            started = time.monotonic()
            events = []
            with post("/gradle", {"directory": "/project/minecraft/mod", "task": "build"}) as response:
                first_at = None
                for line in response:
                    if line.strip():
                        events.append(json.loads(line))
                        if first_at is None:
                            first_at = time.monotonic() - started
            kinds = [event["type"] for event in events]
            _expect(kinds[0], "start")
            _expect("stdout" in kinds, True)
            _expect("stderr" in kinds, True)
            _expect(kinds[-1], "end")
            _expect(events[-1]["exitCode"], 0)
            text = "".join(event.get("data", "") for event in events if event["type"] == "stdout")
            _expect("stdout line 3" in text, True)
            _expect(first_at is not None and first_at < 0.5, True)
        check("streams stdout/stderr in real time", streaming)

        def nonzero_exit():
            os.environ["FAKE_EXIT_CODE"] = "7"
            try:
                with post("/gradle", {"directory": "/project/minecraft/mod", "task": "test"}) as response:
                    events = [json.loads(line) for line in response if line.strip()]
            finally:
                os.environ.pop("FAKE_EXIT_CODE", None)
            _expect(events[-1]["type"], "end")
            _expect(events[-1]["exitCode"], 7)
        check("propagates a non-zero exit code", nonzero_exit)

        check("rejects an unlisted task", lambda: expect_http(
            403, lambda: post("/gradle", {"directory": "/project/minecraft/mod", "task": "clean"}), "unlisted task"))
        check("accepts subproject build", lambda: _expect(
            _run("/project/minecraft/mod", "fabric:build")[-1]["exitCode"], 0))
        check("accepts subproject runTestServer", lambda: _expect(
            _run("/project/minecraft/mod", "fabric:runTestServer")[-1]["exitCode"], 0))
        check("accepts neoforge build", lambda: _expect(
            _run("/project/minecraft/mod", "neoforge:build")[-1]["exitCode"], 0))
        check("passes the command to Gradle verbatim", lambda: _expect(
            [e["argv"][-1] for e in _run("/project/minecraft/mod", "fabric:build")
             if e["type"] == "task_start"], ["fabric:build"]))
        check("all builds fabric then neoforge", lambda: _expect(
            [e["task"] for e in _run("/project/minecraft/mod", "all") if e["type"] == "task_start"],
            ["fabric:build", "neoforge:build"]))
        check("rejects an unlisted subproject", lambda: expect_http(
            403, lambda: _run("/project/minecraft/mod", "forge:build"), "unlisted subproject"))
        check("rejects an unlisted subproject task", lambda: expect_http(
            403, lambda: _run("/project/minecraft/mod", "fabric:clean"), "unlisted subproject task"))
        check("rejects a leading colon", lambda: expect_http(
            400, lambda: _run("/project/minecraft/mod", ":build"), "leading colon"))
        check("rejects a command payload", lambda: expect_http(
            400, lambda: post("/gradle", {"directory": "/project/minecraft/mod",
                                          "args": ["build", "clean"]}), "multi-arg request"))
        check("rejects a shell task", lambda: expect_http(
            403, lambda: post("/gradle", {"directory": "/project/minecraft/mod",
                                          "task": "powershell -c whoami"}), "shell-ish task"))
        check("rejects a host path", lambda: expect_http(
            403, lambda: post("/gradle", {"directory": r"E:\\project\\minecraft\\mod",
                                          "task": "build"}), "host path"))
        check("rejects traversal", lambda: expect_http(
            403, lambda: post("/gradle", {"directory": "/project/minecraft/../etc",
                                          "task": "build"}), "traversal"))
        check("rejects a missing project", lambda: expect_http(
            404, lambda: post("/gradle", {"directory": "/project/minecraft/nope",
                                          "task": "build"}), "missing project"))
        check("rejects unknown fields", lambda: expect_http(
            400, lambda: post("/gradle", {"directory": "/project/minecraft/mod", "task": "build",
                                          "command": "whoami"}), "unknown field"))
        check("rejects the removed subproject field", lambda: expect_http(
            400, lambda: post("/gradle", {"directory": "/project/minecraft/mod", "task": "build",
                                          "subproject": "fabric"}), "subproject field"))
        check("404 for an unknown endpoint", lambda: expect_http(
            404, lambda: post("/exec", {"command": "whoami"}), "unknown endpoint"))

        def concurrency():
            results = {}
            os.environ["FAKE_SLEEP"] = "1"

            def slow_request():
                try:
                    with post("/gradle", {"directory": "/project/minecraft/mod", "task": "build"}) as response:
                        for _ in response:
                            pass
                    results["slow"] = "done"
                except urllib.error.HTTPError as exc:
                    results["slow"] = exc.code

            thread = threading.Thread(target=slow_request)
            thread.start()
            try:
                time.sleep(0.4)
                try:
                    post("/gradle", {"directory": "/project/minecraft/mod", "task": "build"})
                except urllib.error.HTTPError as exc:
                    _expect(exc.code, 409)
                else:
                    raise AssertionError("second concurrent job was accepted")

                # A different project may still run in parallel.
                os.makedirs(os.path.join(host_root, "other"))
                with open(os.path.join(host_root, "other", "gradlew"), "w", encoding="utf-8") as handle:
                    handle.write("#!/bin/sh\necho other\nexit 0\n")
                os.chmod(os.path.join(host_root, "other", "gradlew"), 0o755)
                os.environ["FAKE_SLEEP"] = "0"
                with post("/gradle", {"directory": "/project/minecraft/other", "task": "build"}) as response:
                    events = [json.loads(line) for line in response if line.strip()]
                _expect(events[-1]["exitCode"], 0)
            finally:
                os.environ.pop("FAKE_SLEEP", None)
            thread.join(timeout=15)
            _expect(results.get("slow"), "done")
        check("409 on a concurrent job for the same project", concurrency)

        def cancel_running_job():
            os.environ["FAKE_SLEEP"] = "30"
            result = {}

            def slow_request():
                try:
                    with post("/gradle", {"directory": "/project/minecraft/mod", "task": "build"}) as response:
                        events = [json.loads(line) for line in response if line.strip()]
                    result["events"] = events
                except urllib.error.HTTPError as exc:
                    result["error"] = exc.code

            thread = threading.Thread(target=slow_request)
            thread.start()
            try:
                time.sleep(0.5)
                with post("/gradle/cancel", {"directory": "/project/minecraft/mod"}) as response:
                    payload = json.loads(response.read())
                _expect(payload["cancelled"], True)
                thread.join(timeout=15)
                _expect(thread.is_alive(), False)
                _expect(result["events"][-1]["reason"], "cancelled")
                _expect(result["events"][-1]["exitCode"], 130)
                # The slot must be reusable afterwards.
                os.environ["FAKE_SLEEP"] = "0"
                with post("/gradle", {"directory": "/project/minecraft/mod", "task": "build"}) as response:
                    events = [json.loads(line) for line in response if line.strip()]
                _expect(events[-1]["exitCode"], 0)
            finally:
                os.environ.pop("FAKE_SLEEP", None)
        check("cancel stops a running job and frees the project", cancel_running_job)

    if FAILURES:
        print(f"\n{len(FAILURES)} check(s) failed")
        for failure in FAILURES:
            print(f"  - {failure}")
        return 1
    print("\nall service checks passed")
    return 0


def _run(directory, command, timeout=30):
    with post("/gradle", {"directory": directory, "task": command}, timeout=timeout) as response:
        return [json.loads(line) for line in response if line.strip()]


def _expect(actual, expected):
    if actual != expected:
        raise AssertionError(f"expected {expected!r}, got {actual!r}")


if __name__ == "__main__":
    sys.exit(main())
