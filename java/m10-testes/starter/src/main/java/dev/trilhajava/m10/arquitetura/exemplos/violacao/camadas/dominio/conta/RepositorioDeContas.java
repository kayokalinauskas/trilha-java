package dev.trilhajava.m10.arquitetura.exemplos.violacao.camadas.dominio.conta;

import java.util.Optional;

/** Exemplo analisado pelo ArchUnit (violação: camadas). */
public interface RepositorioDeContas {

    Optional<Conta> buscar(long id);

    void salvar(Conta conta);
}
