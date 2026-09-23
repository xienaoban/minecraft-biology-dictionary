"""Entry point: load configuration and serve requests until stopped."""

import argparse
import logging
import os
import sys

# An embedded Python distribution (.pth file) does not put this directory on
# sys.path, so make sibling modules importable no matter how we were started.
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import config as config_module  # noqa: E402
import server  # noqa: E402

LOG_FORMAT = "[%(asctime)s] [%(levelname)s] %(message)s"
DATE_FORMAT = "%Y-%m-%d %H:%M:%S"


def configure_logging(verbose):
    logging.basicConfig(
        level=logging.DEBUG if verbose else logging.INFO,
        format=LOG_FORMAT,
        datefmt=DATE_FORMAT,
        stream=sys.stderr,
    )


def parse_args(argv):
    parser = argparse.ArgumentParser(description="Host Service: limited host capabilities for containerized agents.")
    parser.add_argument("--config", help="path to config.json (defaults to the file next to this script)")
    parser.add_argument("--host", help="override server.host")
    parser.add_argument("--port", type=int, help="override server.port")
    parser.add_argument("--verbose", action="store_true", help="log every HTTP request")
    parser.add_argument("--show-config", action="store_true", help="print the effective config and exit")
    return parser.parse_args(argv)


def main(argv=None):
    args = parse_args(argv if argv is not None else sys.argv[1:])
    configure_logging(args.verbose)
    logging_module = logging.getLogger("host-service")

    service_dir = os.path.dirname(args.config) if args.config else None
    try:
        cfg, created = config_module.load(service_dir)
    except config_module.ConfigError as exc:
        logging_module.error("invalid configuration: %s", exc)
        return 2

    if created:
        logging_module.warning(
            "no config.json found, wrote a default one to %s",
            config_module.config_path(service_dir),
        )
        logging_module.warning(
            "check dockerRoot (%s) and hostRoot (%s) before relying on it",
            cfg["dockerRoot"],
            cfg["hostRoot"],
        )

    if args.host:
        cfg["server"]["host"] = args.host
    if args.port:
        cfg["server"]["port"] = args.port

    if not os.path.isdir(config_module.normalize_host_root(cfg["hostRoot"])):
        logging_module.error(
            "hostRoot does not exist on this machine: %s", cfg["hostRoot"]
        )
        logging_module.error(
            "edit %s and point hostRoot at the host side of the mounted project root",
            config_module.config_path(service_dir),
        )
        return 2

    if cfg["server"]["host"] in ("0.0.0.0", "::"):
        logging_module.warning(
            "listening on every interface; anyone able to reach this port can run the allowed Gradle tasks"
        )

    if args.show_config:
        import json

        print(json.dumps(cfg, indent=2, ensure_ascii=False))
        return 0

    try:
        return server.run(cfg, logging_module)
    except OSError as exc:
        logging_module.error("cannot listen on %s:%s: %s", cfg["server"]["host"], cfg["server"]["port"], exc)
        return 2


if __name__ == "__main__":
    sys.exit(main())
