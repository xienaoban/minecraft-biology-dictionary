"""HTTP layer: request parsing, routing, streaming and error mapping."""

import json
import logging
import os
import platform
import signal
import socket
import threading
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlparse

import config as config_module
from capabilities.base import Context, FirstEvent, JobAlreadyRunning, JobRejected, Request, close_stream
from errors import BadRequest, NotFound, PayloadTooLarge, ServiceError, UnsupportedMediaType

PROTOCOL_VERSION = 1
MAX_BODY_BYTES = 64 * 1024


def _error_payload(error):
    return {"error": type(error).__name__, "message": str(error)}


class Service:
    """Wires configuration, capabilities and job bookkeeping together."""

    def __init__(self, cfg, logging_module):
        self.config = cfg
        self.logging = logging_module
        self.path_mapper = None
        self.context = None
        self.routes = {}
        self.extra_routes = {}
        self._register(config_module.normalize_host_root(cfg["hostRoot"]))

    def _register(self, host_root):
        from path_mapper import PathMapper

        self.path_mapper = PathMapper(self.config["dockerRoot"], host_root)
        self.context = Context(self.config, self.path_mapper, self.logging)
        for capability in _capabilities():
            for method, path in capability.routes:
                self.routes[(method, path)] = capability
            for method, path, method_name in getattr(capability, "extra_routes", ()):
                self.extra_routes[(method, path)] = getattr(capability, method_name)

    def capability_names(self):
        names = []
        for capability in self.routes.values():
            if capability.name not in names:
                names.append(capability.name)
        return names

    def base_info(self):
        return {
            "service": "host-service",
            "protocolVersion": PROTOCOL_VERSION,
            "platform": platform.system(),
            "hostRoot": self.config["hostRoot"],
            "dockerRoot": self.config["dockerRoot"],
            "capabilities": self.capability_names(),
        }


def _capabilities():
    from capabilities import all_capabilities

    return all_capabilities()

