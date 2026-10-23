package dev.trilhajava.m13.aulas.modular.cobranca;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Os dados da cobrança. A consulta é pública; a gravação é package-private, porque só
 * a GeracaoDeCobranca (deste pacote) grava.
 */
@Component
public class Cobrancas {

    private final Map<UUID, Cobranca> porConsulta = new ConcurrentHashMap<>();

    public Optional<Cobranca> daConsulta(UUID consulta) {
        return Optional.ofNullable(porConsulta.get(consulta));
    }

    void registrar(Cobranca cobranca) {
        porConsulta.put(cobranca.consulta(), cobranca);
    }
}
