package dev.trilhajava.fintech.dominio;

import java.util.Optional;
import java.util.UUID;

public interface RepositorioDeTransferencias {

    void salvar(Transferencia transferencia);

    Optional<Transferencia> buscar(UUID id);

    Optional<Transferencia> buscarPorChave(String chaveIdempotencia);
}
