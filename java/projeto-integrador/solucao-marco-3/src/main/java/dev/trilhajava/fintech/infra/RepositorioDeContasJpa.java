package dev.trilhajava.fintech.infra;

import dev.trilhajava.fintech.dominio.Conta;
import dev.trilhajava.fintech.dominio.Cpf;
import dev.trilhajava.fintech.dominio.RepositorioDeContas;
import dev.trilhajava.fintech.dominio.TipoConta;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/** O adaptador: implementa a porta do domínio usando o Spring Data. Trocou memória por JPA, e só isso. */
@Repository
class RepositorioDeContasJpa implements RepositorioDeContas {

    private static final Sort ORDEM = Sort.by("criadaEm", "id");

    private final ContaJpa jpa;

    RepositorioDeContasJpa(ContaJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(Conta conta) {
        jpa.saveAndFlush(conta); // flush imediato: violações de constraint aparecem aqui, não no commit
    }

    @Override
    public Optional<Conta> buscar(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Conta> buscarParaAtualizar(UUID id) {
        return jpa.buscarComLock(id);
    }

    @Override
    public boolean existe(Cpf cpf, TipoConta tipo) {
        return jpa.existsByCpfAndTipo(cpf, tipo);
    }

    @Override
    public List<Conta> listar(Cpf cpf, int pagina, int tamanho) {
        var pageable = PageRequest.of(pagina, tamanho, ORDEM);
        return (cpf == null ? jpa.findAll(pageable) : jpa.findByCpf(cpf, pageable)).getContent();
    }

    @Override
    public long contar(Cpf cpf) {
        return cpf == null ? jpa.count() : jpa.countByCpf(cpf);
    }
}
