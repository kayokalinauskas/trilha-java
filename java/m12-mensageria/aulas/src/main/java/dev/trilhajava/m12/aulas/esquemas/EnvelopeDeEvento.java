package dev.trilhajava.m12.aulas.esquemas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.JsonNode;

import java.time.Instant;

/**
 * Envelope no formato JSON do CloudEvents 1.0 (modo estruturado).
 *
 * <p>Os quatro primeiros atributos são obrigatórios pela especificação. O {@code type} carrega
 * a versão do esquema ({@code ...pedido-confirmado.v2}); o {@code data} fica como árvore JSON
 * para que os upcasters possam reescrevê-lo antes de virar um objeto Java.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EnvelopeDeEvento(
        String specversion,
        String id,
        String source,
        String type,
        Instant time,
        String subject,
        String datacontenttype,
        String dataschema,
        JsonNode data) {

    public EnvelopeDeEvento comTipoEDados(String novoTipo, JsonNode novosDados) {
        return new EnvelopeDeEvento(specversion, id, source, novoTipo, time, subject,
                datacontenttype, dataschema, novosDados);
    }
}
