package dev.trilhajava.fintech.infra;

import dev.trilhajava.fintech.dominio.Conta;
import dev.trilhajava.fintech.dominio.Cpf;
import dev.trilhajava.fintech.dominio.RepositorioDeContas;
import dev.trilhajava.fintech.dominio.TipoConta;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.springframework.stereotype.Repository;

/** Marco 1: tudo em memória (some ao reiniciar). O marco 2 troca esta classe por JPA. */
@Repository
public class RepositorioDeContasEmMemoria implements RepositorioDeContas {

    private final Map<UUID, Conta> contas = new ConcurrentHashMap<>();

    @Override
    public void salvar(Conta conta) {
        contas.put(conta.id(), conta);
    }

    @Override
    public Optional<Conta> buscar(UUID id) {
        return Optional.ofNullable(contas.get(id));
    }

    @Override
    public boolean existe(Cpf cpf, TipoConta tipo) {
        return contas.values().stream().anyMatch(c -> c.cpf().equals(cpf) && c.tipo() == tipo);
    }

    @Override
    public List<Conta> listar(Cpf cpf, int pagina, int tamanho) {
        return filtrar(cpf)
                .sorted(Comparator.comparing(Conta::criadaEm).thenComparing(Conta::id))
                .skip((long) pagina * tamanho)
                .limit(tamanho)
                .toList();
    }

    @Override
    public long contar(Cpf cpf) {
        return filtrar(cpf).count();
    }

    private Stream<Conta> filtrar(Cpf cpf) {
        return contas.values().stream().filter(c -> cpf == null || c.cpf().equals(cpf));
    }
}
