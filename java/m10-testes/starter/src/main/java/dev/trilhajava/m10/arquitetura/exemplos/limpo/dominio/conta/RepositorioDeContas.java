package dev.trilhajava.m10.arquitetura.exemplos.limpo.dominio.conta;

import java.util.Optional;

/** Exemplo analisado pelo ArchUnit (código limpo). */
public interface RepositorioDeContas {

    Optional<Conta> buscar(long id);

    void salvar(Conta conta);
}
