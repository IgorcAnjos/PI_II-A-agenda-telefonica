package com.agenda.exception;

/** Lançada quando o email informado já pertence a uma conta ativa. */
public class EmailJaCadastradoException extends RuntimeException {
    public EmailJaCadastradoException(String email) {
        super("Este email já está cadastrado: " + email);
    }
}
