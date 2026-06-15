package com.agenda.service;

import com.agenda.exception.ContatoNaoEncontradoException;
import com.agenda.model.Contato;
import com.agenda.repository.ContatoRepository;

import java.sql.SQLException;
import java.util.List;

/** Regras de negócio para operações de contatos da agenda. */
public class AgendaTelefonica {

    private final ContatoRepository contatoRepository;

    public AgendaTelefonica(ContatoRepository contatoRepository) {
        this.contatoRepository = contatoRepository;
    }

    /** Valida os dados, cria o contato e persiste vinculado ao usuário informado. */
    public Contato adicionarContato(int usuarioId, String nome, String telefone, String email)
            throws SQLException {
        validarContato(nome, telefone, email);
        Contato novo = new Contato(null, usuarioId, nome, telefone, email);
        return contatoRepository.save(novo);
    }

    /** Remove (soft delete) o contato pelo id. Lança exceção se não encontrado ou de outro usuário. */
    public void removerContato(int usuarioId, int contatoId) throws SQLException {
        boolean removido = contatoRepository.softDeleteById(contatoId, usuarioId);
        if (!removido) throw new ContatoNaoEncontradoException(String.valueOf(contatoId));
    }

    /** Busca o primeiro contato ativo com nome parcialmente coincidente (case-insensitive). */
    public Contato buscarContato(int usuarioId, String nome) throws SQLException {
        if (nome.isBlank()) throw new IllegalArgumentException("Nome não pode ser vazio.");
        return contatoRepository.findByNome(usuarioId, nome)
                .orElseThrow(() -> new ContatoNaoEncontradoException(nome));
    }

    /** Retorna todos os contatos ativos do usuário, ordenados por nome. */
    public List<Contato> listarContatos(int usuarioId) throws SQLException {
        return contatoRepository.findAll(usuarioId);
    }

    private void validarContato(String nome, String telefone, String email) {
        if (nome.isBlank())       throw new IllegalArgumentException("Nome do contato não pode ser vazio.");
        if (telefone.isBlank())   throw new IllegalArgumentException("Telefone não pode ser vazio.");
        if (email.isBlank())      throw new IllegalArgumentException("Email do contato não pode ser vazio.");
        if (!email.contains("@")) throw new IllegalArgumentException("Email do contato inválido.");
    }
}
