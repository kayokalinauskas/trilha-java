-- Exercício 12.04 (PostgreSQL). Recriado a cada subida da aplicação de exemplo.
DROP TABLE IF EXISTS mensagem_processada, lancamento_contabil, pagamento, pedido;

CREATE TABLE pedido (
    id             VARCHAR(36)    PRIMARY KEY,
    valor          NUMERIC(12, 2) NOT NULL,
    status         VARCHAR(20)    NOT NULL DEFAULT 'AGUARDANDO',   -- AGUARDANDO | PAGO | ESTORNADO
    versao_evento  BIGINT         NOT NULL DEFAULT 0,              -- a maior sequência já aplicada
    atualizado_em  TIMESTAMPTZ
);

CREATE TABLE pagamento (
    id         BIGSERIAL      PRIMARY KEY,
    pedido_id  VARCHAR(36)    NOT NULL REFERENCES pedido (id),
    tipo       VARCHAR(20)    NOT NULL,
    valor      NUMERIC(12, 2) NOT NULL,
    transacao  VARCHAR(64)    NOT NULL
);

CREATE TABLE lancamento_contabil (
    id         BIGSERIAL      PRIMARY KEY,
    pedido_id  VARCHAR(36)    NOT NULL,
    valor      NUMERIC(12, 2) NOT NULL,
    historico  VARCHAR(100)   NOT NULL
);

-- A tabela de deduplicação: a chave primária é a garantia de "no máximo uma vez".
CREATE TABLE mensagem_processada (
    consumidor     VARCHAR(50)  NOT NULL,
    mensagem_id    VARCHAR(64)  NOT NULL,
    processada_em  TIMESTAMPTZ  NOT NULL,
    PRIMARY KEY (consumidor, mensagem_id)
);
