CREATE TABLE transferencia (
    id                 UUID PRIMARY KEY,
    origem_id          UUID           NOT NULL REFERENCES conta (id),
    destino_id         UUID           NOT NULL REFERENCES conta (id),
    valor              NUMERIC(15, 2) NOT NULL CHECK (valor > 0),
    criada_em          TIMESTAMP WITH TIME ZONE NOT NULL,
    -- Idempotência: a mesma chave nunca gera duas transferências, nem com requisições simultâneas.
    chave_idempotencia VARCHAR(100)   NOT NULL,
    hash_requisicao    VARCHAR(64)    NOT NULL,
    CONSTRAINT uk_transferencia_chave UNIQUE (chave_idempotencia),
    CONSTRAINT ck_transferencia_contas CHECK (origem_id <> destino_id)
);

ALTER TABLE movimento ADD COLUMN transferencia_id UUID REFERENCES transferencia (id);
