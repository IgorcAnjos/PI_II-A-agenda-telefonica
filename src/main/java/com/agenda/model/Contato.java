package com.agenda.model;

/**
 * Representa um contato da agenda.
 * id é Integer (nullable) pois é null antes do INSERT ser executado.
 */
public record Contato(Integer id, int usuarioId, String nome, String telefone, String email) {}
