CREATE TABLE IF NOT EXISTS public.usuarios (
    id         SERIAL       PRIMARY KEY,
    nome       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    criado_em  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP    NULL DEFAULT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_usuarios_email_active
    ON public.usuarios (email) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS public.contatos (
    id         SERIAL       PRIMARY KEY,
    usuario_id INTEGER      NOT NULL REFERENCES public.usuarios(id),
    nome       VARCHAR(100) NOT NULL,
    telefone   VARCHAR(20)  NOT NULL,
    email      VARCHAR(150) NOT NULL,
    criado_em  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP    NULL DEFAULT NULL
);
