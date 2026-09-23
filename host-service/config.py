"""Configuration loading and validation.

The config file only describes the environment (where things are and where to
listen).  It never grants capabilities: which capabilities exist, which Gradle
tasks may run and which parameters are accepted are decided by code.
"""

import copy
import json
import os
from pathlib import Path

CONFIG_FILE_NAME = "config.json"

# hostRoot is deliberately a placeholder: it will not exist on a fresh machine,
# so startup fails with an explicit "configure me" error instead of guessing.
DEFAULT_CONFIG = {
    "dockerRoot": "/project/minecraft",
    "hostRoot": "/srv/minecraft" if os.name != "nt" else "C:\\minecraft",
    "server": {
        "host": "0.0.0.0",
        "port": 11109,
    },
}


class ConfigError(Exception):
    """Raised when the configuration file is missing required or valid values."""


def _merge_defaults(defaults, values):
    merged = copy.deepcopy(defaults)
    for key, value in values.items():
        if isinstance(value, dict) and isinstance(merged.get(key), dict):
            merged[key] = _merge_defaults(merged[key], value)
        else:
            merged[key] = value
    return merged


def _require_non_empty_string(config, key):
    value = config.get(key)
    if not isinstance(value, str) or not value.strip():
        raise ConfigError(f'"{key}" must be a non-empty string')
    return value.strip()


def _validate_server(config):
    server = config.get("server")
    if not isinstance(server, dict):
        raise ConfigError('"server" must be an object')

    host = server.get("host")
    if not isinstance(host, str) or not host.strip():
        raise ConfigError('"server.host" must be a non-empty string')
    server["host"] = host.strip()

    port = server.get("port")
    if isinstance(port, bool) or not isinstance(port, int) or not 1 <= port <= 65535:
        raise ConfigError('"server.port" must be an integer between 1 and 65535')
    return server


def validate(raw):
    """Validate a raw config mapping and return a normalized copy."""
    if not isinstance(raw, dict):
        raise ConfigError("config must be a JSON object")

    config = _merge_defaults(DEFAULT_CONFIG, raw)

    config["dockerRoot"] = _require_non_empty_string(config, "dockerRoot")
    config["hostRoot"] = _require_non_empty_string(config, "hostRoot")
    if not config["dockerRoot"].startswith("/"):
        raise ConfigError('"dockerRoot" must be an absolute POSIX path, e.g. "/project/minecraft"')

    _validate_server(config)
    return config


def config_path(service_dir=None):
    directory = Path(service_dir) if service_dir else Path(__file__).resolve().parent
    return directory / CONFIG_FILE_NAME


def write_default(path):
    """Write the default config file, never overwriting an existing one."""
    path = Path(path)
    if path.exists():
        return False
    path.write_text(json.dumps(DEFAULT_CONFIG, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    return True


def load(service_dir=None):
    """Load the config file, generating the default one on first start.

    Returns (config, created) where created tells whether the file was just
    generated.  An invalid config raises ConfigError; the caller must not fall
    back to unsafe defaults.
    """
    path = config_path(service_dir)
    created = write_default(path)
    try:
        raw = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        raise ConfigError(f"{path} is not valid JSON: {exc}") from exc
    return validate(raw), created


def normalize_host_root(host_root):
    """Return the host root in native form, resolving symlinks when possible."""
    native = os.path.abspath(host_root)
    try:
        return os.path.realpath(native)
    except OSError:
        return native
