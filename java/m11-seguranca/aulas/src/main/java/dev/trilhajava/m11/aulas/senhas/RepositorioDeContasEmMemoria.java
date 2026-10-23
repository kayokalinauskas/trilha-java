package dev.trilhajava.m11.aulas.senhas;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RepositorioDeContasEmMemoria implements RepositorioDeContas {

    private final Map<String, Conta> contas = new ConcurrentHashMap<>();

    @Override
    public Optional<Conta> buscar(String email) {
        return Optional.ofNullable(contas.get(email));
    }

    @Override
    public void salvar(Conta conta) {
        contas.put(conta.email(), conta);
    }

    @Override
    public List<Conta> todas() {
        return List.copyOf(contas.values());
    }
}
