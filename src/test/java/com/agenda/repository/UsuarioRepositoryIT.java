package com.agenda.repository;

import com.agenda.exception.EmailJaCadastradoException;
import com.agenda.model.Usuario;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class UsuarioRepositoryIT {

    static {
        // Testcontainers 1.21.3 ignora DOCKER_API_VERSION e usa VERSION_1_32 como mínimo.
        // Docker Desktop 4.62+ (MinAPIVersion=1.44) rejeita /v1.32/info com HTTP 400.
        // Patch: altera o campo `minor` de VERSION_1_32 para 44 antes da inicialização do TC.
        try {
            Class<?> ravClass = Class.forName(
                    "org.testcontainers.shaded.com.github.dockerjava.core.RemoteApiVersion");
            java.lang.reflect.Field f132 = ravClass.getDeclaredField("VERSION_1_32");
            f132.setAccessible(true);
            Object version132 = f132.get(null);
            java.lang.reflect.Field minorField = ravClass.getDeclaredField("minor");
            minorField.setAccessible(true);
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            java.lang.reflect.Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Object unsafe = unsafeField.get(null);
            long offset = (long) unsafeClass.getMethod("objectFieldOffset", java.lang.reflect.Field.class)
                    .invoke(unsafe, minorField);
            unsafeClass.getMethod("putInt", Object.class, long.class, int.class)
                    .invoke(unsafe, version132, offset, 44);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withInitScript("init-test.sql");

    static DataSource dataSource;
    UsuarioRepository repository;

    @BeforeAll
    static void iniciarPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        config.setMaximumPoolSize(2);
        dataSource = new HikariDataSource(config);
    }

    @BeforeEach
    void setUp() throws SQLException {
        repository = new UsuarioRepository(dataSource);
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE public.contatos, public.usuarios RESTART IDENTITY CASCADE");
        }
    }

    @Test
    void save_dadosValidos_retornaUsuarioComId() throws SQLException {
        Usuario usuario = repository.save("Igor", "igor@test.com", "hash_bcrypt");

        assertNotNull(usuario);
        assertTrue(usuario.id() > 0);
        assertEquals("Igor", usuario.nome());
        assertEquals("igor@test.com", usuario.email());
    }

    @Test
    void save_emailDuplicado_lancaEmailJaCadastradoException() throws SQLException {
        repository.save("Igor", "igor@test.com", "hash1");

        assertThrows(EmailJaCadastradoException.class,
                () -> repository.save("Igor2", "igor@test.com", "hash2"));
    }

    @Test
    void findCredenciaisByEmail_usuarioExistente_retornaCredenciais() throws SQLException {
        repository.save("Igor", "igor@test.com", "$2a$10$fakehash");

        Optional<UsuarioRepository.UsuarioCredenciais> resultado =
                repository.findCredenciaisByEmail("igor@test.com");

        assertTrue(resultado.isPresent());
        assertEquals("Igor", resultado.get().nome());
        assertEquals("$2a$10$fakehash", resultado.get().senhaHash());
    }

    @Test
    void findCredenciaisByEmail_emailInexistente_retornaVazio() throws SQLException {
        Optional<UsuarioRepository.UsuarioCredenciais> resultado =
                repository.findCredenciaisByEmail("naoexiste@test.com");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void findCredenciaisByEmail_usuarioSoftDeleted_retornaVazio() throws SQLException {
        repository.save("Igor", "igor@test.com", "hash");
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE public.usuarios SET deleted_at = NOW() WHERE email = 'igor@test.com'");
        }

        Optional<UsuarioRepository.UsuarioCredenciais> resultado =
                repository.findCredenciaisByEmail("igor@test.com");

        assertTrue(resultado.isEmpty());
    }
}
