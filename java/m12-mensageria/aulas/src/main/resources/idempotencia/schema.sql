-- Aula 12.05: consumidores idempotentes. Roda no PostgreSQL e no H2 (MODE=PostgreSQL).

CREATE TABLE conta (
    id     VARCHAR(40)   PRIMARY KEY,
    saldo  NUMERIC(12,2) NOT NULL
);

-- A "inbox": uma linha por mensagem já processada, por consumidor.
CREATE TABLE mensagem_processada (
    consumidor     VARCHAR(50)  NOT NULL,
    mensagem_id    VARCHAR(64)  NOT NULL,
    processada_em  TIMESTAMP    NOT NULL,
    PRIMARY KEY (consumidor, mensagem_id)
);

-- O efeito. A chave de negócio (um cashback por compra) também é única.
CREATE TABLE lancamento (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    conta_id   VARCHAR(40)   NOT NULL REFERENCES conta (id),
    compra_id  VARCHAR(40)   NOT NULL,
    valor      NUMERIC(12,2) NOT NULL,
    CONSTRAINT uk_lancamento_compra UNIQUE (compra_id)
);

-- Projeção de estado: guarda só o último nível e a versão que o produziu.
CREATE TABLE nivel_do_cliente (
    cliente_id  VARCHAR(40) PRIMARY KEY,
    nivel       VARCHAR(20) NOT NULL,
    versao      BIGINT      NOT NULL
);

-- Idempotency-Key da API HTTP: a chave vale por cliente, com o hash do corpo e a resposta guardada.
CREATE TABLE chave_de_idempotencia (
    cliente_id         VARCHAR(40)  NOT NULL,
    chave              VARCHAR(80)  NOT NULL,
    hash_da_requisicao CHAR(64)     NOT NULL,
    status_http        INTEGER,
    corpo_da_resposta  VARCHAR(2000),
    criada_em          TIMESTAMP    NOT NULL,
    PRIMARY KEY (cliente_id, chave)
);

CREATE TABLE transferencia (
    id          VARCHAR(40)   PRIMARY KEY,
    cliente_id  VARCHAR(40)   NOT NULL,
    destino     VARCHAR(40)   NOT NULL,
    valor       NUMERIC(12,2) NOT NULL
);
