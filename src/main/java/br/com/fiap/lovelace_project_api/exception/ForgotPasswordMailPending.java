package br.com.fiap.lovelace_project_api.exception;

public class ForgotPasswordMailPending extends RuntimeException {
    public ForgotPasswordMailPending(String message) {
        super(message);
    }
}