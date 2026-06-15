package com.agenda.repository;

import com.agenda.model.Contato;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class ContatoRepositoryIT {

    static {
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
        } catch (Exception ignored) {}
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withInitScript("init-test.sql");

    static DataSource dataSource;
    ContatoRepository repository;
    int usuarioId;

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
        repository = new ContatoRepository(dataSource);
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE public.contatos, public.usuarios RESTART IDENTITY CASCADE");
            ResultSet rs = stmt.executeQuery(
                    "INSERT INTO public.usuarios (nome, email, senha_hash) " +
                    "VALUES ('Teste', 'teste@test.com', 'hash') RETURNING id");
            rs.next();
            usuarioId = rs.getInt(1);
        }
    }

    @Test
    void save_contatoValido_retornaContatoComId() throws SQLException {
        Contato contato = new Contato(null, usuarioId, "Ana Silva", "(62)99999-0001", "ana@test.com");

        Contato salvo = repository.save(contato);

        assertNotNull(salvo.id());
        assertTrue(salvo.id() > 0);
        assertEquals("Ana Silva", salvo.nome());
        assertEquals(usuarioId, salvo.usuarioId());
    }

    @Test
    void findAll_comContatos_retornaOrdenadoPorNome() throws SQLException {
        repository.save(new Contato(null, usuarioId, "Zilda", "99999-0003", "z@test.com"));
        repository.save(new Contato(null, usuarioId, "Ana",   "99999-0001", "a@test.com"));
        repository.save(new Contato(null, usuarioId, "Bruno", "99999-0002", "b@test.com"));

        List<Contato> lista = repository.findAll(usuarioId);

        assertEquals(3, lista.size());
        assertEquals("Ana",   lista.get(0).nome());
        assertEquals("Bruno", lista.get(1).nome());
        assertEquals("Zilda", lista.get(2).nome());
    }

    @Test
    void findAll_semContatos_retornaListaVazia() throws SQLException {
        assertTrue(repository.findAll(usuarioId).isEmpty());
    }

    @Test
    void findByNome_termoParcial_retornaPrimeiroEncontrado() throws SQLException {
        repository.save(new Contato(null, usuarioId, "Ana Silva", "99999", "ana@test.com"));

        Optional<Contato> resultado = repository.findByNome(usuarioId, "ana");

        assertTrue(resultado.isPresent());
        assertEquals("Ana Silva", resultado.get().nome());
    }

    @Test
    void findByNome_nomeInexistente_retornaVazio() throws SQLException {
        assertTrue(repository.findByNome(usuarioId, "inexistente").isEmpty());
    }

    @Test
    void softDeleteById_contatoExistente_retornaTrue() throws SQLException {
        Contato salvo = repository.save(new Contato(null, usuarioId, "Ana", "99999", "ana@test.com"));

        boolean removido = repository.softDeleteById(salvo.id(), usuarioId);

        assertTrue(removido);
    }

    @Test
    void softDeleteById_idInexistente_retornaFalse() throws SQLException {
        assertFalse(repository.softDeleteById(9999, usuarioId));
    }

    @Test
    void softDeleteById_idDeOutroUsuario_retornaFalse() throws SQLException {
        Contato salvo = repository.save(new Contato(null, usuarioId, "Ana", "99999", "ana@test.com"));

        boolean removido = repository.softDeleteById(salvo.id(), usuarioId + 99);

        assertFalse(removido);
    }

    @Test
    void softDelete_contatoDeletadoNaoAparece_emFindAll() throws SQLException {
        Contato salvo = repository.save(new Contato(null, usuarioId, "Ana", "99999", "ana@test.com"));
        repository.softDeleteById(salvo.id(), usuarioId);

        assertTrue(repository.findAll(usuarioId).isEmpty());
    }

    @Test
    void softDelete_contatoDeletadoNaoAparece_emFindByNome() throws SQLException {
        Contato salvo = repository.save(new Contato(null, usuarioId, "Ana", "99999", "ana@test.com"));
        repository.softDeleteById(salvo.id(), usuarioId);

        assertTrue(repository.findByNome(usuarioId, "ana").isEmpty());
    }
}
