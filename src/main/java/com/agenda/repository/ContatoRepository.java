package com.agenda.repository;

import com.agenda.model.Contato;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Acesso a dados da tabela public.contatos. Garante invisibilidade de registros soft-deleted. */
public class ContatoRepository {

    private final DataSource dataSource;

    public ContatoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Persiste um novo contato e retorna o registro com id gerado pelo banco. */
    public Contato save(Contato contato) throws SQLException {
        String sql = """
                INSERT INTO public.contatos (usuario_id, nome, telefone, email)
                VALUES (?, ?, ?, ?)
                RETURNING id, usuario_id, nome, telefone, email
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, contato.usuarioId());
            ps.setString(2, contato.nome());
            ps.setString(3, contato.telefone());
            ps.setString(4, contato.email());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return mapear(rs);
            }
        }
    }

    /** Aplica soft delete pelo id do contato. Inclui usuario_id para segurança multi-tenant. */
    public boolean softDeleteById(int id, int usuarioId) throws SQLException {
        String sql = """
                UPDATE public.contatos
                SET deleted_at = CURRENT_TIMESTAMP
                WHERE id         = ?
                  AND usuario_id = ?
                  AND deleted_at IS NULL
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, usuarioId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Busca o primeiro contato ativo com nome contendo o termo (case-insensitive, LIMIT 1). */
    public Optional<Contato> findByNome(int usuarioId, String termo) throws SQLException {
        String sql = """
                SELECT id, usuario_id, nome, telefone, email
                FROM public.contatos
                WHERE usuario_id = ?
                  AND nome ILIKE ?
                  AND deleted_at IS NULL
                LIMIT 1
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setString(2, "%" + termo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    /** Retorna todos os contatos ativos do usuário, ordenados por nome. Nunca retorna null. */
    public List<Contato> findAll(int usuarioId) throws SQLException {
        String sql = """
                SELECT id, usuario_id, nome, telefone, email
                FROM public.contatos
                WHERE usuario_id = ?
                  AND deleted_at IS NULL
                ORDER BY nome ASC
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Contato> lista = new ArrayList<>();
                while (rs.next()) lista.add(mapear(rs));
                return lista.isEmpty() ? Collections.emptyList() : lista;
            }
        }
    }

    private Contato mapear(ResultSet rs) throws SQLException {
        return new Contato(
                rs.getInt("id"),
                rs.getInt("usuario_id"),
                rs.getString("nome"),
                rs.getString("telefone"),
                rs.getString("email")
        );
    }
}
