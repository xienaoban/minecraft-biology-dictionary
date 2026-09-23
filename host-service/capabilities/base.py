"""Shared types for capabilities on top of the service layer."""

import os
import queue
import signal
import subprocess
import threading
import time

# Silence between two output chunks before a stream reports being idle.
IDLE_SECONDS = 30


class JobAlreadyRunning(Exception):
    """Raised when a capability already has a job reserved for a resource key."""


class JobRejected(Exception):
    """A capability's lazy validation failed; safe to report before streaming.

    A capability validates inside its (lazy) generator so that no work starts
    until the event stream is consumed.  The service layer converts this into
    the wrapped service error before any HTTP response is written.
    """

    def __init__(self, error):
        super().__init__(str(error))
        self.error = error


class Request:
    """A validated incoming request handed to a capability."""

    def __init__(self, method, path, body, query, logging):
        self.method = method
        self.path = path
        self.body = body if isinstance(body, dict) else {}
        self.query = query
        self.logging = logging


class Context:
    """Services the server exposes to capabilities: job slots, paths, logging.

    A capability reserves a job key before it starts streaming.  The reservation
    also owns the process handle, so a second job for the same key is rejected
    and a stuck process can be cancelled.
    """

    def __init__(self, config, path_mapper, logging):
        self.config = config
        self.path_mapper = path_mapper
        self.logging = logging
        self._lock = threading.Lock()
        self._jobs = {}

    def reserve(self, key):
        """Claim key for a new job. Raises JobAlreadyRunning when it is taken."""
        with self._lock:
            if key in self._jobs:
                raise JobAlreadyRunning(key)
            self._jobs[key] = {"process": None, "cancelRequested": False}

    def release(self, key):
        """Drop the reservation for key, terminating any process still alive."""
        with self._lock:
            record = self._jobs.pop(key, None)
        if record is not None:
            _terminate_tree(record["process"])

    def set_process(self, key, process):
        with self._lock:
            record = self._jobs.get(key)
            if record is not None:
                record["process"] = process

    def reserve_with_grace(self, key, grace_seconds, poll_seconds=0.05):
        """Reserve key, waiting briefly for a just-finished job to release it.

        A finished job releases its slot as soon as its response has been
        written, so a short wait absorbs that lag and a normal back-to-back
        request never has to take anything over.  A slot whose process is still
        alive is a real conflict; a slot whose process is gone (the client
        disconnected, or the build was killed) is taken over.
        """
        deadline = time.monotonic() + grace_seconds
        first = True
        while True:
            try:
                self.reserve(key)
                return
            except JobAlreadyRunning:
                if first and self.process_alive(key):
                    raise
                first = False
            if time.monotonic() >= deadline:
                break
            time.sleep(poll_seconds)
        if self.process_alive(key):
            raise JobAlreadyRunning(key)
        self.logging.warning("taking over the job slot for %s; its process is gone", key)
        self.release(key)
        self.reserve(key)

    def cancel(self, key):
        """Terminate the job reserved for key. Returns True when one was signalled."""
        with self._lock:
            record = self._jobs.get(key)
            if record is None:
                return False
            record["cancelRequested"] = True
            process = record["process"]
        if process is None or process.poll() is not None:
            # The job is reserved but has not spawned its process yet.
            return True
        _terminate_tree(process)
        return True

    def process_alive(self, key):
        """True when the reserved job has a process that is still running."""
        with self._lock:
            record = self._jobs.get(key)
            process = record["process"] if record else None
        return process is not None and process.poll() is None

    def cancel_requested(self, key):
        with self._lock:
            record = self._jobs.get(key)
            return bool(record and record["cancelRequested"])

    def is_reserved(self, key):
        with self._lock:
            return key in self._jobs

    def running_keys(self):
        with self._lock:
            return sorted(self._jobs)


class Capability:
    """Base class for a host capability. Subclasses implement the handlers."""

    name = "capability"
    routes = ()
    # Extra endpoints served by a named method instead of the main handler.
    extra_routes = ()
    # Streaming capabilities write an NDJSON event stream; others return JSON.
    streaming = False

    def handle(self, request, context):
        """Return the response: a dict, or an event iterable for streaming.

        Validation errors should be raised as JobRejected while the event
        stream is still untouched, so the service can answer with a real HTTP
        error instead of a half-written 200 response.
        """
        raise NotImplementedError


class FirstEvent:
    """A stream whose first event has already been pulled.

    Validation happens on the first next(), before any HTTP response is
    written.  This wrapper hands that event back and then defers to the
    original stream, so the stream's own close() still reaches the cleanup.
    """

    def __init__(self, first, stream):
        self._first = first
        self._stream = stream
        self._done = False

    def __iter__(self):
        return self

    def __next__(self):
        if not self._done:
            self._done = True
            return self._first
        return next(self._stream)

    def close(self):
        self._stream.close()


