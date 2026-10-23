package dev.trilhajava.fintech.infra;

import dev.trilhajava.fintech.dominio.RepositorioDeTransferencias;
import dev.trilhajava.fintech.dominio.Transferencia;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class RepositorioDeTransferenciasJpa implements RepositorioDeTransferencias {

    private final TransferenciaJpa jpa;

    RepositorioDeTransferenciasJpa(TransferenciaJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(Transferencia transferencia) {
        jpa.saveAndFlush(transferencia);
    }

    @Override
    public Optional<Transferencia> buscar(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Transferencia> buscarPorChave(String chaveIdempotencia) {
        return jpa.findByChaveIdempotencia(chaveIdempotencia);
    }
}
