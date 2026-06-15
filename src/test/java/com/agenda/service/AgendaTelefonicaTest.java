package com.agenda.service;

import com.agenda.exception.ContatoNaoEncontradoException;
import com.agenda.model.Contato;
import com.agenda.repository.ContatoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendaTelefonicaTest {

    @Mock
    private ContatoRepository contatoRepository;

    private AgendaTelefonica agenda;

    @BeforeEach
    void setUp() {
        agenda = new AgendaTelefonica(contatoRepository);
    }

    @Test
    void adicionarContato_dadosValidos_retornaContatoComId() throws SQLException {
        Contato esperado = new Contato(1, 10, "Ana", "(62)99999-0001", "ana@email.com");
        when(contatoRepository.save(any())).thenReturn(esperado);

        Contato resultado = agenda.adicionarContato(10, "Ana", "(62)99999-0001", "ana@email.com");

        assertEquals(1, resultado.id());
        assertEquals("Ana", resultado.nome());
        verify(contatoRepository).save(any());
    }

    @Test
    void adicionarContato_nomeVazio_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> agenda.adicionarContato(1, "", "telefone", "email@x.com"));
    }

    @Test
    void adicionarContato_telefoneVazio_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> agenda.adicionarContato(1, "Ana", "", "email@x.com"));
    }

    @Test
    void adicionarContato_emailInvalido_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> agenda.adicionarContato(1, "Ana", "99999", "emailsemarroba"));
    }

    @Test
    void removerContato_idEncontrado_executaSemExcecao() throws SQLException {
        when(contatoRepository.softDeleteById(1, 10)).thenReturn(true);

        assertDoesNotThrow(() -> agenda.removerContato(10, 1));
        verify(contatoRepository).softDeleteById(1, 10);
    }

    @Test
    void removerContato_idNaoEncontrado_lancaContatoNaoEncontradoException() throws SQLException {
        when(contatoRepository.softDeleteById(anyInt(), anyInt())).thenReturn(false);

        assertThrows(ContatoNaoEncontradoException.class,
                () -> agenda.removerContato(1, 999));
    }

    @Test
    void buscarContato_nomeEncontrado_retornaContato() throws SQLException {
        Contato esperado = new Contato(1, 1, "Ana", "99999", "ana@email.com");
        when(contatoRepository.findByNome(1, "ana")).thenReturn(Optional.of(esperado));

        Contato resultado = agenda.buscarContato(1, "ana");

        assertEquals("Ana", resultado.nome());
    }

    @Test
    void buscarContato_nomeNaoEncontrado_lancaContatoNaoEncontradoException() throws SQLException {
        when(contatoRepository.findByNome(anyInt(), anyString())).thenReturn(Optional.empty());

        assertThrows(ContatoNaoEncontradoException.class,
                () -> agenda.buscarContato(1, "Inexistente"));
    }

    @Test
    void buscarContato_nomeVazio_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> agenda.buscarContato(1, ""));
    }

    @Test
    void listarContatos_retornaListaOrdenada() throws SQLException {
        List<Contato> lista = List.of(
                new Contato(1, 1, "Ana",   "99999-0001", "ana@email.com"),
                new Contato(2, 1, "Bruno", "99999-0002", "bruno@email.com")
        );
        when(contatoRepository.findAll(1)).thenReturn(lista);

        List<Contato> resultado = agenda.listarContatos(1);

        assertEquals(2, resultado.size());
        assertEquals("Ana", resultado.get(0).nome());
    }

    @Test
    void listarContatos_semContatos_retornaListaVazia() throws SQLException {
        when(contatoRepository.findAll(anyInt())).thenReturn(List.of());

        List<Contato> resultado = agenda.listarContatos(1);

        assertTrue(resultado.isEmpty());
    }
}
