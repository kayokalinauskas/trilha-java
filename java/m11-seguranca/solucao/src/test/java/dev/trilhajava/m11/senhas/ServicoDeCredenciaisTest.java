package dev.trilhajava.m11.senhas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Confere a solução de referência do exercício 11.01 contra as regras do enunciado (garante que o que o site
 * mostra funciona). Não faz parte do starter.
 */
@DisplayName("Exercício 11.01 — Senhas: hash, política, bloqueio e migração de hashes")
class ServicoDeCredenciaisTest {

    /** A mesma lista do enunciado. */
    static final Set<String> VAZADAS = Set.of("senha12345678", "123456789012", "qwertyuiop123", "brasil2026!!");

    /** Um relógio que o teste avança. */
    static final class Relogio extends Clock {
        Instant agora = Instant.parse("2026-09-28T13:00:00Z");

        @Override
        public Instant instant() {
            return agora;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }

    /** Conta quantas vezes uma senha foi conferida (regra 9). */
    static final class CodificadorContador implements PasswordEncoder {
        final PasswordEncoder real = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        final AtomicInteger conferencias = new AtomicInteger();

        @Override
        public String encode(CharSequence senha) {
            return real.encode(senha);
        }

        @Override
        public boolean matches(CharSequence senha, String hash) {
            conferencias.incrementAndGet();
            return real.matches(senha, hash);
        }

        @Override
        public boolean upgradeEncoding(String hash) {
            return real.upgradeEncoding(hash);
        }
    }

    RepositorioDeUsuariosEmMemoria repositorio;
    CodificadorContador codificador;
    Relogio relogio;
    ServicoDeCredenciais servico;

    @BeforeEach
    void criar() {
        repositorio = new RepositorioDeUsuariosEmMemoria();
        codificador = new CodificadorContador();
        relogio = new Relogio();
        servico = new ServicoDeCredenciais(repositorio, codificador, relogio, VAZADAS);
    }

    String hashDe(String email) {
        return repositorio.buscar(email).orElseThrow(
                () -> new AssertionError("esperava um usuário guardado com o e-mail \"" + email + "\"")).hashDaSenha();
    }

    void assertRecusada(String email, String senha, String porque) {
        assertThatThrownBy(() -> servico.cadastrar(email, senha)).as(porque).isInstanceOf(SenhaFracaException.class);
        assertThat(repositorio.buscar(email))
                .as("um cadastro recusado não pode deixar nada guardado")
                .isEmpty();
    }

    @Nested
    @DisplayName("Regra 1 — e-mail normalizado; e-mail inválido lança IllegalArgumentException")
    class Regra1 {

        @Test
        @DisplayName("\"  Ana.Souza@Email.COM \" é guardado como \"ana.souza@email.com\"")
        void normaliza() {
            servico.cadastrar("  Ana.Souza@Email.COM ", "cafe com leite e pao de queijo");
            assertThat(repositorio.buscar("ana.souza@email.com"))
                    .as("o e-mail deve ser guardado sem os espaços das pontas e em minúsculas")
                    .isPresent();
        }

        @ParameterizedTest(name = "[{0}] é inválido")
        @NullAndEmptySource
        @ValueSource(strings = {"carlos.email.com", "@email.com", "carlos@", "carlos@@email.com", "carlos silva@email.com"})
        void invalido(String email) {
            assertThatThrownBy(() -> servico.cadastrar(email, "uma frase longa qualquer"))
                    .as("e-mail precisa de exatamente um @, com algo antes e depois, e nenhum espaço no meio")
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("o login também normaliza: \"Fabio@Email.com\" entra na conta de \"fabio@email.com\"")
        void loginNormaliza() {
            servico.cadastrar("fabio@email.com", "tapioca de coco no domingo");
            assertThat(servico.autenticar("Fabio@Email.com", "tapioca de coco no domingo"))
                    .as("maiúsculas no e-mail do login não podem impedir a entrada")
                    .isEqualTo(ResultadoLogin.SUCESSO);
        }
    }

