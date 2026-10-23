package dev.trilhajava.fintech.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** Marco 5: um aviso para o dono de uma conta, gerado a partir de um evento. */
@Entity
@Table(name = "notificacao")
public class Notificacao {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoNotificacao tipo;

    @Column(name = "transferencia_id", nullable = false)
    private UUID transferencia;

    @Column(name = "conta_id", nullable = false)
    private UUID conta;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, length = 200)
    private String mensagem;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    protected Notificacao() {
    }

    public Notificacao(UUID id, UUID usuario, TipoNotificacao tipo, UUID transferencia, UUID conta,
            BigDecimal valor, Instant criadaEm) {
        this.id = Objects.requireNonNull(id);
        this.usuario = Objects.requireNonNull(usuario);
        this.tipo = Objects.requireNonNull(tipo);
        this.transferencia = Objects.requireNonNull(transferencia);
        this.conta = Objects.requireNonNull(conta);
        this.valor = Objects.requireNonNull(valor);
        this.criadaEm = Objects.requireNonNull(criadaEm);
        this.mensagem = texto(tipo, valor);
    }

    private static String texto(TipoNotificacao tipo, BigDecimal valor) {
        String reais = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR")).format(valor);
        return switch (tipo) {
            case TRANSFERENCIA_ENVIADA -> "Você enviou uma transferência de " + reais;
            case TRANSFERENCIA_RECEBIDA -> "Você recebeu uma transferência de " + reais;
        };
    }

    public UUID id() {
        return id;
    }

    public UUID usuario() {
        return usuario;
    }

    public TipoNotificacao tipo() {
        return tipo;
    }

    public UUID transferencia() {
        return transferencia;
    }

    public UUID conta() {
        return conta;
    }

    public BigDecimal valor() {
        return valor;
    }

    public String mensagem() {
        return mensagem;
    }

    public Instant criadaEm() {
        return criadaEm;
    }
}
