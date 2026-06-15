package com.agenda.exception;

/** Lançada quando nenhum contato ativo corresponde ao nome buscado. */
public class ContatoNaoEncontradoException extends RuntimeException {
    public ContatoNaoEncontradoException(String nome) {
        super("Contato não encontrado: " + nome);
    }
}
