package com.agenda;

import com.agenda.config.DatabaseConfig;
import com.agenda.exception.ContatoNaoEncontradoException;
import com.agenda.exception.CredenciaisInvalidasException;
import com.agenda.exception.EmailJaCadastradoException;
import com.agenda.model.Contato;
import com.agenda.model.Usuario;
import com.agenda.repository.ContatoRepository;
import com.agenda.repository.UsuarioRepository;
import com.agenda.service.AgendaTelefonica;
import com.agenda.service.AuthService;
import com.zaxxer.hikari.HikariDataSource;

import java.util.List;
import java.util.Scanner;

/** Ponto de entrada da aplicação. Gerencia menus, I/O e estado de sessão. */
public class AgendaTeste {

    private static HikariDataSource dataSource;
    private static Scanner scanner;
    private static AuthService authService;
    private static AgendaTelefonica agenda;
    private static Usuario usuarioLogado;

    /** Inicializa infraestrutura e inicia o loop principal de menus. */
    public static void main(String[] args) {
        try {
            dataSource = DatabaseConfig.build();
        } catch (Exception e) {
            System.out.println("[ERRO] Não foi possível conectar ao banco de dados.");
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }
        }));

        scanner = new Scanner(System.in);

        UsuarioRepository usuarioRepo = new UsuarioRepository(dataSource);
        ContatoRepository contatoRepo  = new ContatoRepository(dataSource);
        authService = new AuthService(usuarioRepo);
        agenda      = new AgendaTelefonica(contatoRepo);

        exibirMenuAcesso();
    }

    /** Loop do menu de acesso (sem usuário logado). */
    private static void exibirMenuAcesso() {
        while (true) {
            System.out.println("\n========================================");
            System.out.println("       AGENDA TELEFÔNICA");
            System.out.println("========================================");
            System.out.println("[1] Login");
            System.out.println("[2] Criar nova conta");
            System.out.println("[3] Sair");
            System.out.println("----------------------------------------");
            System.out.print("Opção: ");

            String entrada = scanner.nextLine().trim();
            switch (entrada) {
                case "1" -> processarOpcaoAcesso(1);
                case "2" -> processarOpcaoAcesso(2);
                case "3" -> {
                    System.out.println("Até logo!");
                    dataSource.close();
                    System.exit(0);
                }
                default  -> System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    /** Loop do menu da agenda (usuário autenticado). */
    private static void exibirMenuAgenda() {
        while (true) {
            System.out.println("\n========================================");
            System.out.println("  Bem-vindo, " + usuarioLogado.nome() + "!");
            System.out.println("========================================");
            System.out.println("[1] Adicionar contato");
            System.out.println("[2] Remover contato");
            System.out.println("[3] Buscar contato");
            System.out.println("[4] Listar todos os contatos");
            System.out.println("[5] Sair (logout)");
            System.out.println("----------------------------------------");
            System.out.print("Opção: ");

            String entrada = scanner.nextLine().trim();
            switch (entrada) {
                case "1" -> processarOpcaoAgenda(1);
                case "2" -> processarOpcaoAgenda(2);
                case "3" -> processarOpcaoAgenda(3);
                case "4" -> processarOpcaoAgenda(4);
                case "5" -> {
                    processarOpcaoAgenda(5);
                    return;
                }
                default  -> System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    /** Executa a opção selecionada no menu de acesso. */
    private static void processarOpcaoAcesso(int opcao) {
        try {
            switch (opcao) {
                case 1 -> {
                    String email = solicitarString("Email:");
                    String senha = solicitarString("Senha:");
                    usuarioLogado = authService.login(email, senha);
                    System.out.println("Bem-vindo, " + usuarioLogado.nome() + "!");
                    exibirMenuAgenda();
                }
                case 2 -> {
                    String nome  = solicitarString("Nome completo:");
                    String email = solicitarString("Email:");
                    String senha = solicitarString("Senha:");
                    usuarioLogado = authService.cadastrar(nome, email, senha);
                    System.out.println("Conta criada! Bem-vindo, " + usuarioLogado.nome() + "!");
                    exibirMenuAgenda();
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (CredenciaisInvalidasException e) {
            System.out.println(e.getMessage());
        } catch (EmailJaCadastradoException e) {
            System.out.println("Este email já está cadastrado.");
        } catch (Exception e) {
            System.out.println("Erro interno. Tente novamente.");
        }
    }

    /** Executa a opção selecionada no menu da agenda. Sempre verifica sessão primeiro. */
    private static void processarOpcaoAgenda(int opcao) {
        verificarSessao();
        try {
            switch (opcao) {
                case 1 -> {
                    String nome     = solicitarString("Nome do contato:");
                    String telefone = solicitarString("Telefone:");
                    String email    = solicitarString("Email do contato:");
                    Contato c = agenda.adicionarContato(usuarioLogado.id(), nome, telefone, email);
                    System.out.println("Contato adicionado!");
                    exibirContato(c);
                }
                case 2 -> {
                    List<Contato> contatos = agenda.listarContatos(usuarioLogado.id());
                    if (contatos.isEmpty()) {
                        System.out.println("Nenhum contato cadastrado.");
                    } else {
                        exibirListaContatos(contatos);
                        String entrada = solicitarString("Número do contato a remover:");
                        int numero;
                        try {
                            numero = Integer.parseInt(entrada);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Número inválido. Digite apenas o número da lista.");
                        }
                        if (numero < 1 || numero > contatos.size()) {
                            throw new IllegalArgumentException(
                                    "Número fora do intervalo. Digite entre 1 e " + contatos.size() + ".");
                        }
                        Contato c = contatos.get(numero - 1);
                        agenda.removerContato(usuarioLogado.id(), c.id());
                        System.out.println("Contato '" + c.nome() + "' removido com sucesso.");
                    }
                }
                case 3 -> {
                    String nome = solicitarString("Nome (ou parte do nome):");
                    Contato c = agenda.buscarContato(usuarioLogado.id(), nome);
                    exibirContato(c);
                }
                case 4 -> {
                    List<Contato> contatos = agenda.listarContatos(usuarioLogado.id());
                    if (contatos.isEmpty()) {
                        System.out.println("Nenhum contato cadastrado.");
                    } else {
                        exibirListaContatos(contatos);
                    }
                }
                case 5 -> {
                    String nome = usuarioLogado.nome();
                    usuarioLogado = null;
                    System.out.println("Até logo, " + nome + "! Sessão encerrada.");
                }
            }
        } catch (IllegalArgumentException | ContatoNaoEncontradoException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Erro interno. Tente novamente.");
        }
    }

    /** Lança IllegalStateException se não houver sessão ativa. */
    private static void verificarSessao() {
        if (usuarioLogado == null) {
            throw new IllegalStateException("Sessão inválida. Nenhum usuário autenticado.");
        }
    }

    /** Exibe o prompt e retorna a entrada do usuário sem espaços extras. */
    private static String solicitarString(String prompt) {
        System.out.print(prompt + " ");
        return scanner.nextLine().trim();
    }

    /** Exibe os dados de um contato formatados. */
    private static void exibirContato(Contato contato) {
        System.out.println("Contato encontrado:");
        System.out.println("  Nome:     " + contato.nome());
        System.out.println("  Telefone: " + contato.telefone());
        System.out.println("  Email:    " + contato.email());
    }

    /** Exibe a lista de contatos numerada e ordenada. */
    private static void exibirListaContatos(List<Contato> contatos) {
        System.out.println("Seus contatos (" + contatos.size() + " total):");
        System.out.printf(" %-3s %-25s %-20s %s%n", "#", "Nome", "Telefone", "Email");
        for (int i = 0; i < contatos.size(); i++) {
            Contato c = contatos.get(i);
            System.out.printf(" %-3d %-25s %-20s %s%n",
                    i + 1, c.nome(), c.telefone(), c.email());
        }
    }
}
