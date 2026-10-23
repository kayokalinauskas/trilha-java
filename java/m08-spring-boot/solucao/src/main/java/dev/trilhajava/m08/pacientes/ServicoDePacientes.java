package dev.trilhajava.m08.pacientes;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

/** Regras de negócio. O controller só traduz HTTP ↔ chamadas deste serviço. */
@Service
public class ServicoDePacientes {

    private final RepositorioDePacientes repositorio;

    public ServicoDePacientes(RepositorioDePacientes repositorio) {
        this.repositorio = repositorio;
    }

    // synchronized: "verificar CPF + inserir" é check-then-act (módulo 06). Com banco de dados,
    // quem garante isso é uma constraint UNIQUE (módulo 07) — o synchronized não protege várias instâncias.
    public synchronized Paciente cadastrar(DadosPaciente dados) {
        if (repositorio.buscarPorCpf(dados.cpf()).isPresent()) {
            throw new CpfJaCadastradoException();
        }
        return repositorio.inserir(dados);
    }

    public Paciente buscar(long id) {
        return repositorio.buscar(id).orElseThrow(() -> new PacienteNaoEncontradoException(id));
    }

    public Pagina<Paciente> listar(String nome, int pagina, int tamanho) {
        String filtro = nome.toLowerCase(Locale.ROOT);
        List<Paciente> encontrados = repositorio.todos().stream()
                .filter(p -> p.nome().toLowerCase(Locale.ROOT).contains(filtro))
                .sorted(Comparator.comparing(Paciente::nome, String.CASE_INSENSITIVE_ORDER).thenComparing(Paciente::id))
                .toList();
        return Pagina.de(encontrados, pagina, tamanho);
    }

    public synchronized Paciente atualizar(long id, DadosPaciente dados) {
        buscar(id);
        repositorio.buscarPorCpf(dados.cpf())
                .filter(outro -> outro.id() != id)
                .ifPresent(outro -> {
                    throw new CpfJaCadastradoException();
                });
        var atualizado = new Paciente(id, dados.nome(), dados.cpf(), dados.email(), dados.nascimento());
        repositorio.atualizar(atualizado);
        return atualizado;
    }

    public void remover(long id) {
        if (!repositorio.remover(id)) {
            throw new PacienteNaoEncontradoException(id);
        }
    }
}
