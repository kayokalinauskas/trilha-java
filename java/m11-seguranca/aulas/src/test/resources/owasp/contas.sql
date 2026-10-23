CREATE TABLE conta (
    id      BIGINT PRIMARY KEY,
    titular VARCHAR(100)   NOT NULL,
    agencia VARCHAR(4)     NOT NULL,
    saldo   NUMERIC(15, 2) NOT NULL
);
INSERT INTO conta VALUES (1, 'Ana Souza',      '0001',  1500.00);
INSERT INTO conta VALUES (2, 'Bruno Lima',     '0001',   320.50);
INSERT INTO conta VALUES (3, 'Carla Mendes',   '0002', 98000.00);
INSERT INTO conta VALUES (4, 'Diego Ferreira', '0002',    12.00);
