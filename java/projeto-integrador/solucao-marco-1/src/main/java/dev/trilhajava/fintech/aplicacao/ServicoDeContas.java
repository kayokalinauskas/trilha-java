package dev.trilhajava.fintech.aplicacao;

import dev.trilhajava.fintech.dominio.Conta;
import dev.trilhajava.fintech.dominio.ContaDuplicadaException;
import dev.trilhajava.fintech.dominio.ContaNaoEncontradaException;
import dev.trilhajava.fintech.dominio.Cpf;
import dev.trilhajava.fintech.dominio.RepositorioDeContas;
import dev.trilhajava.fintech.dominio.TipoConta;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Casos de uso de contas. Não sabe nada de HTTP nem de como os dados são guardados. */
@Service
public class ServicoDeContas {

    private final RepositorioDeContas repositorio;
    private final Clock clock;

    public ServicoDeContas(RepositorioDeContas repositorio, Clock clock) {
        this.repositorio = repositorio;
        this.clock = clock;
    }

    /** synchronized: "verificar duplicidade + salvar" não pode ser intercalado (no marco 2, UNIQUE no banco). */
    public synchronized Conta abrir(String titular, Cpf cpf, TipoConta tipo) {
        if (repositorio.existe(cpf, tipo)) {
            throw new ContaDuplicadaException(tipo);
        }
        var conta = new Conta(UUID.randomUUID(), titular.strip(), cpf, tipo, clock.instant());
        repositorio.salvar(conta);
        return conta;
    }

    public Conta buscar(UUID id) {
        return repositorio.buscar(id).orElseThrow(() -> new ContaNaoEncontradaException(id));
    }

    public Pagina<Conta> listar(Cpf cpf, int pagina, int tamanho) {
        return new Pagina<>(repositorio.listar(cpf, pagina, tamanho), pagina, tamanho, repositorio.contar(cpf));
    }

    public Conta depositar(UUID id, BigDecimal valor) {
        Conta conta = buscar(id);
        conta.depositar(valor, clock.instant());
        repositorio.salvar(conta);
        return conta;
    }

    public Conta sacar(UUID id, BigDecimal valor) {
        Conta conta = buscar(id);
        conta.sacar(valor, clock.instant());
        repositorio.salvar(conta);
        return conta;
    }
}
