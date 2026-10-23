package dev.trilhajava.m11.aulas.senhas;

import java.util.List;
import java.util.Optional;

/** Onde as contas ficam guardadas. Em produção, uma tabela; nos exemplos, um {@code Map}. */
public interface RepositorioDeContas {

    Optional<Conta> buscar(String email);

    /** Grava a conta, substituindo a anterior de mesmo e-mail. */
    void salvar(Conta conta);

    List<Conta> todas();
}
