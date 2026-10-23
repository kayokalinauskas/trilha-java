package dev.trilhajava.m08.pacientes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

/**
 * Armazenamento em memória (no módulo 09 vira JPA + PostgreSQL).
 * O bean é um singleton compartilhado por todas as requisições: precisa ser thread-safe.
 */
@Repository
public class RepositorioDePacientes {

    private final Map<Long, Paciente> pacientes = new ConcurrentHashMap<>();
    private final AtomicLong sequencia = new AtomicLong();

    public Paciente inserir(DadosPaciente dados) {
        var paciente = new Paciente(sequencia.incrementAndGet(), dados.nome(), dados.cpf(), dados.email(), dados.nascimento());
        pacientes.put(paciente.id(), paciente);
        return paciente;
    }

    public void atualizar(Paciente paciente) {
        pacientes.put(paciente.id(), paciente);
    }

    public Optional<Paciente> buscar(long id) {
        return Optional.ofNullable(pacientes.get(id));
    }

    public Optional<Paciente> buscarPorCpf(String cpf) {
        return pacientes.values().stream().filter(p -> p.cpf().equals(cpf)).findFirst();
    }

    public List<Paciente> todos() {
        return new ArrayList<>(pacientes.values());
    }

    public boolean remover(long id) {
        return pacientes.remove(id) != null;
    }
}
