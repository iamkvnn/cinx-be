package com.cinx.common.exception;

public class BadRequestException extends RuntimeException implements ErrorCodedException {
    private final ErrorCode errorCode;

    public BadRequestException(String message) {
        this(ErrorCode.BAD_REQUEST, message);
    }

    public BadRequestException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    @Override
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
