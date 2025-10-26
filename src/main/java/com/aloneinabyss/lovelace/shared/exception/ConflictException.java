package com.aloneinabyss.lovelace.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a request conflicts with the current state.
 * HTTP Status: 409 Conflict
 * Examples: Email already verified, password reset already pending
 */
public class ConflictException extends ApplicationException {
    
    public ConflictException(ErrorCode errorCode, Object... messageArgs) {
        super(errorCode.name(), errorCode.getMessageKey(), HttpStatus.CONFLICT, messageArgs);
    }
    
    public ConflictException(String errorCode, String messageKey, Object... messageArgs) {
        super(errorCode, messageKey, HttpStatus.CONFLICT, messageArgs);
    }
}