    @Nested
    @DisplayName("Regra 2 — senha com 12 a 64 caracteres e no máximo 72 bytes em UTF-8")
    class Regra2 {

        @Test
        @DisplayName("11 caracteres é pouco")
        void curta() {
            assertRecusada("carlos@email.com", "curta123456", "\"curta123456\" tem 11 caracteres; o mínimo é 12");
        }

        @Test
        @DisplayName("65 caracteres é demais")
        void longa() {
            assertRecusada("davi@email.com", "a".repeat(65), "o máximo é 64 caracteres");
        }

        @Test
        @DisplayName("\"á\" repetido 40 vezes: 40 caracteres, mas 80 bytes")
        void bytes() {
            assertRecusada("davi@email.com", "á".repeat(40),
                    "cada \"á\" ocupa 2 bytes em UTF-8: 80 bytes passam do limite de 72 do BCrypt");
        }

        @Test
        @DisplayName("12 e 64 caracteres são aceitos (os limites valem)")
        void limites() {
            servico.cadastrar("davi@email.com", "abcdefghijkl");
            servico.cadastrar("dora@email.com", "b".repeat(64));
            assertThat(repositorio.buscar("davi@email.com")).as("12 caracteres é o mínimo, e vale").isPresent();
            assertThat(repositorio.buscar("dora@email.com")).as("64 caracteres ASCII é o máximo, e vale").isPresent();
        }
    }

    @Nested
    @DisplayName("Regra 3 — senha da lista de vazadas é recusada, sem diferenciar maiúsculas")
    class Regra3 {

        @ParameterizedTest(name = "\"{0}\"")
        @ValueSource(strings = {"senha12345678", "SENHA12345678", "Brasil2026!!"})
        void vazada(String senha) {
            assertRecusada("carlos@email.com", senha,
                    "\"" + senha + "\" está na lista de senhas vazadas (comparando sem diferenciar maiúsculas)");
        }
    }

    @Nested
    @DisplayName("Regra 4 — senha não pode conter o usuário do e-mail (se ele tiver 3 letras ou mais)")
    class Regra4 {

        @ParameterizedTest(name = "\"{0}\" para carlos@email.com")
        @ValueSource(strings = {"meu nome e carlos silva", "meu nome e CARLOS silva"})
        void contemUsuario(String senha) {
            assertRecusada("carlos@email.com", senha, "a senha contém \"carlos\", o texto antes do @");
        }

        @Test
        @DisplayName("usuário com menos de 3 letras não entra na regra: \"al\" pode ter \"qualquer frase longa\"")
        void usuarioCurto() {
            servico.cadastrar("al@email.com", "qualquer frase longa");
            assertThat(repositorio.buscar("al@email.com"))
                    .as("\"al\" tem 2 letras: a regra 4 só vale para 3 ou mais")
                    .isPresent();
        }
    }

    @Nested
    @DisplayName("Regra 5 — nenhuma regra de composição: frases, espaços e acentos valem")
    class Regra5 {

        @ParameterizedTest(name = "\"{0}\"")
        @ValueSource(strings = {"correct horse battery staple", "pão de queijo com café"})
        void frases(String senha) {
            servico.cadastrar("elis@email.com", senha);
            assertThat(repositorio.buscar("elis@email.com"))
                    .as("sem exigir número, maiúscula ou símbolo: uma frase longa é uma boa senha")
                    .isPresent();
        }
    }

    @Nested
    @DisplayName("Regra 6 — um cadastro por e-mail, sem diferenciar maiúsculas")
    class Regra6 {

