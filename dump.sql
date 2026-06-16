--
-- PostgreSQL database dump
--

-- Dumped from database version 16.14 (Debian 16.14-1.pgdg12+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg12+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

ALTER TABLE IF EXISTS ONLY public.contatos DROP CONSTRAINT IF EXISTS fk_contatos_usuario;
DROP INDEX IF EXISTS public.idx_usuarios_email_active;
DROP INDEX IF EXISTS public.idx_contatos_usuario_nome_active;
DROP INDEX IF EXISTS public.idx_contatos_usuario_id_active;
ALTER TABLE IF EXISTS ONLY public.usuarios DROP CONSTRAINT IF EXISTS usuarios_pkey;
ALTER TABLE IF EXISTS ONLY public.usuarios DROP CONSTRAINT IF EXISTS uq_usuarios_email;
ALTER TABLE IF EXISTS ONLY public.contatos DROP CONSTRAINT IF EXISTS contatos_pkey;
ALTER TABLE IF EXISTS public.usuarios ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.contatos ALTER COLUMN id DROP DEFAULT;
DROP SEQUENCE IF EXISTS public.usuarios_id_seq;
DROP TABLE IF EXISTS public.usuarios;
DROP SEQUENCE IF EXISTS public.contatos_id_seq;
DROP TABLE IF EXISTS public.contatos;
DROP EXTENSION IF EXISTS pgcrypto;
--
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: contatos; Type: TABLE; Schema: public; Owner: agenda
--

CREATE TABLE public.contatos (
    id integer NOT NULL,
    usuario_id integer NOT NULL,
    nome character varying(100) NOT NULL,
    telefone character varying(20) NOT NULL,
    email character varying(150) NOT NULL,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp without time zone
);


ALTER TABLE public.contatos OWNER TO agenda;

--
-- Name: contatos_id_seq; Type: SEQUENCE; Schema: public; Owner: agenda
--

CREATE SEQUENCE public.contatos_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.contatos_id_seq OWNER TO agenda;

--
-- Name: contatos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: agenda
--

ALTER SEQUENCE public.contatos_id_seq OWNED BY public.contatos.id;


--
-- Name: usuarios; Type: TABLE; Schema: public; Owner: agenda
--

CREATE TABLE public.usuarios (
    id integer NOT NULL,
    nome character varying(100) NOT NULL,
    email character varying(150) NOT NULL,
    senha_hash character varying(255) NOT NULL,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp without time zone
);


ALTER TABLE public.usuarios OWNER TO agenda;

--
-- Name: usuarios_id_seq; Type: SEQUENCE; Schema: public; Owner: agenda
--

CREATE SEQUENCE public.usuarios_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.usuarios_id_seq OWNER TO agenda;

--
-- Name: usuarios_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: agenda
--

ALTER SEQUENCE public.usuarios_id_seq OWNED BY public.usuarios.id;


--
-- Name: contatos id; Type: DEFAULT; Schema: public; Owner: agenda
--

ALTER TABLE ONLY public.contatos ALTER COLUMN id SET DEFAULT nextval('public.contatos_id_seq'::regclass);


--
-- Name: usuarios id; Type: DEFAULT; Schema: public; Owner: agenda
--

ALTER TABLE ONLY public.usuarios ALTER COLUMN id SET DEFAULT nextval('public.usuarios_id_seq'::regclass);


--
-- Data for Name: contatos; Type: TABLE DATA; Schema: public; Owner: agenda
--

COPY public.contatos (id, usuario_id, nome, telefone, email, criado_em, deleted_at) FROM stdin;
2	1	Carla Mendes	(62) 97777-3333	carla.mendes@email.com	2026-05-22 15:03:20.047862	\N
3	1	Diego Ferreira	(62) 96666-4444	diego.ferreira@email.com	2026-05-22 15:03:20.047862	\N
4	1	Elena Costa	(62) 95555-5555	elena.costa@email.com	2026-05-22 15:03:20.047862	\N
5	2	Felipe Almeida	(62) 94444-6666	felipe.almeida@email.com	2026-05-22 15:03:20.047862	\N
6	2	Gabriela Rocha	(62) 93333-7777	gabriela.rocha@email.com	2026-05-22 15:03:20.047862	\N
7	2	Henrique Oliveira	(62) 92222-8888	henrique.oliveira@email.com	2026-05-22 15:03:20.047862	\N
8	3	Isabel Martins	(62) 91111-9999	isabel.martins@email.com	2026-05-22 15:03:20.047862	\N
9	3	João Pedro Lima	(62) 90000-0000	joao.lima@email.com	2026-05-22 15:03:20.047862	\N
10	3	Karen Vieira	(62) 89999-1010	karen.vieira@email.com	2026-05-22 15:03:20.047862	\N
11	1	Teste Ciclo3	(62)91234-5678	teste@ciclo3.com	2026-05-22 21:39:52.785361	\N
1	1	Bruno Souza	(62) 98888-2222	bruno.souza@email.com	2026-05-22 15:03:20.047862	2026-05-22 21:39:52.80667
12	1	cleber	(62) 9 1112-2233	cleber@contato.com	2026-05-22 21:56:00.244285	\N
13	4	Jose Silva	62999990000	jose@silva.com	2026-06-13 14:27:22.201774	\N
\.


--
-- Data for Name: usuarios; Type: TABLE DATA; Schema: public; Owner: agenda
--

COPY public.usuarios (id, nome, email, senha_hash, criado_em, deleted_at) FROM stdin;
1	Igor Anjos	igor@agenda.local	$2a$10$bT3pRChJhyLgFKbtLm9L7OB8BqPo1KgLbZf.HlXGfIpuSbBavEexO	2026-05-22 15:03:19.912866	\N
2	Ana Lima	ana@agenda.local	$2a$10$sgwRK.vXMX41y5YpOQ8T1eA7l1LvWEAP5/IKjk9Zu0UVye2pLfyBC	2026-05-22 15:03:19.912866	\N
3	Carlos Rocha	carlos@agenda.local	$2a$10$DJyTAZpIZjziBqW3A8sBKOqBRRoQ8k8TMT.k9cXtemIJfaIedLEie	2026-05-22 15:03:19.912866	\N
4	Igor Teste	igor@teste.com	$2a$10$AHMo9zZpWFVljrI.RvSu0ODGSFy/rFmQHC4ATfylB.M2udKJtALHG	2026-06-13 14:26:48.206746	\N
\.


--
-- Name: contatos_id_seq; Type: SEQUENCE SET; Schema: public; Owner: agenda
--

SELECT pg_catalog.setval('public.contatos_id_seq', 13, true);


--
-- Name: usuarios_id_seq; Type: SEQUENCE SET; Schema: public; Owner: agenda
--

SELECT pg_catalog.setval('public.usuarios_id_seq', 4, true);


--
-- Name: contatos contatos_pkey; Type: CONSTRAINT; Schema: public; Owner: agenda
--

ALTER TABLE ONLY public.contatos
    ADD CONSTRAINT contatos_pkey PRIMARY KEY (id);


--
-- Name: usuarios uq_usuarios_email; Type: CONSTRAINT; Schema: public; Owner: agenda
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT uq_usuarios_email UNIQUE (email);


--
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: agenda
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);


--
-- Name: idx_contatos_usuario_id_active; Type: INDEX; Schema: public; Owner: agenda
--

CREATE INDEX idx_contatos_usuario_id_active ON public.contatos USING btree (usuario_id) WHERE (deleted_at IS NULL);


--
-- Name: idx_contatos_usuario_nome_active; Type: INDEX; Schema: public; Owner: agenda
--

CREATE INDEX idx_contatos_usuario_nome_active ON public.contatos USING btree (usuario_id, nome) WHERE (deleted_at IS NULL);


--
-- Name: idx_usuarios_email_active; Type: INDEX; Schema: public; Owner: agenda
--

CREATE UNIQUE INDEX idx_usuarios_email_active ON public.usuarios USING btree (email) WHERE (deleted_at IS NULL);


--
-- Name: contatos fk_contatos_usuario; Type: FK CONSTRAINT; Schema: public; Owner: agenda
--

ALTER TABLE ONLY public.contatos
    ADD CONSTRAINT fk_contatos_usuario FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: pg_database_owner
--

GRANT ALL ON SCHEMA public TO agenda;


--
-- PostgreSQL database dump complete
--

