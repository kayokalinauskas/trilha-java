package dev.trilhajava.m10.arquitetura.exemplos.violacao.dominio.dominio.conta;

import java.util.Optional;

/** Exemplo analisado pelo ArchUnit (violação: dominio). */
public interface RepositorioDeContas {

    Optional<Conta> buscar(long id);

    void salvar(Conta conta);
}
