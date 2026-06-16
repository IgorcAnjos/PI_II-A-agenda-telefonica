# Agenda Telefônica

Projeto Integrador II-A — PUC Goiás / ADS EaD  
Professor: Jose Ricardo Cosme Lérias Ribeiro

Aplicação console em Java 21 que implementa uma agenda de contatos com CRUD completo persistido em PostgreSQL. Cada usuário tem sua própria agenda isolada. A autenticação é feita por email e senha com hashing BCrypt — o banco armazena apenas o hash, nunca a senha em texto. Todo o acesso ao banco usa JDBC puro, sem ORM.

---

## Pré-requisitos

- Java 21
- Maven 3.9+
- PostgreSQL 16 rodando em `localhost:5432`

**Alternativa com Docker (sem instalar o PostgreSQL localmente):**

```bash
docker run -d \
  --name agenda-postgres \
  -e POSTGRES_USER=agenda \
  -e POSTGRES_PASSWORD=agenda123 \
  -e POSTGRES_DB=agenda_db \
  -p 5432:5432 \
  postgres:16
```

---

## Restaurar o banco de dados

O arquivo `database.sql` está na raiz deste projeto. Ele contém o schema completo e os dados de exemplo (3 usuários, 10 contatos).

**Com psql (PostgreSQL local):**

```bash
# Criar usuário e banco (pular se já existirem)
psql -U postgres -c "CREATE USER agenda WITH PASSWORD 'agenda123';"
psql -U postgres -c "CREATE DATABASE agenda_db OWNER agenda;"

# Restaurar
psql -U agenda -d agenda_db < database.sql
```

**Com Docker:**

```bash
docker exec -i agenda-postgres psql -U agenda -d agenda_db < database.sql
```

**Com pgAdmin:**

1. Abra o pgAdmin e conecte-se ao servidor PostgreSQL
   - Host: `localhost` · Porta: `5432` · Usuário: `postgres` (ou seu superusuário)
2. Crie o usuário `agenda`:
   - Clique com o botão direito em **Login/Group Roles → Create → Login/Group Role**
   - Name: `agenda` · Password: `agenda123` · aba **Privileges**: marque **Can login**
3. Crie o banco `agenda_db`:
   - Clique com o botão direito em **Databases → Create → Database**
   - Database: `agenda_db` · Owner: `agenda`
4. Execute o script via Query Tool:
   - Clique com o botão direito em `agenda_db` → **Query Tool**
   - No Query Tool: **File → Open** → selecione o arquivo `database.sql`
   - Clique em **Execute / Refresh (F5)** para rodar o script
5. Verifique: em **Schemas → public → Tables** devem aparecer `usuarios` e `contatos`

---

## Configuração da conexão

A conexão está hardcoded em `src/main/java/com/agenda/config/DatabaseConfig.java`:

```
Host:     localhost:5432
Banco:    agenda_db
Usuário:  agenda
Senha:    agenda123
```

Para usar credenciais diferentes, edite essa classe antes de buildar.

---

## Build

```bash
mvn clean package
```

Gera o fat JAR em `target/agenda.jar` com todas as dependências incluídas.

---

## Executar

```bash
java -jar target/agenda.jar
```

---

## Usuários e dados de exemplo

Os seguintes usuários já estão incluídos no `database.sql`:

| Nome         | Email               | Senha     | Contatos |
|--------------|---------------------|-----------|----------|
| Igor Anjos   | igor@agenda.local   | igor123   | 4        |
| Ana Lima     | ana@agenda.local    | ana123    | 3        |
| Carlos Rocha | carlos@agenda.local | carlos123 | 3        |

**Menus da aplicação:**

```
── Acesso ──────────────────
[1] Login
[2] Criar nova conta
[3] Sair

── Agenda (pós-login) ──────
[1] Adicionar contato
[2] Remover contato
[3] Buscar contato
[4] Listar todos os contatos
[5] Sair (logout)
```

---

## Testes

```bash
# Unitários — não requer Docker, execução rápida
mvn test

# Unitários + integração — requer Docker (Testcontainers sobe um PostgreSQL temporário)
mvn verify
```

