-- init.sql — Script de inicialização do banco agenda_db
-- Executado automaticamente pelo PostgreSQL na primeira inicialização do container.
-- Combina todas as migrations (V1→V4) e seeds (S1→S2) em ordem.
-- Fontes canônicas: docs/migrations/ e docs/seeds/

-- ============================================================
-- V1: Extensões
-- ============================================================
-- pgcrypto:  usado SOMENTE nos seeds (S1) para gerar hashes BCrypt de dev.
--            A aplicação Java NUNCA usa pgcrypto — toda auth é no AuthService.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- V2: Permissões no schema public
-- ============================================================
GRANT USAGE  ON SCHEMA public TO agenda;
GRANT CREATE ON SCHEMA public TO agenda;

-- ============================================================
-- V3: Tabela de usuários (schema public — negócio)
-- Soft delete: deleted_at NULL = ativo; preenchido = inativo.
-- O banco apenas armazena o hash como VARCHAR — não verifica senhas.
-- ============================================================
CREATE TABLE IF NOT EXISTS public.usuarios (
    id         SERIAL       PRIMARY KEY,
    nome       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,  -- hash BCrypt gerado pela aplicação
    criado_em  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP    NULL DEFAULT NULL,  -- NULL = ativo; preenchido = soft deleted

    CONSTRAINT uq_usuarios_email UNIQUE (email)
);

-- Índice parcial: cobre apenas usuários ativos
CREATE UNIQUE INDEX IF NOT EXISTS idx_usuarios_email_active
    ON public.usuarios (email)
    WHERE deleted_at IS NULL;

-- ============================================================
-- V4: Tabela de contatos vinculados a usuários (schema public)
-- Soft delete: deleted_at NULL = ativo; preenchido = removido.
-- Remoção via UPDATE SET deleted_at — nunca DELETE físico.
-- ============================================================
CREATE TABLE IF NOT EXISTS public.contatos (
    id         SERIAL       PRIMARY KEY,
    usuario_id INTEGER      NOT NULL,
    nome       VARCHAR(100) NOT NULL,
    telefone   VARCHAR(20)  NOT NULL,
    email      VARCHAR(150) NOT NULL,
    criado_em  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP    NULL DEFAULT NULL,  -- NULL = ativo; preenchido = soft deleted

    CONSTRAINT fk_contatos_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES public.usuarios(id)
);

-- Índices parciais: cobrem apenas registros ativos
CREATE INDEX IF NOT EXISTS idx_contatos_usuario_id_active
    ON public.contatos (usuario_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_contatos_usuario_nome_active
    ON public.contatos (usuario_id, nome)
    WHERE deleted_at IS NULL;

-- ============================================================
-- S1: Usuários iniciais (dados de desenvolvimento)
-- pgcrypto é usado AQUI APENAS para gerar hashes BCrypt compatíveis com jBCrypt.
-- A aplicação verifica senhas com BCrypt.checkpw() — o banco não participa.
-- Senhas: igor123 | ana123 | carlos123
-- ============================================================
INSERT INTO public.usuarios (nome, email, senha_hash) VALUES
    ('Igor Anjos',    'igor@agenda.local',   crypt('igor123',   gen_salt('bf', 10))),
    ('Ana Lima',      'ana@agenda.local',    crypt('ana123',    gen_salt('bf', 10))),
    ('Carlos Rocha',  'carlos@agenda.local', crypt('carlos123', gen_salt('bf', 10)));

-- ============================================================
-- S2: Contatos iniciais vinculados aos usuários
-- Igor(1)→4 contatos | Ana(2)→3 contatos | Carlos(3)→3 contatos
-- ============================================================
INSERT INTO public.contatos (usuario_id, nome, telefone, email) VALUES
    (1, 'Bruno Souza',        '(62) 98888-2222', 'bruno.souza@email.com'),
    (1, 'Carla Mendes',       '(62) 97777-3333', 'carla.mendes@email.com'),
    (1, 'Diego Ferreira',     '(62) 96666-4444', 'diego.ferreira@email.com'),
    (1, 'Elena Costa',        '(62) 95555-5555', 'elena.costa@email.com'),
    (2, 'Felipe Almeida',     '(62) 94444-6666', 'felipe.almeida@email.com'),
    (2, 'Gabriela Rocha',     '(62) 93333-7777', 'gabriela.rocha@email.com'),
    (2, 'Henrique Oliveira',  '(62) 92222-8888', 'henrique.oliveira@email.com'),
    (3, 'Isabel Martins',     '(62) 91111-9999', 'isabel.martins@email.com'),
    (3, 'João Pedro Lima',    '(62) 90000-0000', 'joao.lima@email.com'),
    (3, 'Karen Vieira',       '(62) 89999-1010', 'karen.vieira@email.com');
