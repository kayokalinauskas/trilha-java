package dev.trilhajava.m13.aulas.modular.agenda.internal;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * Os dados da agenda. Em memória para a aula; numa aplicação real, as tabelas deste módulo
 * (num schema "agenda", por exemplo), que nenhum outro módulo consulta diretamente.
 */
@Repository
public class Consultas {

    private final Map<UUID, Consulta> porId = new ConcurrentHashMap<>();

    public void salvar(Consulta consulta) {
        porId.put(consulta.id(), consulta);
    }

    public Optional<Consulta> buscar(UUID id) {
        return Optional.ofNullable(porId.get(id));
    }
}
