package dev.trilhajava.fintech.dominio;

import java.util.List;
import java.util.UUID;

public interface RepositorioDeMovimentos {

    void registrar(Movimento movimento);

    /** Em ordem cronológica. */
    List<Movimento> extrato(UUID contaId);
}
