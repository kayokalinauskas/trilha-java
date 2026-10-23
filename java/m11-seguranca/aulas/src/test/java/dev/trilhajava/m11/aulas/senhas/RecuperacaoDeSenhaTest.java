package dev.trilhajava.m11.aulas.senhas;

import static dev.trilhajava.m11.aulas.senhas.RecuperacaoDeSenha.Resultado.REDEFINIDA;
import static dev.trilhajava.m11.aulas.senhas.RecuperacaoDeSenha.Resultado.TOKEN_INVALIDO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class RecuperacaoDeSenhaTest {

    private static final String SENHA_NOVA = "girassol no vaso da varanda";

    private final RepositorioDeContasEmMemoria contas = new RepositorioDeContasEmMemoria();
    private final PasswordEncoder codificador = Codificadores.doPortal();
    private final RelogioDeTeste relogio = new RelogioDeTeste(Instant.parse("2026-10-05T13:00:00Z"));
    private final CaixaDeEmailEmMemoria emails = new CaixaDeEmailEmMemoria();
    private final ControleDeTentativas tentativas = new ControleDeTentativas(relogio);
    private RecuperacaoDeSenha recuperacao;

    @BeforeEach
    void montar() {
        contas.salvar(new Conta("marta@email.com", codificador.encode("cachorro late no quintal")));
        var politica = new PoliticaDeSenha(senha -> false, Set.of("vitalis"));
        recuperacao = new RecuperacaoDeSenha(contas, codificador, politica, tentativas, emails, relogio);
    }

    /** O token só existe no e-mail: o teste o tira de lá, como a pessoa faria clicando no link. */
    private String tokenDoUltimoEmail() {
        String corpo = emails.ultimo().corpo();
        return corpo.substring(corpo.indexOf("token=") + "token=".length());
    }

    @Test
    @DisplayName("O link leva 43 caracteres de token (256 bits em Base64 URL)")
    void token() {
        recuperacao.solicitar("marta@email.com");

        assertThat(tokenDoUltimoEmail()).hasSize(43);
    }

    @Test
    @DisplayName("E-mail não cadastrado: nada acontece, e a chamada se comporta igual")
    void emailInexistente() {
        recuperacao.solicitar("ninguem@email.com");

        assertThat(emails.enviados).isEmpty();
    }

    @Test
    @DisplayName("Token válido redefine a senha, libera o bloqueio e avisa o dono")
    void redefine() {
        for (int i = 0; i < 100; i++) {
            tentativas.registrarFalha("marta@email.com");
        }
        recuperacao.solicitar("marta@email.com");

        assertThat(recuperacao.redefinir(tokenDoUltimoEmail(), SENHA_NOVA)).isEqualTo(REDEFINIDA);

        String hash = contas.buscar("marta@email.com").orElseThrow().hashDaSenha();
        assertThat(codificador.matches(SENHA_NOVA, hash)).isTrue();
        assertThat(tentativas.exigeRecuperacao("marta@email.com")).isFalse();
        assertThat(emails.ultimo().assunto()).isEqualTo("Sua senha foi alterada");
    }

    @Test
    @DisplayName("O token vale uma vez só")
    void usoUnico() {
        recuperacao.solicitar("marta@email.com");
        String token = tokenDoUltimoEmail();

        assertThat(recuperacao.redefinir(token, SENHA_NOVA)).isEqualTo(REDEFINIDA);
        assertThat(recuperacao.redefinir(token, "outra frase bem comprida")).isEqualTo(TOKEN_INVALIDO);
    }

    @Test
    @DisplayName("Vale 20 minutos: em 19min59s funciona, em 20 min não")
    void expiracao() {
        recuperacao.solicitar("marta@email.com");
        String token = tokenDoUltimoEmail();

        relogio.avancar(Duration.ofMinutes(20));
        assertThat(recuperacao.redefinir(token, SENHA_NOVA)).isEqualTo(TOKEN_INVALIDO);
    }

    @Test
    @DisplayName("Pedido novo invalida o token anterior")
    void pedidoNovoInvalidaAnterior() {
        recuperacao.solicitar("marta@email.com");
        String primeiro = tokenDoUltimoEmail();
        recuperacao.solicitar("marta@email.com");
        String segundo = tokenDoUltimoEmail();

        assertThat(recuperacao.redefinir(primeiro, SENHA_NOVA)).isEqualTo(TOKEN_INVALIDO);
        assertThat(recuperacao.redefinir(segundo, SENHA_NOVA)).isEqualTo(REDEFINIDA);
    }

    @Test
    @DisplayName("Senha nova fora da política: exceção, e o token continua valendo")
    void senhaNovaRecusada() {
        recuperacao.solicitar("marta@email.com");
        String token = tokenDoUltimoEmail();

        assertThatThrownBy(() -> recuperacao.redefinir(token, "curta"))
                .isInstanceOf(SenhaRecusadaException.class);
        assertThat(recuperacao.redefinir(token, SENHA_NOVA)).isEqualTo(REDEFINIDA);
    }

    @Test
    @DisplayName("Token inventado ou vazio: TOKEN_INVALIDO")
    void tokenInventado() {
        assertThat(recuperacao.redefinir("abc", SENHA_NOVA)).isEqualTo(TOKEN_INVALIDO);
        assertThat(recuperacao.redefinir(null, SENHA_NOVA)).isEqualTo(TOKEN_INVALIDO);
    }
}
