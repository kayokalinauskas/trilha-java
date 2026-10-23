package dev.trilhajava.m13.reservas.adaptador.persistencia;

import dev.trilhajava.m13.reservas.aplicacao.porta.saida.CatalogoDeSalas;
import dev.trilhajava.m13.reservas.dominio.Sala;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/** As salas do escritório. Em produção, viriam de um cadastro. */
@Repository
class CatalogoDeSalasEmMemoria implements CatalogoDeSalas {

    private final Map<String, Sala> salas = List.of(
                    new Sala("aquario", "Aquário", 6),
                    new Sala("auditorio", "Auditório", 40),
                    new Sala("foco", "Sala de foco", 2))
            .stream()
            .collect(Collectors.toMap(Sala::id, Function.identity()));

    @Override
    public Optional<Sala> buscar(String id) {
        return Optional.ofNullable(salas.get(id));
    }
}
