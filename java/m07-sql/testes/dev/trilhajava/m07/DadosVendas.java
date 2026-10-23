package dev.trilhajava.m07;

/** Base de vendas usada pelos exercícios 07.02 e 07.03. */
final class DadosVendas {

    private DadosVendas() {
    }

    static final String SCRIPT = """

CREATE TABLE cliente (id INT PRIMARY KEY, nome VARCHAR(80) NOT NULL, cidade VARCHAR(60) NOT NULL);
CREATE TABLE produto (id INT PRIMARY KEY, nome VARCHAR(80) NOT NULL, categoria VARCHAR(40) NOT NULL, preco NUMERIC(10,2) NOT NULL);
CREATE TABLE pedido (id INT PRIMARY KEY, cliente_id INT NOT NULL REFERENCES cliente(id), data DATE NOT NULL, status VARCHAR(10) NOT NULL);
CREATE TABLE item_pedido (pedido_id INT NOT NULL REFERENCES pedido(id), produto_id INT NOT NULL REFERENCES produto(id),
                          quantidade INT NOT NULL, preco_unitario NUMERIC(10,2) NOT NULL, PRIMARY KEY (pedido_id, produto_id));

INSERT INTO cliente VALUES (1,'Ana','São Paulo'),(2,'Bruno','São Paulo'),(3,'Carla','Recife'),(4,'Davi','Recife'),(5,'Elis','Curitiba'),(6,'Fábio','Curitiba');
INSERT INTO produto VALUES (1,'Café 500g','Mercearia',25.00),(2,'Filtro','Mercearia',5.00),(3,'Caneca','Casa',40.00),
                           (4,'Moedor','Eletro',200.00),(5,'Cafeteira','Eletro',350.00),(6,'Bule','Casa',90.00);
INSERT INTO pedido VALUES
 (1,1,DATE '2026-01-05','PAGO'),(2,1,DATE '2026-01-20','PAGO'),(3,1,DATE '2026-02-02','PAGO'),
 (4,2,DATE '2026-01-07','PAGO'),(5,2,DATE '2026-02-10','CANCELADO'),
 (6,3,DATE '2026-02-15','PAGO'),(7,3,DATE '2026-03-01','PAGO'),(8,3,DATE '2026-03-03','PAGO'),
 (9,4,DATE '2026-03-03','PENDENTE'),
 (10,5,DATE '2026-03-05','PAGO');
INSERT INTO item_pedido VALUES
 (1,1,2,25.00),(1,2,1,5.00),
 (2,3,1,40.00),
 (3,1,4,22.50),
 (4,5,1,350.00),(4,2,2,5.00),
 (5,4,1,200.00),
 (6,4,1,180.00),(6,1,1,25.00),
 (7,3,2,40.00),
 (8,1,1,25.00),
 (9,5,1,350.00),
 (10,5,1,320.00),(10,3,1,40.00);
            """;
}
