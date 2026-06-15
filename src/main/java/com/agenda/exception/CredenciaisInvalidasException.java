package com.agenda.exception;

/** Lançada quando email ou senha não correspondem a nenhuma conta ativa. */
public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException(String message) {
        super(message);
    }
}
