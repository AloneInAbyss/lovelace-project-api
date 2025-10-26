package com.aloneinabyss.lovelace.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when input validation fails.
 * HTTP Status: 400 Bad Request
 * Examples: Username taken, invalid email format, password too weak
 */
public class ValidationException extends ApplicationException {
    
    public ValidationException(ErrorCode errorCode, Object... messageArgs) {
        super(errorCode.name(), errorCode.getMessageKey(), HttpStatus.BAD_REQUEST, messageArgs);
    }
    
    public ValidationException(String errorCode, String messageKey, Object... messageArgs) {
        super(errorCode, messageKey, HttpStatus.BAD_REQUEST, messageArgs);
    }
}
