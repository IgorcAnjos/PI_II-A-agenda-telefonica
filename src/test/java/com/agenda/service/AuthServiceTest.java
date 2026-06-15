package com.agenda.service;

import com.agenda.exception.CredenciaisInvalidasException;
import com.agenda.exception.EmailJaCadastradoException;
import com.agenda.model.Usuario;
import com.agenda.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioRepository);
    }

    @Test
    void cadastrar_dadosValidos_retornaUsuario() throws SQLException {
        when(usuarioRepository.save(eq("Igor"), eq("igor@email.com"), anyString()))
                .thenReturn(new Usuario(1, "Igor", "igor@email.com"));

        Usuario resultado = authService.cadastrar("Igor", "igor@email.com", "senha123");

        assertEquals(1, resultado.id());
        assertEquals("Igor", resultado.nome());
        verify(usuarioRepository).save(eq("Igor"), eq("igor@email.com"), anyString());
    }

    @Test
    void cadastrar_nomeVazio_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.cadastrar("", "igor@email.com", "senha123"));
    }

    @Test
    void cadastrar_emailVazio_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.cadastrar("Igor", "", "senha123"));
    }

    @Test
    void cadastrar_emailSemArroba_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.cadastrar("Igor", "emailsemarroba", "senha123"));
    }

    @Test
    void cadastrar_senhaCurta_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.cadastrar("Igor", "igor@email.com", "123"));
    }

    @Test
    void cadastrar_emailDuplicado_propagaEmailJaCadastradoException() throws SQLException {
        when(usuarioRepository.save(any(), any(), any()))
                .thenThrow(new EmailJaCadastradoException("igor@email.com"));

        assertThrows(EmailJaCadastradoException.class,
                () -> authService.cadastrar("Igor", "igor@email.com", "senha123"));
    }

    @Test
    void login_credenciaisValidas_retornaUsuario() throws SQLException {
        String hash = BCrypt.hashpw("senha123", BCrypt.gensalt(10));
        var creds = new UsuarioRepository.UsuarioCredenciais(1, "Igor", "igor@email.com", hash);
        when(usuarioRepository.findCredenciaisByEmail("igor@email.com"))
                .thenReturn(Optional.of(creds));

        Usuario resultado = authService.login("igor@email.com", "senha123");

        assertEquals("Igor", resultado.nome());
    }

    @Test
    void login_emailNaoEncontrado_lancaCredenciaisInvalidasException() throws SQLException {
        when(usuarioRepository.findCredenciaisByEmail(any()))
                .thenReturn(Optional.empty());

        assertThrows(CredenciaisInvalidasException.class,
                () -> authService.login("naoexiste@email.com", "senha123"));
    }

    @Test
    void login_senhaErrada_lancaCredenciaisInvalidasException() throws SQLException {
        String hash = BCrypt.hashpw("senhaCorreta", BCrypt.gensalt(10));
        var creds = new UsuarioRepository.UsuarioCredenciais(1, "Igor", "igor@email.com", hash);
        when(usuarioRepository.findCredenciaisByEmail("igor@email.com"))
                .thenReturn(Optional.of(creds));

        assertThrows(CredenciaisInvalidasException.class,
                () -> authService.login("igor@email.com", "senhaErrada"));
    }

    @Test
    void login_emailVazio_lancaCredenciaisInvalidasException() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> authService.login("", "senha123"));
    }

    @Test
    void login_senhaVazia_lancaCredenciaisInvalidasException() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> authService.login("igor@email.com", ""));
    }
}