class Handler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"
    server_version = "host-service"
    sys_version = ""

    # -- request entry points ------------------------------------------------

    def do_GET(self):
        self._handle("GET")

    def do_POST(self):
        self._handle("POST")

    def _handle(self, method):
        try:
            path = urlparse(self.path).path.rstrip("/") or "/"
            if path == "/health":
                self._send_json(200, dict(self.service.base_info(), status="ok"))
                return
            route = (method, path)
            if route in self.service.extra_routes:
                body = self._read_body()
                request = Request(method, path, body, {}, self.service.logging)
                result = self.service.extra_routes[route](request, self.service.context)
                self._send_json(200, result)
                return
            capability = self.service.routes.get(route)
            if capability is None:
                raise NotFound(f"{method} {path} is not a known capability endpoint")
            body = self._read_body()
            request = Request(method, path, body, {}, self.service.logging)
            response = self._respond(capability, request)
            if getattr(capability, "streaming", False):
                self._stream(response)
            else:
                self._send_json(200, response)
        except JobRejected as exc:
            self._send_json(exc.error.status, _error_payload(exc.error))
        except ServiceError as exc:
            self._send_json(exc.status, _error_payload(exc))
        except JobAlreadyRunning as exc:
            self._send_json(409, {"error": "Conflict", "message": f"a job is already running for {exc}"})
        except BrokenPipeError:
            self.service.logging.info("client disconnected from %s", self.path)
        except Exception as exc:  # noqa: BLE001 - last resort, never leak a traceback to the client
            self.service.logging.exception("unhandled error for %s: %s", self.path, exc)
            self._send_json(500, {"error": "InternalError", "message": "internal error"})

    def _respond(self, capability, request):
        """Run a capability's synchronous part, unwrapping lazy validation errors.

        A streaming capability validates inside its lazy event generator, so the
        first step is pulled here: that way a rejected request still gets a real
        HTTP error status instead of a half-written 200 response.
        """
        response = capability.handle(request, self.service.context)
        if not getattr(capability, "streaming", False):
            return response
        try:
            first = next(response)
        except StopIteration:
            raise JobRejected(BadRequest("the capability produced no events")) from None
        return FirstEvent(first, response)

    def _read_body(self):
        raw_length = self.headers.get("Content-Length")
        if raw_length is None:
            raise BadRequest("Content-Length header is required")
        try:
            length = int(raw_length)
        except ValueError:
            raise BadRequest("Content-Length header is invalid") from None
        if length < 0:
            raise BadRequest("Content-Length header is invalid")
        if length > MAX_BODY_BYTES:
            raise PayloadTooLarge(f"request body larger than {MAX_BODY_BYTES} bytes")
        if length == 0:
            return {}
        content_type = (self.headers.get("Content-Type") or "application/json").split(";")[0].strip()
        if content_type not in ("application/json", ""):
            raise UnsupportedMediaType("Content-Type must be application/json")
        raw = self.rfile.read(length)
        try:
            body = json.loads(raw.decode("utf-8"))
        except (UnicodeDecodeError, json.JSONDecodeError) as exc:
            raise BadRequest(f"request body is not valid JSON: {exc}") from exc
        if body is None:
            return {}
        if not isinstance(body, dict):
            raise BadRequest("request body must be a JSON object")
        return body

    # -- responses -----------------------------------------------------------

    def _send_json(self, status, payload):
        data = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def _stream(self, events):
        """Write NDJSON events with chunked transfer encoding."""
        self.send_response(200)
        self.send_header("Content-Type", "application/x-ndjson; charset=utf-8")
        self.send_header("Transfer-Encoding", "chunked")
        self.send_header("Cache-Control", "no-store")
        self.send_header("X-Accel-Buffering", "no")
        self.end_headers()
        try:
            for event in events:
                self._write_chunk((json.dumps(event, ensure_ascii=False) + "\n").encode("utf-8"))
        except BrokenPipeError:
            self.service.logging.info("client disconnected during streaming")
        finally:
            # Closing the event stream runs the capability's cleanup, which
            # releases the job slot and terminates a process nobody is
            # listening to.  Iterating the response itself is not enough: a
            # StopIteration inside the for loop above skips this on purpose.
            close_stream(events)
            try:
                self._write_chunk(b"")
            except BrokenPipeError:
                pass

    def _write_chunk(self, data):
        if data:
            self.wfile.write(b"%X\r\n" % len(data) + data + b"\r\n")
        else:
            self.wfile.write(b"0\r\n\r\n")
        self.wfile.flush()

    @property
    def service(self):
        return self.server.service

    def log_message(self, fmt, *args):
        self.service.logging.info("%s - %s", self.address_string(), fmt % args)


def build_server(cfg, logging_module):
    service = Service(cfg, logging_module)
    host = cfg["server"]["host"]
    port = cfg["server"]["port"]
    server = ThreadingHTTPServer((host, port), Handler)
    server.daemon_threads = True
    server.service = service
    return server


def local_addresses():
    """Best-effort list of IPv4 addresses other machines can reach."""
    addresses = set()
    try:
        for info in socket.getaddrinfo(socket.gethostname(), None, socket.AF_INET):
            addresses.add(info[4][0])
    except socket.gaierror:
        pass
    return sorted(addresses)


def run(cfg, logging_module):
    server = build_server(cfg, logging_module)
    host, port = server.server_address[:2]
    logging_module.info("Host Service listening on http://%s:%s", host, port)
    for address in local_addresses():
        logging_module.info("reachable from the network at http://%s:%s", address, port)
    logging_module.info("dockerRoot=%s hostRoot=%s", cfg["dockerRoot"], cfg["hostRoot"])

    stop = threading.Event()

    def shutdown(signum, _frame):
        if stop.is_set():
            os._exit(1)
        stop.set()
        logging_module.info("shutting down (signal %s)", signum)
        threading.Thread(target=server.shutdown, daemon=True).start()

    for name in ("SIGINT", "SIGTERM"):
        if hasattr(signal, name):
            try:
                signal.signal(getattr(signal, name), shutdown)
            except ValueError:
                pass

    try:
        server.serve_forever(poll_interval=0.2)
    finally:
        server.server_close()
        logging_module.info("Host Service stopped")
    return 0
