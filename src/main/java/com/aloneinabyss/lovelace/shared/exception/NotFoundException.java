package com.aloneinabyss.lovelace.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found.
 * HTTP Status: 404 Not Found
 * Examples: User not found, game not found
 */
public class NotFoundException extends ApplicationException {
    
    public NotFoundException(ErrorCode errorCode, Object... messageArgs) {
        super(errorCode.name(), errorCode.getMessageKey(), HttpStatus.NOT_FOUND, messageArgs);
    }
    
    public NotFoundException(String errorCode, String messageKey, Object... messageArgs) {
        super(errorCode, messageKey, HttpStatus.NOT_FOUND, messageArgs);
    }
}
