-- Marco 4: usuários e dono da conta.

CREATE TABLE usuario (
    id         UUID PRIMARY KEY,
    nome       VARCHAR(120) NOT NULL,
    email      VARCHAR(254) NOT NULL,           -- sempre em minúsculas (normalizado na aplicação)
    senha_hash VARCHAR(255) NOT NULL,           -- {bcrypt}$2a$10$... — nunca a senha
    papel      VARCHAR(20)  NOT NULL CHECK (papel IN ('CLIENTE', 'ADMIN')),
    criado_em  TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_usuario_email UNIQUE (email)
);

-- Contas abertas antes do marco 4 ficam sem dono (NULL): só o ADMIN as enxerga.
ALTER TABLE conta ADD COLUMN dono_id UUID REFERENCES usuario (id);
CREATE INDEX ix_conta_dono ON conta (dono_id);
