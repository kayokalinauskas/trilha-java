-- Marco 5: eventos com Transactional Outbox.

-- A outbox: cada linha é um evento gravado na mesma transação da mudança de negócio.
CREATE TABLE evento_saida (
    id                 UUID PRIMARY KEY,
    agregado_id        UUID          NOT NULL,           -- vira a chave da mensagem no Kafka
    tipo               VARCHAR(100)  NOT NULL,
    payload            TEXT          NOT NULL,           -- o JSON do evento
    criado_em          TIMESTAMP WITH TIME ZONE NOT NULL,
    publicado_em       TIMESTAMP WITH TIME ZONE,         -- NULL = pendente
    tentativas         INT           NOT NULL DEFAULT 0,
    proxima_tentativa  TIMESTAMP WITH TIME ZONE NOT NULL,
    ultimo_erro        VARCHAR(500)
);

CREATE INDEX ix_evento_saida_pendentes ON evento_saida (publicado_em, proxima_tentativa, criado_em);

-- Deduplicação dos consumidores: (consumidor, evento) processado no máximo uma vez.
CREATE TABLE evento_processado (
    consumidor     VARCHAR(50) NOT NULL,
    evento_id      UUID        NOT NULL,
    processado_em  TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (consumidor, evento_id)
);

CREATE TABLE notificacao (
    id                UUID PRIMARY KEY,
    usuario_id        UUID           NOT NULL REFERENCES usuario (id),
    tipo              VARCHAR(30)    NOT NULL,
    transferencia_id  UUID           NOT NULL REFERENCES transferencia (id),
    conta_id          UUID           NOT NULL REFERENCES conta (id),
    valor             NUMERIC(15, 2) NOT NULL,
    mensagem          VARCHAR(200)   NOT NULL,
    criada_em         TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX ix_notificacao_usuario ON notificacao (usuario_id, criada_em DESC);
