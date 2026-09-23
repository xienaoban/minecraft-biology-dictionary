"""Error types shared by the service layer and the capabilities."""


class ServiceError(Exception):
    """An error that maps onto a specific HTTP status code."""

    status = 500

    def __init__(self, message, status=None):
        super().__init__(message)
        if status is not None:
            self.status = status


class BadRequest(ServiceError):
    status = 400


class Unauthorized(ServiceError):
    status = 401


class Forbidden(ServiceError):
    status = 403


class NotFound(ServiceError):
    status = 404


class Conflict(ServiceError):
    status = 409


class PayloadTooLarge(ServiceError):
    status = 413


class UnsupportedMediaType(ServiceError):
    status = 415