| Tipo       | Classe                  | O que cobre                                    |
|------------|-------------------------|------------------------------------------------|
| Unitário   | `AuthServiceTest`       | BCrypt, validação de campos, login e cadastro  |
| Unitário   | `AgendaTelefonicaTest`  | CRUD de contatos com mock do repository        |
| Integração | `UsuarioRepositoryIT`   | Operações JDBC com PostgreSQL real             |
| Integração | `ContatoRepositoryIT`   | Operações JDBC com PostgreSQL real             |

---

## Estrutura de dados

### Tabela `public.usuarios`

```
id         SERIAL       PRIMARY KEY
nome       VARCHAR(100) NOT NULL
email      VARCHAR(150) NOT NULL       -- único entre registros ativos
senha_hash VARCHAR(255) NOT NULL       -- hash BCrypt gerado pela aplicação
criado_em  TIMESTAMP    DEFAULT NOW()
deleted_at TIMESTAMP    NULL           -- NULL = ativo | preenchido = removido
```

### Tabela `public.contatos`

```
id         SERIAL       PRIMARY KEY
usuario_id INTEGER      NOT NULL  ──FK──> usuarios.id
nome       VARCHAR(100) NOT NULL
telefone   VARCHAR(20)  NOT NULL
email      VARCHAR(150) NOT NULL
criado_em  TIMESTAMP    DEFAULT NOW()
deleted_at TIMESTAMP    NULL           -- NULL = ativo | preenchido = removido
```

### Diagrama ER

```
usuarios (1) ────────────< contatos (N)
   id  <──────── usuario_id
```

### Índices

Três índices parciais (`WHERE deleted_at IS NULL`) garantem que buscas e a restrição de unicidade de email operam apenas sobre registros ativos:

- `idx_usuarios_email_active` — unicidade de email por usuário ativo
- `idx_contatos_usuario_id_active` — busca de contatos por dono
- `idx_contatos_usuario_nome_active` — busca de contato por nome dentro do dono

---

## Arquitetura

### Camadas

```
AgendaTeste.java              ← entrada / menus (Scanner + System.out)
    ├── AuthService           ← login, cadastro, BCrypt
    └── AgendaTelefonica      ← adicionarContato, removerContato, buscarContato, listarContatos
             ├── UsuarioRepository    ← JDBC: INSERT, SELECT
             └── ContatoRepository    ← JDBC: INSERT, UPDATE (soft delete), SELECT
                          ↓
                    HikariCP (connection pool)
                          ↓
                   PostgreSQL 16 — schema public
```

### Pacotes Java

```
com.agenda
├── model/
│   ├── Usuario.java           record(id, nome, email)
│   └── Contato.java           record(id, usuarioId, nome, telefone, email)
├── repository/
│   ├── UsuarioRepository.java
│   └── ContatoRepository.java
├── service/
│   ├── AuthService.java
│   └── AgendaTelefonica.java
├── config/
│   └── DatabaseConfig.java    HikariCP — método estático build()
├── exception/
│   ├── ContatoNaoEncontradoException.java
│   ├── CredenciaisInvalidasException.java
│   └── EmailJaCadastradoException.java
└── AgendaTeste.java           main()
```

---

## Decisões técnicas

| Decisão | Detalhe |
|---|---|
| JDBC puro | Sem Hibernate, JPA ou qualquer ORM — exigência da avaliação |
| BCrypt cost 10 | Hash computacionalmente caro, resistente a força bruta |
| Soft delete | Contatos removidos ficam no banco com `deleted_at` preenchido — nunca `DELETE` físico |
| `UsuarioCredenciais` | Record interno ao `UsuarioRepository`: `senha_hash` nunca escapa para models ou services |
| Mensagem genérica de login | "Email ou senha inválidos" — não revela se o email está cadastrado |
| Shutdown hook | Fecha o pool HikariCP ao encerrar a JVM (`Runtime.getRuntime().addShutdownHook`) |
| `search_path=public` | Configurado no HikariCP; conexão enxerga apenas o schema `public` |

---

## Sobre o database.sql

O `database.sql` é uma cópia do script `sql/init.sql` do workspace — o mesmo script
que o Docker Compose executa automaticamente para inicializar o container do
PostgreSQL. Ele cria o schema do zero (extensão `pgcrypto`, tabelas `usuarios` e
`contatos`, índices) e popula com os 3 usuários e 10 contatos de exemplo.

Se o `sql/init.sql` for alterado, copie o conteúdo atualizado para `database.sql`
para manter os dois sincronizados.
