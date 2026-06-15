package com.agenda.repository;

import com.agenda.exception.EmailJaCadastradoException;
import com.agenda.model.Usuario;
import org.postgresql.util.PSQLException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

/** Acesso a dados da tabela public.usuarios. */
public class UsuarioRepository {

    private final DataSource dataSource;

    public UsuarioRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Busca credenciais completas (incluindo hash) pelo email.
     * Retorna Optional.empty() se não encontrado ou soft-deleted.
     */
    public Optional<UsuarioCredenciais> findCredenciaisByEmail(String email) throws SQLException {
        String sql = """
                SELECT id, nome, email, senha_hash
                FROM public.usuarios
                WHERE email = ?
                  AND deleted_at IS NULL
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new UsuarioCredenciais(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("email"),
                            rs.getString("senha_hash")
                    ));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Insere um novo usuário e retorna o registro criado com id gerado pelo banco.
     * Lança EmailJaCadastradoException se o email já estiver em uso.
     */
    public Usuario save(String nome, String email, String senhaHash) throws SQLException {
        String sql = """
                INSERT INTO public.usuarios (nome, email, senha_hash)
                VALUES (?, ?, ?)
                RETURNING id, nome, email
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, email);
            ps.setString(3, senhaHash);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new Usuario(rs.getInt("id"), rs.getString("nome"), rs.getString("email"));
            }
        } catch (PSQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new EmailJaCadastradoException(email);
            }
            throw e;
        }
    }

    /** Credenciais internas — acessível por AuthService para verificação de hash. */
    public record UsuarioCredenciais(int id, String nome, String email, String senhaHash) {}
}
