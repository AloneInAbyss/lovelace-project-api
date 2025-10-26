package com.aloneinabyss.lovelace.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an internal server error occurs.
 * HTTP Status: 500 Internal Server Error
 * Examples: Email sending failure, database connection issues
 */
public class InternalServerException extends ApplicationException {
    
    public InternalServerException(ErrorCode errorCode, Object... messageArgs) {
        super(errorCode.name(), errorCode.getMessageKey(), HttpStatus.INTERNAL_SERVER_ERROR, messageArgs);
    }
    
    public InternalServerException(ErrorCode errorCode, Throwable cause, Object... messageArgs) {
        super(errorCode.name(), errorCode.getMessageKey(), HttpStatus.INTERNAL_SERVER_ERROR, cause, messageArgs);
    }
    
    public InternalServerException(String errorCode, String messageKey, Throwable cause, Object... messageArgs) {
        super(errorCode, messageKey, HttpStatus.INTERNAL_SERVER_ERROR, cause, messageArgs);
    }
}
