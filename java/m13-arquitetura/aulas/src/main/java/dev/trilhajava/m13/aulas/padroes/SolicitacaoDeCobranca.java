package dev.trilhajava.m13.aulas.padroes;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Tudo o que o gateway precisa para cobrar um pedido. Os quatro primeiros campos são
 * obrigatórios; os demais têm valor padrão. O builder recebe os obrigatórios no próprio
 * construtor, então é impossível esquecê-los, e deixa os opcionais com nome.
 */
public record SolicitacaoDeCobranca(
        String pedidoId,
        String clienteId,
        BigDecimal valor,
        MeioDePagamento meio,
        String chaveDeIdempotencia,
        String descricao,
        boolean notificarCliente,
        Map<String, String> metadados) {

    public SolicitacaoDeCobranca {
        Objects.requireNonNull(pedidoId, "pedidoId");
        Objects.requireNonNull(clienteId, "clienteId");
        Objects.requireNonNull(meio, "meio");
        Objects.requireNonNull(chaveDeIdempotencia, "chaveDeIdempotencia");
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("valor deve ser positivo: " + valor);
        }
        if (valor.scale() > 2) {
            throw new IllegalArgumentException("valor com mais de 2 casas decimais: " + valor);
        }
        metadados = Map.copyOf(metadados);
    }

    public static Builder builder(String pedidoId, String clienteId, BigDecimal valor, MeioDePagamento meio) {
        return new Builder(pedidoId, clienteId, valor, meio);
    }

    public static final class Builder {
        private final String pedidoId;
        private final String clienteId;
        private final BigDecimal valor;
        private final MeioDePagamento meio;
        private String chaveDeIdempotencia;
        private String descricao = "";
        private boolean notificarCliente = true;
        private final Map<String, String> metadados = new LinkedHashMap<>();

        private Builder(String pedidoId, String clienteId, BigDecimal valor, MeioDePagamento meio) {
            this.pedidoId = pedidoId;
            this.clienteId = clienteId;
            this.valor = valor;
            this.meio = meio;
        }

        public Builder chaveDeIdempotencia(String chave) {
            this.chaveDeIdempotencia = chave;
            return this;
        }

        public Builder descricao(String descricao) {
            this.descricao = descricao;
            return this;
        }

        public Builder semNotificarCliente() {
            this.notificarCliente = false;
            return this;
        }

        public Builder metadado(String chave, String valor) {
            this.metadados.put(chave, valor);
            return this;
        }

        public SolicitacaoDeCobranca build() {
            // Padrão: uma cobrança por pedido. Repetir a mesma chave não cobra duas vezes.
            String chave = chaveDeIdempotencia != null ? chaveDeIdempotencia : "cobranca-" + pedidoId;
            return new SolicitacaoDeCobranca(pedidoId, clienteId, valor, meio, chave,
                    descricao, notificarCliente, metadados);
        }
    }
}
