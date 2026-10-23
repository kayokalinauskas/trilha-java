package dev.trilhajava.m11.aulas.senhas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

class HashDeSenhaTest {

    @Test
    @DisplayName("SHA-256 sem salt: duas pessoas com a mesma senha têm o mesmo hash")
    void sha256SemSalt() throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        String daAna = HexFormat.of().formatHex(sha256.digest("bolo de fubá".getBytes(StandardCharsets.UTF_8)));
        String doBeto = HexFormat.of().formatHex(sha256.digest("bolo de fubá".getBytes(StandardCharsets.UTF_8)));

        assertThat(daAna).isEqualTo(doBeto);
    }

    @Test
    @DisplayName("Argon2id com salt: mesma senha, hashes diferentes, e os dois conferem")
    void argon2idComSalt() {
        PasswordEncoder argon2 = Codificadores.argon2idOwasp();

        String daAna = argon2.encode("bolo de fubá com café coado");
        String doBeto = argon2.encode("bolo de fubá com café coado");

        assertThat(daAna).startsWith("$argon2id$v=19$m=19456,t=2,p=1$").isNotEqualTo(doBeto);
        assertThat(argon2.matches("bolo de fubá com café coado", daAna)).isTrue();
        assertThat(argon2.matches("bolo de fubá com café coado", doBeto)).isTrue();
        assertThat(argon2.matches("bolo de fubá com café frio", daAna)).isFalse();
    }

    @Test
    @DisplayName("BCrypt: encode recusa mais de 72 bytes, mas matches compara só os 72 primeiros")
    void bcryptSetentaEDoisBytes() {
        var bcrypt = new BCryptPasswordEncoder(10);
        String setentaEDois = "a".repeat(72);
        String hash = bcrypt.encode(setentaEDois);

        assertThatThrownBy(() -> bcrypt.encode(setentaEDois + "b"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("password cannot be more than 72 bytes");
        // O perigo: "a"×72 seguido de QUALQUER coisa confere com o hash de "a"×72.
        assertThat(bcrypt.matches(setentaEDois + "qualquer coisa", hash)).isTrue();
    }

    @Test
    @DisplayName("Em UTF-8, 40 letras acentuadas já passam dos 72 bytes")
    void acentosOcupamDoisBytes() {
        String senha = "á".repeat(40);

        assertThat(senha.length()).isEqualTo(40);
        assertThat(senha.getBytes(StandardCharsets.UTF_8)).hasSize(80);
    }

    @Test
    @DisplayName("DelegatingPasswordEncoder do Spring: prefixo {bcrypt} e upgrade de {noop}")
    void delegatingPadrao() {
        PasswordEncoder codificador = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        String hash = codificador.encode("bolo de fubá com café coado");

        assertThat(hash).startsWith("{bcrypt}$2a$10$").hasSize(68);
        assertThat(codificador.upgradeEncoding(hash)).isFalse();
        assertThat(codificador.matches("antiga", "{noop}antiga")).isTrue();
        assertThat(codificador.upgradeEncoding("{noop}antiga")).isTrue();
    }

    @Test
    @DisplayName("upgradeEncoding também enxerga custo: BCrypt 10 precisa de upgrade num codificador de custo 12")
    void upgradePorCusto() {
        String hashCusto10 = new BCryptPasswordEncoder(10).encode("bolo de fubá");

        assertThat(new BCryptPasswordEncoder(12).upgradeEncoding(hashCusto10)).isTrue();
        assertThat(new BCryptPasswordEncoder(10).upgradeEncoding(hashCusto10)).isFalse();
    }

    @Test
    @DisplayName("O codificador do portal grava {argon2id} e pede upgrade dos formatos antigos")
    void codificadorDoPortal() {
        PasswordEncoder portal = Codificadores.doPortal();

        String novo = portal.encode("bolo de fubá com café coado");
        String antigo = "{bcrypt}" + new BCryptPasswordEncoder(10).encode("bolo de fubá com café coado");

        assertThat(novo).startsWith("{argon2id}$argon2id$");
        assertThat(portal.upgradeEncoding(novo)).isFalse();
        assertThat(portal.matches("bolo de fubá com café coado", antigo)).isTrue();
        assertThat(portal.upgradeEncoding(antigo)).isTrue();
    }

    @Test
    @DisplayName("Hash sem prefixo no DelegatingPasswordEncoder: IllegalArgumentException")
    void semPrefixo() {
        PasswordEncoder portal = Codificadores.doPortal();
        String bcryptCru = new BCryptPasswordEncoder(10).encode("bolo de fubá");

        assertThatThrownBy(() -> portal.matches("bolo de fubá", bcryptCru))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Given that there is no default password encoder configured");
    }
}
