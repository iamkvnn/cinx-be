package com.cinx.common.exception;

public class ForbiddenException extends RuntimeException implements ErrorCodedException {
    private final ErrorCode errorCode;

    public ForbiddenException(String message) {
        this(ErrorCode.FORBIDDEN, message);
    }

    public ForbiddenException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    @Override
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
