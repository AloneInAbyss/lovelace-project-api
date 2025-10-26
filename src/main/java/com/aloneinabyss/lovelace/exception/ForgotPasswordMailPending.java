package com.aloneinabyss.lovelace.exception;

public class ForgotPasswordMailPending extends RuntimeException {
    public ForgotPasswordMailPending(String message) {
        super(message);
    }
}