package com.agenda.model;

/** Representa um usuário autenticado. Nunca carrega senha_hash. */
public record Usuario(int id, String nome, String email) {}
