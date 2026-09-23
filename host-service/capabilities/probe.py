"""Probe capability.

The container side often cannot know how its project root is named on the host
(the same files may be reachable as ``/project`` or ``/project/minecraft``).
It therefore asks which of its candidate roots this service can map; that
avoids asking a human to correct config.json every time the layout changes.

Several candidates usually map (the mount root and the project inside it), so
the best one wins: a directory that actually holds a Gradle wrapper, then the
longest path.
"""

import os

from capabilities.base import Capability
from errors import BadRequest, ServiceError

MAX_CANDIDATES = 32
WRAPPER_NAMES = ("gradlew.bat", "gradlew")


class ProbeCapability(Capability):
    name = "probe"
    routes = (("POST", "/probe"),)

    def handle(self, request, context):
        candidates = request.body.get("candidates")
        unknown = sorted(set(request.body) - {"candidates"})
        if unknown:
            raise BadRequest(f"unknown request field(s): {', '.join(unknown)}")
        if not isinstance(candidates, list) or not candidates:
            raise BadRequest('"candidates" must be a non-empty array of container paths')
        if len(candidates) > MAX_CANDIDATES:
            raise BadRequest(f'"candidates" must contain at most {MAX_CANDIDATES} entries')

        matched = None
        for candidate in candidates:
            if not isinstance(candidate, str):
                raise BadRequest('"candidates" must contain only strings')
            try:
                mapped = context.path_mapper.resolve(candidate)
            except ServiceError:
                # A rejected candidate is simply not a match.
                continue
            if matched is None or _score(mapped) > _score(matched["host"]):
                matched = {"docker": candidate, "host": mapped}
        return {
            "matched": matched,
            "dockerRoot": context.path_mapper.docker_root,
            "hostRoot": context.path_mapper.host_root,
        }


def _score(host_directory):
    has_wrapper = any(
        os.path.isfile(os.path.join(host_directory, name)) for name in WRAPPER_NAMES
    )
    return (1 if has_wrapper else 0, len(host_directory))
