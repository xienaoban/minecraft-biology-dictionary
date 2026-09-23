"""Host capabilities.  Only capabilities listed here are reachable."""

from capabilities.gradle import GradleCapability
from capabilities.probe import ProbeCapability


def all_capabilities():
    """Return the capabilities compiled into this build, in registration order."""
    return [GradleCapability(), ProbeCapability()]