        @Test
        @DisplayName("\"ELIS@email.com\" depois de \"elis@email.com\" lança EmailJaCadastradoException")
        void duplicado() {
            servico.cadastrar("elis@email.com", "primeira senha bem longa");
            assertThatThrownBy(() -> servico.cadastrar("ELIS@email.com", "outra senha bem longa"))
                    .as("depois de normalizado, é o mesmo e-mail")
                    .isInstanceOf(EmailJaCadastradoException.class);
            assertThat(servico.autenticar("elis@email.com", "primeira senha bem longa"))
                    .as("o cadastro recusado não pode trocar a senha do primeiro")
                    .isEqualTo(ResultadoLogin.SUCESSO);
        }
    }

    @Nested
    @DisplayName("Regra 7 — guarde codificador.encode(senha), nunca a senha")
    class Regra7 {

        @Test
        @DisplayName("o que fica guardado é um hash {bcrypt}, sem a senha dentro")
        void hash() {
            servico.cadastrar("ana@email.com", "cafe com leite e pao de queijo");
            assertThat(hashDe("ana@email.com"))
                    .as("com o DelegatingPasswordEncoder, encode devolve \"{bcrypt}$2...\"")
                    .startsWith("{bcrypt}$2")
                    .doesNotContain("cafe");
        }

        @Test
        @DisplayName("a mesma senha em dois usuários gera hashes diferentes (salt)")
        void salt() {
            servico.cadastrar("ana@email.com", "uma frase longa qualquer");
            servico.cadastrar("bruno@email.com", "uma frase longa qualquer");
            assertThat(hashDe("ana@email.com"))
                    .as("cada chamada de encode sorteia um salt; hashes iguais indicam que encode não foi usado")
                    .isNotEqualTo(hashDe("bruno@email.com"));
        }
    }

    @Nested
    @DisplayName("Regra 8 — senha certa dá SUCESSO; errada dá CREDENCIAIS_INVALIDAS")
    class Regra8 {

        @Test
        void basico() {
            servico.cadastrar("fabio@email.com", "tapioca de coco no domingo");
            assertThat(servico.autenticar("fabio@email.com", "tapioca de coco no domingo"))
                    .as("senha certa").isEqualTo(ResultadoLogin.SUCESSO);
            assertThat(servico.autenticar("fabio@email.com", "tapioca de coco na segunda"))
                    .as("senha errada").isEqualTo(ResultadoLogin.CREDENCIAIS_INVALIDAS);
        }
    }

    @Nested
    @DisplayName("Regra 9 — e-mail inexistente dá CREDENCIAIS_INVALIDAS, com uma conferência de senha")
    class Regra9 {

        @Test
        void emailInexistente() {
            servico.cadastrar("fabio@email.com", "tapioca de coco no domingo");
            codificador.conferencias.set(0);
            assertThat(servico.autenticar("ninguem@email.com", "qualquer coisa longa"))
                    .as("a resposta não pode revelar que o e-mail não existe")
                    .isEqualTo(ResultadoLogin.CREDENCIAIS_INVALIDAS);
            assertThat(codificador.conferencias)
                    .as("sem uma chamada a matches, a resposta sai muito mais rápido e o TEMPO revela que o e-mail"
                            + " não existe")
                    .hasValue(1);
        }
    }

    @Nested
    @DisplayName("Regras 10 e 11 — 5 falhas seguidas bloqueiam por 15 minutos; um sucesso zera as falhas")
    class Regras10e11 {

        @BeforeEach
        void cadastrar() {
            servico.cadastrar("fabio@email.com", "tapioca de coco no domingo");
        }

        void errar(int vezes) {
            for (int i = 0; i < vezes; i++) {
                assertThat(servico.autenticar("fabio@email.com", "errada " + i + " mas longa"))
                        .as("tentativa errada nº %d", i + 1)
                        .isEqualTo(ResultadoLogin.CREDENCIAIS_INVALIDAS);
            }
        }

