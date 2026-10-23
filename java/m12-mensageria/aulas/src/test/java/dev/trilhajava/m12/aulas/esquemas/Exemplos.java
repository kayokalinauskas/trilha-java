package dev.trilhajava.m12.aulas.esquemas;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Lê os exemplos de eventos guardados em src/test/resources/esquemas/pedido-confirmado. */
final class Exemplos {

    private Exemplos() {
    }

    static Path pasta() {
        try {
            return Path.of(Exemplos.class.getResource("/esquemas/pedido-confirmado").toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("pasta de exemplos inválida", e);
        }
    }

    static String ler(String arquivo) {
        try {
            return Files.readString(pasta().resolve(arquivo));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static List<Path> todos() {
        try (var arquivos = Files.list(pasta())) {
            return arquivos.filter(p -> p.toString().endsWith(".json")).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static LeitorDePedidoConfirmado leitor() {
        return new LeitorDePedidoConfirmado(MapeadoresJson.tolerante(),
                List.of(new PedidoConfirmadoV1ParaV2()));
    }
}
