package dev.trilhajava.fintech.infra;

import dev.trilhajava.fintech.dominio.Movimento;
import dev.trilhajava.fintech.dominio.RepositorioDeMovimentos;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class RepositorioDeMovimentosJpa implements RepositorioDeMovimentos {

    private final MovimentoJpa jpa;

    RepositorioDeMovimentosJpa(MovimentoJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public void registrar(Movimento movimento) {
        jpa.save(movimento);
    }

    @Override
    public List<Movimento> extrato(UUID contaId) {
        return jpa.extrato(contaId);
    }
}
