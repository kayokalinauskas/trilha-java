-- Recursos exclusivos do PostgreSQL de propósito: JSONB, índice parcial e (no código) FOR UPDATE SKIP LOCKED.
-- Nada disso roda num H2 — é por isso que o teste precisa de um PostgreSQL de verdade.

CREATE TABLE tarefa (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tipo          VARCHAR(50)  NOT NULL,
    payload       JSONB        NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDENTE'
                  CHECK (status IN ('PENDENTE', 'EM_PROCESSAMENTO', 'CONCLUIDA', 'FALHOU')),
    tentativas    INT          NOT NULL DEFAULT 0,
    reservada_por VARCHAR(100),
    reservada_em  TIMESTAMPTZ,
    ultimo_erro   TEXT,
    criada_em     TIMESTAMPTZ  NOT NULL DEFAULT clock_timestamp()
);

-- Só as pendentes entram no índice: a fila fica rápida mesmo com milhões de tarefas concluídas.
CREATE INDEX ix_tarefa_pendente ON tarefa (criada_em, id) WHERE status = 'PENDENTE';
