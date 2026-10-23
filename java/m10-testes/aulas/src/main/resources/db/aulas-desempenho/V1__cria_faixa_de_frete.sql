CREATE TABLE faixa_de_frete (
    cep_inicial CHAR(8)       NOT NULL,
    cep_final   CHAR(8)       NOT NULL,
    regiao      VARCHAR(40)   NOT NULL,
    valor_base  DECIMAL(10,2) NOT NULL
);

INSERT INTO faixa_de_frete (cep_inicial, cep_final, regiao, valor_base) VALUES
    ('01000000', '05999999', 'Capital SP',  12.90),
    ('06000000', '19999999', 'Interior SP', 18.50),
    ('20000000', '79999999', 'Demais',      34.90),
    ('80000000', '99999999', 'Sul',         27.40);
