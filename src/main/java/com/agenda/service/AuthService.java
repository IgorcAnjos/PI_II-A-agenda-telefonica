package com.agenda.service;

import com.agenda.exception.CredenciaisInvalidasException;
import com.agenda.exception.EmailJaCadastradoException;
import com.agenda.model.Usuario;
import com.agenda.repository.UsuarioRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

/** Gerencia autenticação: cadastro e login de usuários. */
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Valida campos, hasheia a senha e persiste o novo usuário.
     * Retorna o usuário criado pronto para iniciar sessão.
     */
    public Usuario cadastrar(String nome, String email, String senha) throws SQLException {
        validar(nome, email, senha);
        String hash = BCrypt.hashpw(senha, BCrypt.gensalt(10));
        return usuarioRepository.save(nome, email, hash);
    }

    /**
     * Verifica credenciais contra o hash armazenado.
     * Lança CredenciaisInvalidasException para qualquer falha — sem revelar o motivo.
     */
    public Usuario login(String email, String senha) throws SQLException {
        if (email.isBlank() || senha.isBlank()) {
            throw new CredenciaisInvalidasException("Email e senha obrigatórios.");
        }
        var opt = usuarioRepository.findCredenciaisByEmail(email);
        if (opt.isEmpty()) {
            throw new CredenciaisInvalidasException("Email ou senha inválidos.");
        }
        var creds = opt.get();
        if (!BCrypt.checkpw(senha, creds.senhaHash())) {
            throw new CredenciaisInvalidasException("Email ou senha inválidos.");
        }
        return new Usuario(creds.id(), creds.nome(), creds.email());
    }

    /** Valida presença e formato dos campos de cadastro. */
    private void validar(String nome, String email, String senha) {
        if (nome.isBlank())          throw new IllegalArgumentException("Nome não pode ser vazio.");
        if (email.isBlank())         throw new IllegalArgumentException("Email não pode ser vazio.");
        if (!email.contains("@"))    throw new IllegalArgumentException("Email inválido.");
        if (senha.length() < 6)      throw new IllegalArgumentException("Senha: mínimo 6 caracteres.");
    }
}
