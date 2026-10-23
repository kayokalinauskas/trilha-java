package dev.trilhajava.m11.rh;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import org.springframework.stereotype.Repository;

/** Repositório em memória, já com os seis funcionários de exemplo. */
@Repository
public class RepositorioDeFuncionarios {

    private final Map<Long, Funcionario> porId = new ConcurrentSkipListMap<>();

    public RepositorioDeFuncionarios() {
        salvar(new Funcionario(1, "rita", "Rita Andrade", "Pessoas", null, new BigDecimal("9800.00")));
        salvar(new Funcionario(2, "gustavo", "Gustavo Lima", "Engenharia", null, new BigDecimal("15000.00")));
        salvar(new Funcionario(3, "fernanda", "Fernanda Souza", "Engenharia", 2L, new BigDecimal("11000.00")));
        salvar(new Funcionario(4, "felipe", "Felipe Costa", "Engenharia", 2L, new BigDecimal("8500.00")));
        salvar(new Funcionario(5, "gabriela", "Gabriela Rocha", "Vendas", null, new BigDecimal("14000.00")));
        salvar(new Funcionario(6, "fabio", "Fábio Nunes", "Vendas", 5L, new BigDecimal("7000.00")));
    }

    public Optional<Funcionario> porId(long id) {
        return Optional.ofNullable(porId.get(id));
    }

    public Optional<Funcionario> porUsuario(String usuario) {
        return porId.values().stream().filter(f -> f.usuario().equals(usuario)).findFirst();
    }

    /** Todos, em ordem de id. */
    public List<Funcionario> todos() {
        return List.copyOf(porId.values());
    }

    public Funcionario salvar(Funcionario funcionario) {
        porId.put(funcionario.id(), funcionario);
        return funcionario;
    }
}
