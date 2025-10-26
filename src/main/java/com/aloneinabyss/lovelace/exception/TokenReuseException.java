package com.aloneinabyss.lovelace.exception;

/**
 * Exception thrown when a blacklisted refresh token is reused.
 * This indicates a potential security breach or token theft.
 */
public class TokenReuseException extends RuntimeException {
    
    public TokenReuseException(String message) {
        super(message);
    }
    
    public TokenReuseException(String message, Throwable cause) {
        super(message, cause);
    }
}
