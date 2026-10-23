-- Aula 10.05 (Testcontainers). Escrita no subconjunto que o H2 também entende, porque a aula
-- roda esta mesma migration no H2 para comparar. Num projeto só com PostgreSQL, você usaria
-- TIMESTAMPTZ e now(): o H2 (modo PostgreSQL) não reconhece o tipo TIMESTAMPTZ.

CREATE TABLE item_carrinho (
    carrinho_id   UUID                     NOT NULL,
    sku           VARCHAR(30)              NOT NULL,
    quantidade    INT                      NOT NULL CHECK (quantidade > 0),
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (carrinho_id, sku)
);