class JobRun:
    """One reserved job and the event stream it produces.

    The job key is reserved when the object is created, so a conflict is
    detected before any HTTP response has been written.  The reservation is
    released exactly once, by close() or by the context manager.
    """

    def __init__(self, key, events, context, logging, description):
        self.key = key
        self.description = description
        self._events = iter(events)
        self._context = context
        self._logging = logging
        self._closed = False

    def __iter__(self):
        return self

    def __next__(self):
        return next(self._events)

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_value, traceback):
        self.close()
        return False

    def close(self):
        if self._closed:
            return
        self._closed = True
        self._context.release(self.key)
        self._logging.info("job finished: %s", self.description)


def _creation_flags():
    return subprocess.CREATE_NEW_PROCESS_GROUP if os.name == "nt" else 0


def spawn(argv, cwd, env):
    """Start a process without a shell, in its own process group.

    The separate group matters for cancellation: killing a group that still
    contains this service would take the service down with the build.
    """
    return subprocess.Popen(
        argv,
        cwd=cwd,
        env=env,
        stdin=subprocess.DEVNULL,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        shell=False,
        bufsize=0,
        start_new_session=os.name != "nt",
        creationflags=_creation_flags(),
    )


def _terminate_tree(process):
    if process is None or process.poll() is not None:
        return
    if os.name == "nt":
        subprocess.run(
            ["taskkill", "/T", "/F", "/PID", str(process.pid)],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
            check=False,
        )
        return
    try:
        os.killpg(os.getpgid(process.pid), signal.SIGTERM)
    except (ProcessLookupError, PermissionError, OSError):
        process.terminate()


def kill_tree(process):
    """Best-effort termination of a process and its children."""
    _terminate_tree(process)


def close_stream(events):
    """Run a capability's deferred cleanup.

    Only a closeable stream is closed: draining a plain iterator (for example
    the itertools.chain used to look at the first event before streaming) would
    not reach the generator behind it, so its cleanup would never run.
    """
    close = getattr(events, "close", None)
    if close is None:
        return
    try:
        close()
    except Exception:  # noqa: BLE001 - cleanup must not mask a real error
        pass


def set_binary_mode(pipe):
    """Make a subprocess pipe binary so os.read()/read() skip the text layer."""
    if pipe is None:
        return
    try:
        import msvcrt

        msvcrt.setmode(pipe.fileno(), os.O_BINARY)
    except ImportError:
        pass


class StreamPump:
    """Real-time reader for a process's stdout/stderr.

    A dedicated reader thread per stream feeds a queue, which works on every
    platform: non-blocking reads are not available for pipes on Windows, and
    select() does not work on pipes there either.

    A consumer may stop iterating early (the client disconnected, or a cancel
    ended the stream).  finish() then drains whatever is left so the threads do
    not linger blocked on a pipe, and releases them when they report EOF.
    """

    def __init__(self, process):
        self._queue = queue.Queue()
        self._threads = []
        self._pipes = [pipe for pipe in (process.stdout, process.stderr) if pipe is not None]
        for name, pipe in (("stdout", process.stdout), ("stderr", process.stderr)):
            thread = threading.Thread(
                target=self._reader, args=(name, pipe), daemon=True, name=f"pump-{name}"
            )
            thread.start()
            self._threads.append(thread)

    def _reader(self, name, pipe):
        try:
            while True:
                chunk = pipe.read(65536)
                if not chunk:
                    break
                self._queue.put((name, chunk))
        except (ValueError, OSError):
            pass
        finally:
            self._queue.put((name, None))

    def __iter__(self):
        remaining = len(self._threads)
        while remaining:
            try:
                name, chunk = self._queue.get(timeout=IDLE_SECONDS)
            except queue.Empty:
                yield "__idle__", b""
                continue
            if chunk is None:
                remaining -= 1
                continue
            yield name, chunk

    def finish(self, timeout=1):
        """Release the readers after the process is done.

        Closing our pipe handles unblocks a reader that is still waiting for
        data, so this returns promptly instead of waiting out a drain timeout
        (which would delay the end of the HTTP response).
        """
        for thread in self._threads:
            if thread.is_alive():
                thread.join(0.05)
        for pipe in self._pipes:
            try:
                pipe.close()
            except OSError:
                pass
        for thread in self._threads:
            if thread.is_alive():
                thread.join(timeout)


def decode(data):
    """Decode process output as UTF-8 with CRLF folded to LF."""
    return data.decode("utf-8", errors="replace").replace("\r\n", "\n").replace("\r", "\n")
