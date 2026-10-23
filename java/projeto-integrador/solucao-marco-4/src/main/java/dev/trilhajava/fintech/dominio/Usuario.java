package dev.trilhajava.fintech.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** Quem usa a API. Guarda o HASH da senha (quem calcula é a aplicação); o domínio nunca vê a senha. */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 254, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Papel papel;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected Usuario() {
    }

    public Usuario(UUID id, String nome, String email, String senhaHash, Papel papel, Instant criadoEm) {
        this.id = Objects.requireNonNull(id);
        this.nome = Objects.requireNonNull(nome);
        this.email = normalizarEmail(email);
        this.senhaHash = Objects.requireNonNull(senhaHash);
        this.papel = Objects.requireNonNull(papel);
        this.criadoEm = Objects.requireNonNull(criadoEm);
    }

    /** "Ana@Exemplo.com " e "ana@exemplo.com" são o mesmo usuário. */
    public static String normalizarEmail(String email) {
        return Objects.requireNonNull(email).strip().toLowerCase(Locale.ROOT);
    }

    public UUID id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public String email() {
        return email;
    }

    public String senhaHash() {
        return senhaHash;
    }

    public Papel papel() {
        return papel;
    }

    public Instant criadoEm() {
        return criadoEm;
    }
}
