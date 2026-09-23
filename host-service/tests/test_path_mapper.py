"""Self-checks for the path mapper: mapping, traversal and project whitelist."""

import os
import sys
import tempfile

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from errors import BadRequest, Forbidden, NotFound  # noqa: E402
from path_mapper import PathMapper  # noqa: E402

FAILURES = []


def check(name, fn):
    try:
        fn()
    except Exception as exc:  # noqa: BLE001 - a test harness reports everything
        FAILURES.append(f"{name}: {type(exc).__name__}: {exc}")
        print(f"FAIL {name}: {type(exc).__name__}: {exc}")
    else:
        print(f"ok   {name}")


def expect_error(exc_type, fn, *args, **kwargs):
    try:
        fn(*args, **kwargs)
    except exc_type:
        return
    except Exception as exc:  # noqa: BLE001
        raise AssertionError(f"expected {exc_type.__name__}, got {type(exc).__name__}: {exc}") from exc
    raise AssertionError(f"expected {exc_type.__name__}, nothing was raised")


def main():
    with tempfile.TemporaryDirectory() as tmp:
        host_root = os.path.join(tmp, "host")
        project = os.path.join(host_root, "mod-26.1.2")
        other = os.path.join(tmp, "outside")
        os.makedirs(os.path.join(project, "fabric"))
        os.makedirs(other)

        mapper = PathMapper("/project/minecraft", host_root, allowed_paths=("mod-26.1.2",))

        check("maps the project root", lambda: _expect(
            mapper.resolve("/project/minecraft/mod-26.1.2"), project))
        check("maps a subproject", lambda: _expect(
            mapper.resolve("/project/minecraft/mod-26.1.2/fabric"),
            os.path.join(project, "fabric")))
        check("collapses inside dots", lambda: _expect(
            mapper.resolve("/project/minecraft/mod-26.1.2/./fabric/../fabric"),
            os.path.join(project, "fabric")))
        check("maps the docker root itself", lambda: _expect(
            mapper.resolve("/project/minecraft", must_exist=False), host_root))

        check("rejects traversal", lambda: expect_error(
            Forbidden, mapper.resolve, "/project/minecraft/../outside"))
        check("rejects deep traversal", lambda: expect_error(
            Forbidden, mapper.resolve, "/project/minecraft/mod-26.1.2/../../../etc"))
        check("rejects a host path", lambda: expect_error(
            Forbidden, mapper.resolve, r"E:\project\minecraft\mod-26.1.2"))
        check("rejects a windows drive path", lambda: expect_error(
            BadRequest, mapper.resolve, "C:/project/minecraft"))
        check("rejects a relative path", lambda: expect_error(
            BadRequest, mapper.resolve, "mod-26.1.2"))
        check("rejects a non-project directory", lambda: expect_error(
            Forbidden, mapper.resolve, "/project/minecraft/other"))
        check("rejects sibling prefix confusion", lambda: expect_error(
            Forbidden, mapper.resolve, "/project/minecraft/mod-26.1.2-extra"))
        check("rejects an empty path", lambda: expect_error(
            BadRequest, mapper.resolve, "   "))
        check("reports a missing project", lambda: expect_error(
            NotFound, mapper.resolve, "/project/minecraft/mod-26.1.2/missing"))

        # A symlink inside the project must not escape it.
        link = os.path.join(project, "escape")
        try:
            os.symlink(other, link)
        except (OSError, NotImplementedError):
            print("skip symlink escape check (no symlink support)")
        else:
            check("rejects a symlink escape", lambda: expect_error(
                Forbidden, mapper.resolve, "/project/minecraft/mod-26.1.2/escape"))

    # A mapper without a project whitelist still blocks traversal.
    with tempfile.TemporaryDirectory() as tmp:
        host_root = os.path.join(tmp, "host")
        os.makedirs(os.path.join(host_root, "a"))
        mapper = PathMapper("/project", host_root)
        check("whitelist-free mapping works", lambda: _expect(
            mapper.resolve("/project/a"), os.path.join(host_root, "a")))
        check("whitelist-free traversal still blocked", lambda: expect_error(
            Forbidden, mapper.resolve, "/project/../host/a"))

    if FAILURES:
        print(f"\n{len(FAILURES)} check(s) failed")
        return 1
    print("\nall path mapper checks passed")
    return 0


def _expect(actual, expected):
    if os.path.normcase(actual) != os.path.normcase(expected):
        raise AssertionError(f"expected {expected}, got {actual}")


if __name__ == "__main__":
    sys.exit(main())
