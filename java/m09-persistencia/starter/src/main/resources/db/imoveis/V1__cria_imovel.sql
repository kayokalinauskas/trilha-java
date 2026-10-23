CREATE TABLE imovel (
    id            BIGINT PRIMARY KEY,
    titulo        VARCHAR(160)   NOT NULL,
    tipo          VARCHAR(12)    NOT NULL CHECK (tipo IN ('APARTAMENTO', 'CASA', 'STUDIO')),
    cidade        VARCHAR(80)    NOT NULL,
    bairro        VARCHAR(80)    NOT NULL,
    preco         NUMERIC(12, 2) NOT NULL,
    area_m2       INT            NOT NULL,
    quartos       INT            NOT NULL,
    vagas_garagem INT            NOT NULL,
    publicado_em  DATE           NOT NULL
);
CREATE INDEX ix_imovel_cidade_preco ON imovel (cidade, preco);
