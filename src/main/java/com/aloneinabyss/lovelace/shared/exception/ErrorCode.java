package com.aloneinabyss.lovelace.shared.exception;

/**
 * Centralized catalog of all error codes used in the application.
 * These codes are machine-readable and consistent across i18n translations.
 * Frontend applications should use these codes for programmatic error handling.
 */
public enum ErrorCode {
    
    // Authentication errors (401)
    INVALID_CREDENTIALS("auth.login.invalid.credentials"),
    EMAIL_NOT_VERIFIED("auth.login.email.not.verified"),
    TOKEN_EXPIRED("auth.token.expired"),
    TOKEN_INVALID("auth.token.invalid"),
    TOKEN_REVOKED("auth.refresh.token.revoked"),
    TOKEN_REUSED("auth.refresh.token.reused"),
    AUTHENTICATION_REQUIRED("auth.authentication.required"),
    
    // Authorization errors (403)
    FORBIDDEN("auth.forbidden"),
    
    // Validation errors (400)
    USERNAME_TAKEN("auth.register.username.taken"),
    EMAIL_TAKEN("auth.register.email.taken"),
    INVALID_PASSWORD("auth.password.invalid"),
    PASSWORD_CURRENT_INCORRECT("auth.password.current.incorrect"),
    PASSWORD_MUST_BE_DIFFERENT("auth.password.must.be.different"),
    INVALID_TOKEN("auth.email.token.invalid"),
    
    // Conflict errors (409)
    EMAIL_ALREADY_VERIFIED("auth.email.already.verified"),
    PASSWORD_RESET_PENDING("auth.password.reset.pending"),
    EMAIL_VERIFICATION_PENDING("auth.email.verification.pending"),
    
    // Not found errors (404)
    USER_NOT_FOUND("auth.user.not.found"),
    RESOURCE_NOT_FOUND("resource.not.found"),
    
    // Server errors (500)
    EMAIL_SEND_FAILED("email.send.failed"),
    INTERNAL_ERROR("error.internal");
    
    private final String messageKey;
    
    ErrorCode(String messageKey) {
        this.messageKey = messageKey;
    }
    
    public String getMessageKey() {
        return messageKey;
    }
}
