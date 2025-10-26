package com.aloneinabyss.lovelace.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication fails.
 * HTTP Status: 401 Unauthorized
 * Examples: Invalid credentials, token expired, token revoked
 */
public class AuthenticationException extends ApplicationException {
    
    public AuthenticationException(ErrorCode errorCode, Object... messageArgs) {
        super(errorCode.name(), errorCode.getMessageKey(), HttpStatus.UNAUTHORIZED, messageArgs);
    }
    
    public AuthenticationException(String errorCode, String messageKey, Object... messageArgs) {
        super(errorCode, messageKey, HttpStatus.UNAUTHORIZED, messageArgs);
    }
    
    public AuthenticationException(ErrorCode errorCode, Throwable cause, Object... messageArgs) {
        super(errorCode.name(), errorCode.getMessageKey(), HttpStatus.UNAUTHORIZED, cause, messageArgs);
    }
}
