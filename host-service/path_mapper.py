"""Mapping a container path onto its host counterpart.

This module is part of the security boundary: the client may only ever submit a
path as seen inside the container.  Host paths are always derived here, and the
result must stay inside the configured host root.
"""

import os
import posixpath

from errors import BadRequest, Forbidden, NotFound


def _strip_volume_prefix(path):
    """Split a possibly extended-length path into (prefix, tail).

    ``\\?\\C:\\dir`` and ``\\\\?\\UNC\\server\\share\\dir`` must keep their
    prefix, otherwise joining and normalizing them produces nonsense.
    """
    if not path.startswith("\\\\"):
        return "", path
    if path.startswith("\\\\?\\UNC\\"):
        rest = path[len("\\\\?\\UNC\\"):]
        parts = rest.split("\\", 2)
        if len(parts) < 3:
            return path, ""
        return "\\\\?\\UNC\\" + parts[0] + "\\" + parts[1], parts[2]
    if path.startswith("\\\\?\\"):
        rest = path[len("\\\\?\\"):]
        parts = rest.split("\\", 1)
        if len(parts) < 2:
            return path, ""
        return "\\\\?\\" + parts[0], parts[1]
    return "", path


def _collapse(path):
    """Lexically normalize a host path without resolving symlinks."""
    prefix, tail = _strip_volume_prefix(path)
    normalized = os.path.normpath(tail) if tail else "."
    if prefix:
        return prefix + "\\" + normalized

    drive, rest = os.path.splitdrive(normalized)
    if drive and rest in ("", os.sep):
        return drive + os.sep
    return normalized


def _is_within(candidate, root):
    """True when candidate is root itself or lies below root."""
    try:
        return os.path.commonpath([candidate, root]) == root
    except ValueError:
        # Different drives / one path relative and the other absolute.
        return False


def _fallback_roots(host_root):
    """Roots to use when the resolved host root contains no matching name.

    Handles the common WSL/Windows split where the service reports a path such
    as \\\\wsl.localhost\\Ubuntu\\mnt\\e\\... while the mount itself is
    configured as /mnt/e/....  A Windows host has no such split, and treating
    "/mnt/e/x" as a Windows path there would silently accept a path that is not
    under the configured root.
    """
    if os.name == "nt":
        return []
    roots = []
    wsl_unc = r"\\wsl.localhost"
    wsl_legacy = r"\\wsl$"
    prefixes = (wsl_unc + "\\", wsl_legacy + "\\")
    if host_root.startswith(prefixes):
        rest = host_root.split("\\", 3)
        if len(rest) == 4:
            distro_and_mount = rest[3]
            if distro_and_mount.startswith("mnt\\"):
                roots.append("/" + distro_and_mount.replace("\\", "/"))
    if host_root.startswith("/mnt/") and len(host_root) > len("/mnt/") + 1:
        drive = host_root[len("/mnt/")]
        if drive.isalpha():
            windows_root = drive.upper() + ":" + host_root[len("/mnt/") + 1:].replace("/", "\\")
            if windows_root.endswith("\\"):
                windows_root = windows_root[:-1]
            roots.append(windows_root or drive.upper() + ":\\")
    return roots


class PathMapper:
    """Translates container paths into validated host paths."""

    def __init__(self, docker_root, host_root, allowed_paths=()):
        self.docker_root = "/" + docker_root.strip("/")
        self.host_root = _collapse(host_root)
        self.roots = [self.host_root] + [_collapse(root) for root in _fallback_roots(self.host_root)]
        self.allowed_prefixes = []
        for allowed in allowed_paths:
            prefix = self.docker_root + "/" + allowed.strip("/")
            self.allowed_prefixes.append(prefix)

    def _relative(self, docker_path):
        raw = docker_path.strip()
        if not raw:
            raise BadRequest("directory must not be empty")
        if "\\" in raw:
            raise Forbidden("directory must be a container path, not a host path")

        # Container paths are always POSIX: os.path.normpath would rewrite
        # "/project/x" into "\\project\\x" when the service runs on Windows.
        collapsed = posixpath.normpath(raw)
        if not collapsed.startswith("/"):
            raise BadRequest("directory must be an absolute container path")

        if collapsed != self.docker_root and not collapsed.startswith(self.docker_root + "/"):
            raise Forbidden(f"{raw} is outside the configured container root {self.docker_root}")

        relative = posixpath.relpath(collapsed, self.docker_root)
        if relative == ".":
            relative = ""
        elif relative == ".." or relative.startswith("../"):
            raise Forbidden(f"{raw} escapes the configured container root")

        # A symlink inside the project must not lead outside the container root.
        # On Windows a leading "/" is drive-relative, so a container path can
        # appear to exist on the host; the host-side check in resolve() covers
        # that case instead.
        if os.name != "nt" and os.path.lexists(collapsed):
            real = posixpath.normpath(os.path.realpath(collapsed))
            if real != collapsed:
                if real != self.docker_root and not real.startswith(self.docker_root + "/"):
                    raise Forbidden(f"{raw} resolves outside the configured container root")
                relative = posixpath.relpath(real, self.docker_root)
                collapsed = real

        if relative and self.allowed_prefixes:
            # The project whitelist is expressed in container paths.
            if not any(
                collapsed == prefix or collapsed.startswith(prefix + "/")
                for prefix in self.allowed_prefixes
            ):
                raise Forbidden(f"{raw} is outside the allowed project paths")
        return relative

    def _join(self, root, relative):
        if not relative:
            return root
        # Append the child explicitly: os.path.join drops the root's trailing
        # separator, which would break the containment check for "E:\".
        tail = relative.replace("/", os.sep)
        if root.endswith(os.sep) or root.endswith("\\") or root.endswith("/"):
            return root + tail
        return root + os.sep + tail

    def resolve(self, docker_path, must_exist=True):
        """Map a container path to a host path.

        Raises BadRequest / Forbidden / NotFound on any violation.
        """
        relative = self._relative(docker_path)
        escaped = None
        for root in self.roots:
            candidate = _collapse(self._join(root, relative))
            if not _is_within(candidate, root):
                continue
            # A symlink inside the project must not lead outside the host root.
            real = _collapse(os.path.realpath(candidate))
            if not _is_within(real, root):
                escaped = f"{docker_path} resolves to {real}, outside {root}"
                continue
            if not must_exist or os.path.isdir(candidate):
                return candidate
        if escaped is not None:
            raise Forbidden(escaped)
        if must_exist:
            raise NotFound(f"{docker_path} does not exist on the host")
        # Nothing matched; report the primary mapping so the caller can show it.
        return _collapse(self._join(self.host_root, relative))
