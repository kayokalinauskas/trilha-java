create table carteira_de_pontos (
    cliente_id varchar(40) primary key,
    saldo      bigint      not null check (saldo >= 0),
    versao     bigint      not null
);

create table recompensa (
    codigo    varchar(40)  primary key,
    descricao varchar(120) not null,
    custo     bigint       not null check (custo > 0),
    ativa     boolean      not null
);
