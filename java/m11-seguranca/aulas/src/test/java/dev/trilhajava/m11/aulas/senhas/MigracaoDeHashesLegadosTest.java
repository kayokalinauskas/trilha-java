package dev.trilhajava.m11.aulas.senhas;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MigracaoDeHashesLegadosTest {

    // SHA-1 sem salt, como o sistema antigo gravava: sha1("cachorro late no quintal")
    private static final String SHA1_LEGADO = EnvelopeSha1Legado.sha1Hex("cachorro late no quintal");

    private final RepositorioDeContasEmMemoria contas = new RepositorioDeContasEmMemoria();
    private final MigracaoDeHashesLegados migracao = new MigracaoDeHashesLegados(contas, new EnvelopeSha1Legado());

    @Test
    @DisplayName("Lote: todo SHA-1 sem prefixo vira {bcrypt-sha1}, e rodar de novo não muda nada")
    void lote() {
        contas.salvar(new Conta("joao@email.com", SHA1_LEGADO));
        contas.salvar(new Conta("marta@email.com", Codificadores.doPortal().encode("outra frase bem comprida")));

        assertThat(migracao.executar()).isEqualTo(1);
        assertThat(migracao.executar()).isZero();

        String hash = contas.buscar("joao@email.com").orElseThrow().hashDaSenha();
        assertThat(hash).startsWith("{bcrypt-sha1}$2a$12$").doesNotContain(SHA1_LEGADO);
    }

    @Test
    @DisplayName("Depois do lote, o login confere pelo envelope e regrava em {argon2id}")
    void loteDepoisLogin() {
        contas.salvar(new Conta("joao@email.com", SHA1_LEGADO));
        migracao.executar();
        var servico = new ServicoDeAcesso(contas, Codificadores.doPortal(),
                new PoliticaDeSenha(senha -> false, Set.of()),
                new ControleDeTentativas(new RelogioDeTeste(Instant.parse("2026-10-05T13:00:00Z"))),
                new CaixaDeEmailEmMemoria());

        assertThat(servico.entrar("joao@email.com", "cachorro late no quintal"))
                .isInstanceOf(ResultadoDaEntrada.Autenticada.class);
        assertThat(contas.buscar("joao@email.com").orElseThrow().hashDaSenha()).startsWith("{argon2id}");
    }

    @Test
    @DisplayName("Reconhece SHA-1 legado só por 40 dígitos hexadecimais sem prefixo")
    void reconheceSha1() {
        assertThat(MigracaoDeHashesLegados.ehSha1Legado(SHA1_LEGADO)).isTrue();
        assertThat(MigracaoDeHashesLegados.ehSha1Legado("{bcrypt}$2a$10$abc")).isFalse();
        assertThat(MigracaoDeHashesLegados.ehSha1Legado("z".repeat(40))).isFalse();
    }
}
