package dev.trilhajava.m12.aulas.esquemas;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Lê um evento "pedido confirmado" em qualquer versão suportada e entrega sempre a versão atual.
 *
 * <ol>
 *   <li>Desserializa o envelope CloudEvents e confere a {@code specversion}.</li>
 *   <li>Aplica a cadeia de upcasters até chegar ao tipo atual (v1 → v2 → ...).</li>
 *   <li>Converte o {@code data} no record do consumidor, com o mapper tolerante.</li>
 * </ol>
 * Tipo fora da cadeia é falha permanente: {@link TipoDeEventoDesconhecidoException}.
 */
public class LeitorDePedidoConfirmado {

    public static final String TIPO_ATUAL = PedidoConfirmadoV1ParaV2.V2;

    private final JsonMapper mapper;
    private final Map<String, Upcaster> upcastersPorOrigem;

    public LeitorDePedidoConfirmado(JsonMapper mapper, List<Upcaster> upcasters) {
        this.mapper = mapper;
        this.upcastersPorOrigem = upcasters.stream()
                .collect(Collectors.toMap(Upcaster::tipoDeOrigem, Function.identity()));
    }

    public PedidoConfirmado ler(String json) {
        EnvelopeDeEvento envelope = mapper.readValue(json, EnvelopeDeEvento.class);
        if (!"1.0".equals(envelope.specversion())) {
            throw new TipoDeEventoDesconhecidoException(
                    "specversion não suportada: " + envelope.specversion());
        }
        EnvelopeDeEvento atual = atualizar(envelope);
        return mapper.treeToValue(atual.data(), PedidoConfirmado.class);
    }

    EnvelopeDeEvento atualizar(EnvelopeDeEvento envelope) {
        EnvelopeDeEvento atual = envelope;
        while (!TIPO_ATUAL.equals(atual.type())) {
            Upcaster upcaster = upcastersPorOrigem.get(atual.type());
            if (upcaster == null) {
                throw new TipoDeEventoDesconhecidoException(
                        "evento " + envelope.id() + " com tipo não suportado: " + atual.type());
            }
            ObjectNode convertido = upcaster.converter((ObjectNode) atual.data());
            atual = atual.comTipoEDados(upcaster.tipoDeDestino(), convertido);
        }
        return atual;
    }
}
