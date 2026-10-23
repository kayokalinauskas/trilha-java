package dev.trilhajava.m10.arquitetura.exemplos.violacao.transacao.dominio.conta;

import java.util.Optional;

/** Exemplo analisado pelo ArchUnit (violação: transacao). */
public interface RepositorioDeContas {

    Optional<Conta> buscar(long id);

    void salvar(Conta conta);
}