        @Test
        @DisplayName("regra 10: bloqueado mesmo com a senha certa; aos 14 minutos ainda bloqueado; aos 15, libera")
        void bloqueio() {
            errar(5);
            assertThat(servico.autenticar("fabio@email.com", "tapioca de coco no domingo"))
                    .as("logo depois da 5ª falha, a conta está bloqueada, mesmo com a senha certa")
                    .isEqualTo(ResultadoLogin.BLOQUEADO);

            relogio.agora = relogio.agora.plus(Duration.ofMinutes(14));
            assertThat(servico.autenticar("fabio@email.com", "tapioca de coco no domingo"))
                    .as("14 minutos depois (medidos pelo Clock recebido) ainda está bloqueada")
                    .isEqualTo(ResultadoLogin.BLOQUEADO);

            relogio.agora = relogio.agora.plus(Duration.ofMinutes(1));
            assertThat(servico.autenticar("fabio@email.com", "tapioca de coco no domingo"))
                    .as("15 minutos depois da 5ª falha o bloqueio acabou")
                    .isEqualTo(ResultadoLogin.SUCESSO);
        }

        @Test
        @DisplayName("regra 11: 4 falhas, sucesso, mais 4 falhas: ainda entra (as falhas são SEGUIDAS)")
        void zeraFalhas() {
            errar(4);
            assertThat(servico.autenticar("fabio@email.com", "tapioca de coco no domingo")).isEqualTo(ResultadoLogin.SUCESSO);
            errar(4);
            assertThat(servico.autenticar("fabio@email.com", "tapioca de coco no domingo"))
                    .as("o sucesso anterior zerou a contagem: 4 falhas não bastam para bloquear")
                    .isEqualTo(ResultadoLogin.SUCESSO);
        }
    }

    @Nested
    @DisplayName("Regra 12 — senha com mais de 72 bytes no login dá CREDENCIAIS_INVALIDAS")
    class Regra12 {

        @Test
        void longaDemais() {
            servico.cadastrar("gina@email.com", "g".repeat(64));
            assertThat(servico.autenticar("gina@email.com", "g".repeat(64) + "qualquer-sufixo"))
                    .as("o BCrypt só olha os primeiros 72 bytes e aceitaria \"senha certa + lixo\"")
                    .isEqualTo(ResultadoLogin.CREDENCIAIS_INVALIDAS);
            assertThat(servico.autenticar("gina@email.com", "g".repeat(64)))
                    .as("a senha certa continua entrando")
                    .isEqualTo(ResultadoLogin.SUCESSO);
        }
    }

    @Nested
    @DisplayName("Regra 13 — hash legado vira BCrypt no primeiro login com sucesso")
    class Regra13 {

        @Test
        @DisplayName("{noop} é regravado como {bcrypt} e a senha continua funcionando")
        void migra() {
            repositorio.salvar(Usuario.novo("gil@email.com", "{noop}senha antiga em texto puro"));

            assertThat(servico.autenticar("gil@email.com", "senha antiga em texto puro")).isEqualTo(ResultadoLogin.SUCESSO);
            assertThat(hashDe("gil@email.com"))
                    .as("depois de um login com sucesso, upgradeEncoding(hash) era true: regrave com encode(senha)")
                    .startsWith("{bcrypt}$2");
            assertThat(servico.autenticar("gil@email.com", "senha antiga em texto puro"))
                    .as("o novo hash precisa conferir com a mesma senha")
                    .isEqualTo(ResultadoLogin.SUCESSO);
        }

        @Test
        @DisplayName("senha errada num usuário legado não altera o hash")
        void naoMigraComSenhaErrada() {
            repositorio.salvar(Usuario.novo("iris@email.com", "{noop}outra senha antiga"));
            servico.autenticar("iris@email.com", "chute errado");
            assertThat(hashDe("iris@email.com"))
                    .as("sem saber a senha certa não há como gerar o hash novo")
                    .isEqualTo("{noop}outra senha antiga");
        }
    }
}
