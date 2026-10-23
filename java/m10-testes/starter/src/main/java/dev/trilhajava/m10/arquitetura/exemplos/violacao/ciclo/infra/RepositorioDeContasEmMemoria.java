package dev.trilhajava.m10.arquitetura.exemplos.violacao.ciclo.infra;

import dev.trilhajava.m10.arquitetura.exemplos.violacao.ciclo.dominio.conta.Conta;
import dev.trilhajava.m10.arquitetura.exemplos.violacao.ciclo.dominio.conta.RepositorioDeContas;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/** Exemplo analisado pelo ArchUnit (violação: ciclo). */
@Repository
public class RepositorioDeContasEmMemoria implements RepositorioDeContas {

    private final Map<Long, Conta> contas = new ConcurrentHashMap<>();

    @Override
    public Optional<Conta> buscar(long id) {
        return Optional.ofNullable(contas.get(id));
    }

    @Override
    public void salvar(Conta conta) {
        contas.put(conta.id(), conta);
    }
}
