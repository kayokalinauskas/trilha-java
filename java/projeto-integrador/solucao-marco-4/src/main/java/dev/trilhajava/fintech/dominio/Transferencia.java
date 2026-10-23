package dev.trilhajava.fintech.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Entity
@Table(name = "transferencia")
public class Transferencia {

    @Id
    private UUID id;

    @Column(name = "origem_id", nullable = false)
    private UUID origem;

    @Column(name = "destino_id", nullable = false)
    private UUID destino;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    @Column(name = "chave_idempotencia", nullable = false, length = 100)
    private String chaveIdempotencia;

    /** Impressão digital do pedido: detecta a MESMA chave reutilizada com OUTRO conteúdo. */
    @Column(name = "hash_requisicao", nullable = false, length = 64)
    private String hashRequisicao;

    protected Transferencia() {
    }

    public Transferencia(UUID id, UUID origem, UUID destino, BigDecimal valor, Instant criadaEm,
            String chaveIdempotencia) {
        if (origem.equals(destino)) {
            throw new TransferenciaInvalidaException("A conta de origem e a de destino devem ser diferentes");
        }
        this.id = id;
        this.origem = origem;
        this.destino = destino;
        this.valor = valor;
        this.criadaEm = criadaEm;
        this.chaveIdempotencia = chaveIdempotencia;
        this.hashRequisicao = hash(origem, destino, valor);
    }

    public static String hash(UUID origem, UUID destino, BigDecimal valor) {
        String conteudo = origem + "|" + destino + "|" + valor.stripTrailingZeros().toPlainString();
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(conteudo.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Mesma chave → mesmo pedido? Se não, é reutilização indevida da chave. */
    public void exigirMesmoPedido(UUID outraOrigem, UUID outroDestino, BigDecimal outroValor) {
        if (!hashRequisicao.equals(hash(outraOrigem, outroDestino, outroValor))) {
            throw new ChaveIdempotenciaReutilizadaException(chaveIdempotencia);
        }
    }

    public UUID id() {
        return id;
    }

    public UUID origem() {
        return origem;
    }

    public UUID destino() {
        return destino;
    }

    public BigDecimal valor() {
        return valor;
    }

    public Instant criadaEm() {
        return criadaEm;
    }
}
