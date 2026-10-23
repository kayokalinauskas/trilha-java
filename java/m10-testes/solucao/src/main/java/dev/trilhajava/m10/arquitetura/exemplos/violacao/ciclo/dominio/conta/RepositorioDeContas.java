package dev.trilhajava.m10.arquitetura.exemplos.violacao.ciclo.dominio.conta;

import java.util.Optional;

/** Exemplo analisado pelo ArchUnit (violação: ciclo). */
public interface RepositorioDeContas {

    Optional<Conta> buscar(long id);

    void salvar(Conta conta);
}
