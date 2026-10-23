CREATE TABLE cliente (
    id   BIGINT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL
);

CREATE TABLE produto (
    id    BIGINT PRIMARY KEY,
    nome  VARCHAR(120)  NOT NULL,
    preco NUMERIC(10, 2) NOT NULL
);

CREATE TABLE pedido (
    id         BIGINT PRIMARY KEY,
    cliente_id BIGINT      NOT NULL REFERENCES cliente (id),
    criado_em  TIMESTAMP WITH TIME ZONE NOT NULL,
    status     VARCHAR(10) NOT NULL CHECK (status IN ('PAGO', 'CANCELADO'))
);
CREATE INDEX ix_pedido_cliente_data ON pedido (cliente_id, criado_em);

CREATE TABLE item_pedido (
    id             BIGINT PRIMARY KEY,
    pedido_id      BIGINT         NOT NULL REFERENCES pedido (id),
    produto_id     BIGINT         NOT NULL REFERENCES produto (id),
    quantidade     INT            NOT NULL CHECK (quantidade > 0),
    preco_unitario NUMERIC(10, 2) NOT NULL
);
CREATE INDEX ix_item_pedido ON item_pedido (pedido_id);
