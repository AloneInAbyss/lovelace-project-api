package com.aloneinabyss.lovelace.auth.exception;

public class ForgotPasswordMailPending extends RuntimeException {
    public ForgotPasswordMailPending(String message) {
        super(message);
    }
}