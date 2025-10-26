package com.aloneinabyss.lovelace.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception class for all application-specific exceptions.
 * Provides a consistent structure with error codes, HTTP status, and localized messages.
 */
@Getter
public abstract class ApplicationException extends RuntimeException {
    
    /**
     * Machine-readable error code for frontend error handling.
     * Example: "USERNAME_TAKEN", "EMAIL_NOT_VERIFIED"
     */
    private final String errorCode;
    
    /**
     * HTTP status code to be returned in the response.
     */
    private final HttpStatus status;
    
    /**
     * Message key for i18n localization.
     * Can also be a direct message if not using i18n.
     */
    private final String messageKey;
    
    /**
     * Optional arguments for message formatting (e.g., {0}, {1}).
     */
    private final Object[] messageArgs;
    
    /**
     * Constructs an ApplicationException with error code, message key, status, and optional arguments.
     *
     * @param errorCode Machine-readable error code
     * @param messageKey i18n message key or direct message
     * @param status HTTP status code
     * @param messageArgs Optional arguments for message formatting
     */
    protected ApplicationException(String errorCode, String messageKey, HttpStatus status, Object... messageArgs) {
        super(messageKey);
        this.errorCode = errorCode;
        this.messageKey = messageKey;
        this.status = status;
        this.messageArgs = messageArgs;
    }
    
    /**
     * Constructs an ApplicationException with a cause.
     *
     * @param errorCode Machine-readable error code
     * @param messageKey i18n message key or direct message
     * @param status HTTP status code
     * @param cause The underlying cause
     * @param messageArgs Optional arguments for message formatting
     */
    protected ApplicationException(String errorCode, String messageKey, HttpStatus status, Throwable cause, Object... messageArgs) {
        super(messageKey, cause);
        this.errorCode = errorCode;
        this.messageKey = messageKey;
        this.status = status;
        this.messageArgs = messageArgs;
    }
}
