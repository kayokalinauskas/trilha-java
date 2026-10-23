package dev.trilhajava.m11.aulas.senhas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.trilhajava.m11.aulas.senhas.ResultadoDaEntrada.AguardeAte;
import dev.trilhajava.m11.aulas.senhas.ResultadoDaEntrada.Autenticada;
import dev.trilhajava.m11.aulas.senhas.ResultadoDaEntrada.Recusada;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class ServicoDeAcessoTest {

    private static final Instant INICIO = Instant.parse("2026-10-05T13:00:00Z");
    private static final String SENHA = "cachorro late no quintal";

    private final RepositorioDeContasEmMemoria contas = new RepositorioDeContasEmMemoria();
    private final CodificadorQueConta codificador = new CodificadorQueConta(Codificadores.doPortal());
    private final RelogioDeTeste relogio = new RelogioDeTeste(INICIO);
    private final CaixaDeEmailEmMemoria emails = new CaixaDeEmailEmMemoria();
    private ServicoDeAcesso servico;

    @BeforeEach
    void montar() {
        var politica = new PoliticaDeSenha(senha -> senha.equals("123456789012345"), Set.of("vitalis"));
        servico = new ServicoDeAcesso(contas, codificador, politica, new ControleDeTentativas(relogio), emails);
    }

    @Test
    @DisplayName("Cadastro guarda o e-mail normalizado e um hash {argon2id}, nunca a senha")
    void cadastro() {
        servico.cadastrar("  Marta.Lima@Email.com ", SENHA);

        Conta conta = contas.buscar("marta.lima@email.com").orElseThrow();
        assertThat(conta.hashDaSenha()).startsWith("{argon2id}$argon2id$").doesNotContain(SENHA);
    }

    @Test
    @DisplayName("Senha fora da política: exceção com os problemas, e nada é guardado")
    void cadastroRecusado() {
        assertThatThrownBy(() -> servico.cadastrar("marta@email.com", "123456789012345"))
                .isInstanceOf(SenhaRecusadaException.class)
                .hasMessage("Senha recusada: [VAZADA]");
        assertThat(contas.todas()).isEmpty();
    }

    @Test
    @DisplayName("E-mail já cadastrado: sem erro visível, o dono recebe um aviso e a senha não muda")
    void cadastroRepetido() {
        servico.cadastrar("marta@email.com", SENHA);
        String hashOriginal = contas.buscar("marta@email.com").orElseThrow().hashDaSenha();

        servico.cadastrar("MARTA@email.com", "outra frase bem comprida aqui");

        assertThat(contas.buscar("marta@email.com").orElseThrow().hashDaSenha()).isEqualTo(hashOriginal);
        assertThat(emails.ultimo().assunto()).isEqualTo("Tentativa de cadastro");
    }

    @Test
    @DisplayName("Senha certa autentica; errada e e-mail inexistente dão a MESMA resposta")
    void mesmaResposta() {
        servico.cadastrar("marta@email.com", SENHA);

        assertThat(servico.entrar("Marta@Email.com", SENHA)).isEqualTo(new Autenticada("marta@email.com"));
        assertThat(servico.entrar("marta@email.com", "senha errada mas comprida")).isEqualTo(new Recusada());
        assertThat(servico.entrar("ninguem@email.com", SENHA)).isEqualTo(new Recusada());
    }

    @Test
    @DisplayName("E-mail inexistente gasta exatamente uma conferência de hash, como uma senha errada")
    void mesmoTrabalho() {
        servico.cadastrar("marta@email.com", SENHA);

        codificador.conferencias = 0;
        servico.entrar("marta@email.com", "senha errada mas comprida");
        int comConta = codificador.conferencias;

        codificador.conferencias = 0;
        servico.entrar("ninguem@email.com", "senha errada mas comprida");
        int semConta = codificador.conferencias;

        assertThat(comConta).isEqualTo(1);
        assertThat(semConta).isEqualTo(1);
    }

    @Test
    @DisplayName("A 4ª falha seguida faz esperar 30 s, exista o e-mail ou não")
    void esperaProgressiva() {
        servico.cadastrar("marta@email.com", SENHA);
        for (int i = 0; i < 4; i++) {
            servico.entrar("marta@email.com", "senha errada mas comprida");
            servico.entrar("ninguem@email.com", "senha errada mas comprida");
        }

        assertThat(servico.entrar("marta@email.com", SENHA)).isEqualTo(new AguardeAte(INICIO.plusSeconds(30)));
        assertThat(servico.entrar("ninguem@email.com", SENHA)).isEqualTo(new AguardeAte(INICIO.plusSeconds(30)));

        relogio.avancar(Duration.ofSeconds(30));
        assertThat(servico.entrar("marta@email.com", SENHA)).isInstanceOf(Autenticada.class);
    }

    @Test
    @DisplayName("Login com sucesso zera a contagem")
    void sucessoZera() {
        servico.cadastrar("marta@email.com", SENHA);
        for (int i = 0; i < 3; i++) {
            servico.entrar("marta@email.com", "senha errada mas comprida");
        }
        servico.entrar("marta@email.com", SENHA);
        for (int i = 0; i < 3; i++) {
            servico.entrar("marta@email.com", "senha errada mas comprida");
        }

        assertThat(servico.entrar("marta@email.com", SENHA)).isInstanceOf(Autenticada.class);
    }

    @Test
    @DisplayName("Senha absurda de grande é recusada sem chegar ao hash")
    void senhaGigante() {
        servico.cadastrar("marta@email.com", SENHA);
        codificador.conferencias = 0;

        assertThat(servico.entrar("marta@email.com", "x".repeat(1_000_000))).isEqualTo(new Recusada());
        assertThat(codificador.conferencias).isZero();
    }

    @Test
    @DisplayName("Hash {bcrypt} antigo vira {argon2id} no primeiro login com sucesso")
    void migraNoLogin() {
        contas.salvar(new Conta("joao@email.com", "{bcrypt}" + new BCryptPasswordEncoder(10).encode(SENHA)));

        servico.entrar("joao@email.com", "senha errada mas comprida");
        assertThat(contas.buscar("joao@email.com").orElseThrow().hashDaSenha()).startsWith("{bcrypt}");

        servico.entrar("joao@email.com", SENHA);
        assertThat(contas.buscar("joao@email.com").orElseThrow().hashDaSenha()).startsWith("{argon2id}");
        assertThat(servico.entrar("joao@email.com", SENHA)).isInstanceOf(Autenticada.class);
    }
}
