package dev.trilhajava.fintech;

import static dev.trilhajava.fintech.ApiDeTeste.cpf;
import static dev.trilhajava.fintech.ApiDeTeste.novaConta;
import static dev.trilhajava.fintech.ApiDeTeste.valor;
import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import dev.trilhajava.fintech.ApiDeTeste.Resposta;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Marco 4: usuários, login com JWT e contas com dono. Os tokens "de ataque" são montados aqui mesmo,
 * só com o JDK (HMAC-SHA256), para o teste não depender de biblioteca de JWT.
 */
@TesteDeAceitacao
@DisplayName("Fintech Core — Marco 4: autenticação, JWT e contas com dono")
class Marco4SegurancaTest {

    static final String PROBLEMAS = "https://fintech.dev/problemas/";

    @LocalServerPort
    int porta;

    @Autowired
    DataSource dataSource;

    ApiDeTeste anonimo;

    @BeforeEach
    void cliente() {
        anonimo = ApiDeTeste.anonimo(porta);
    }

    // ------------------------------------------------------------------ auxiliares

    record Pessoa(String id, String email, String senha, ApiDeTeste api) {
    }

    static String email(String nome) {
        return nome.toLowerCase() + "-" + UUID.randomUUID().toString().substring(0, 8) + "@exemplo.com";
    }

    Resposta cadastrar(String nome, String email, String senha) {
        return anonimo.post("/usuarios",
                "{\"nome\": \"%s\", \"email\": \"%s\", \"senha\": \"%s\"}".formatted(nome, email, senha));
    }

    Resposta login(String email, String senha) {
        return anonimo.post("/auth/token", "{\"email\": \"%s\", \"senha\": \"%s\"}".formatted(email, senha));
    }

    /** Cadastra, faz login e devolve um cliente HTTP com o token da pessoa. */
    Pessoa pessoa(String nome) {
        String email = email(nome);
        String senha = "senha-forte-de-" + nome.toLowerCase();
        Resposta cadastro = cadastrar(nome, email, senha);
        assertThat(cadastro.status()).as("cadastro de %s: %s", nome, cadastro).isEqualTo(201);
        Resposta token = login(email, senha);
        assertThat(token.status()).as("login de %s: %s", nome, token).isEqualTo(200);
        return new Pessoa(cadastro.ler("$.id", String.class), email, senha,
                anonimo.comToken(token.ler("$.accessToken", String.class)));
    }

    static void problema(Resposta r, int status, String tipo) {
        assertThat(r.status()).as(r.toString()).isEqualTo(status);
        assertThat(r.header("Content-Type")).hasValueSatisfying(
                tipoDeConteudo -> assertThat(tipoDeConteudo).startsWith("application/problem+json"));
        assertThat(r.ler("$.type", String.class)).isEqualTo(PROBLEMAS + tipo);
    }

    static String base64Url(String json) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    /** Monta um JWT HS256 "à mão". */
    static String jwt(String segredo, String emissor, String sujeito, List<String> papeis, Instant expira) {
        String cabecalho = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String papeisJson = papeis.stream().map(p -> "\"" + p + "\"").reduce((a, b) -> a + "," + b).orElse("");
        String corpo = base64Url("{\"iss\":\"%s\",\"sub\":\"%s\",\"roles\":[%s],\"iat\":%d,\"exp\":%d}".formatted(
                emissor, sujeito, papeisJson, Instant.now().minusSeconds(3600).getEpochSecond(), expira.getEpochSecond()));
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] assinatura = mac.doFinal((cabecalho + "." + corpo).getBytes(StandardCharsets.UTF_8));
            return cabecalho + "." + corpo + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(assinatura);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    Resposta transferir(ApiDeTeste api, String origem, String destino, String quantia) {
        return api.post("/transferencias",
                "{\"origem\": \"%s\", \"destino\": \"%s\", \"valor\": %s}".formatted(origem, destino, quantia),
                Map.of("Idempotency-Key", UUID.randomUUID().toString()));
    }

    String abrirConta(Pessoa dono) {
        Resposta r = dono.api().post("/contas", novaConta("Titular " + dono.email(), cpf(), "CORRENTE"));
        assertThat(r.status()).as(r.toString()).isEqualTo(201);
        return r.ler("$.id", String.class);
    }

    // ------------------------------------------------------------------ testes

    @Nested
    @DisplayName("cadastro e login")
    class CadastroELogin {

        @Test
        @DisplayName("POST /usuarios é público: 201 com id, e sem a senha (nem o hash) na resposta")
        void cadastro() {
            String email = email("Ana");
            Resposta r = cadastrar("Ana", email, "uma-senha-bem-longa");
            assertThat(r.status()).as(r.toString()).isEqualTo(201);
            assertThat(r.ler("$.id", String.class)).isNotBlank();
            assertThat(r.ler("$.email", String.class)).isEqualTo(email);
            assertThat(r.ler("$.papel", String.class)).isEqualTo("CLIENTE");
            assertThat(r.corpo()).doesNotContain("uma-senha-bem-longa").doesNotContainIgnoringCase("senha")
                    .doesNotContain("$2");
        }

        @Test
        @DisplayName("a senha é guardada com hash adaptativo (coluna usuario.senha_hash), nunca em texto puro")
        void hash() throws Exception {
            Pessoa ana = pessoa("Ana");
            try (Connection c = dataSource.getConnection();
                    PreparedStatement ps = c.prepareStatement("select senha_hash from usuario where email = ?")) {
                ps.setString(1, ana.email());
                try (ResultSet rs = ps.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    String hash = rs.getString(1);
                    assertThat(hash).doesNotContain(ana.senha());
                    assertThat(hash).matches("^\\{(bcrypt|argon2@SpringSecurity_v5_8|pbkdf2@SpringSecurity_v5_8)}.+");
                }
            }
        }

        @Test
        @DisplayName("senha fora de 12–64 caracteres ou e-mail inválido → 400 dados-invalidos")
        void invalidos() {
            problema(cadastrar("Ana", email("Ana"), "curta-11ch"), 400, "dados-invalidos");
            problema(cadastrar("Ana", email("Ana"), "x".repeat(65)), 400, "dados-invalidos");
            problema(cadastrar("Ana", "nao-e-email", "uma-senha-bem-longa"), 400, "dados-invalidos");
        }

        @Test
        @DisplayName("e-mail já cadastrado (sem diferenciar maiúsculas) → 409 usuario-duplicado")
        void duplicado() {
            String email = email("Bia");
            assertThat(cadastrar("Bia", email, "uma-senha-bem-longa").status()).isEqualTo(201);
            problema(cadastrar("Outra Bia", email.toUpperCase(), "outra-senha-bem-longa"), 409, "usuario-duplicado");
        }

        @Test
        @DisplayName("POST /auth/token devolve accessToken (JWT), tokenType Bearer e expiresIn")
        void token() {
            Pessoa caio = pessoa("Caio");
            Resposta r = login(caio.email(), caio.senha());
            assertThat(r.ler("$.tokenType", String.class)).isEqualTo("Bearer");
            assertThat(r.ler("$.expiresIn", Integer.class)).isBetween(60, 3600);

            String[] partes = r.ler("$.accessToken", String.class).split("\\.");
            assertThat(partes).hasSize(3);
            String corpo = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8);
            assertThat(JsonPath.<String>read(corpo, "$.sub")).isEqualTo(caio.id());
            assertThat(JsonPath.<List<String>>read(corpo, "$.roles")).containsExactly("CLIENTE");
            assertThat(JsonPath.<String>read(corpo, "$.iss")).isEqualTo("fintech-core");
            assertThat(corpo).doesNotContain(caio.senha());
        }

        @Test
        @DisplayName("senha errada ou e-mail inexistente → o MESMO 401 credenciais-invalidas")
        void credenciaisInvalidas() {
            Pessoa davi = pessoa("Davi");
            Resposta senhaErrada = login(davi.email(), "senha-errada-123");
            Resposta inexistente = login(email("Ninguem"), "senha-errada-123");
            problema(senhaErrada, 401, "credenciais-invalidas");
            problema(inexistente, 401, "credenciais-invalidas");
            assertThat(senhaErrada.ler("$.detail", String.class)).isEqualTo(inexistente.ler("$.detail", String.class));
        }

        @Test
        @DisplayName("e-mail no login não diferencia maiúsculas")
        void emailSemCaixa() {
            Pessoa eva = pessoa("Eva");
            assertThat(login(eva.email().toUpperCase(), eva.senha()).status()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("tokens")
    class Tokens {

        @Test
        @DisplayName("sem token → 401 nao-autenticado com WWW-Authenticate: Bearer")
        void semToken() {
            Resposta r = anonimo.get("/contas");
            problema(r, 401, "nao-autenticado");
            assertThat(r.header("WWW-Authenticate")).hasValueSatisfying(h -> assertThat(h).startsWith("Bearer"));
        }

        @Test
        @DisplayName("a documentação OpenAPI continua pública")
        void documentacao() {
            assertThat(anonimo.get("/v3/api-docs").status()).isEqualTo(200);
        }

        @Test
        @DisplayName("token malformado → 401")
        void malformado() {
            problema(anonimo.comToken("isto.nao.e-um-jwt").get("/contas"), 401, "nao-autenticado");
        }

        @Test
        @DisplayName("token assinado com OUTRO segredo → 401")
        void outroSegredo() {
            String falso = jwt("um-segredo-que-o-atacante-inventou-com-32-bytes", "fintech-core",
                    UUID.randomUUID().toString(), List.of("ADMIN"), Instant.now().plusSeconds(600));
            problema(anonimo.comToken(falso).get("/contas"), 401, "nao-autenticado");
        }

        @Test
        @DisplayName("token expirado → 401")
        void expirado() {
            Pessoa fabi = pessoa("Fabi");
            String vencido = jwt(ApiDeTeste.JWT_SEGREDO, "fintech-core", fabi.id(), List.of("CLIENTE"),
                    Instant.now().minusSeconds(600));
            problema(anonimo.comToken(vencido).get("/contas"), 401, "nao-autenticado");
        }

        @Test
        @DisplayName("token de outro emissor → 401")
        void outroEmissor() {
            Pessoa gil = pessoa("Gil");
            String outro = jwt(ApiDeTeste.JWT_SEGREDO, "outro-sistema", gil.id(), List.of("CLIENTE"),
                    Instant.now().plusSeconds(600));
            problema(anonimo.comToken(outro).get("/contas"), 401, "nao-autenticado");
        }

        @Test
        @DisplayName("token sem assinatura (alg: none) → 401")
        void algNone() {
            String semAssinatura = base64Url("{\"alg\":\"none\"}") + "." + base64Url(
                    "{\"iss\":\"fintech-core\",\"sub\":\"%s\",\"roles\":[\"ADMIN\"],\"exp\":%d}".formatted(
                            UUID.randomUUID(), Instant.now().plusSeconds(600).getEpochSecond())) + ".";
            problema(anonimo.comToken(semAssinatura).get("/contas"), 401, "nao-autenticado");
        }
    }

    @Nested
    @DisplayName("contas com dono")
    class ContasComDono {

        Pessoa ana;
        Pessoa bruno;
        String contaDaAna;
        String contaDoBruno;

        @BeforeEach
        void preparar() {
            ana = pessoa("Ana");
            bruno = pessoa("Bruno");
            contaDaAna = abrirConta(ana);
            contaDoBruno = abrirConta(bruno);
            assertThat(ana.api().post("/contas/" + contaDaAna + "/depositos", valor("300.00")).status()).isEqualTo(200);
        }

        @Test
        @DisplayName("o dono consulta a própria conta")
        void dono() {
            Resposta r = ana.api().get("/contas/" + contaDaAna);
            assertThat(r.status()).isEqualTo(200);
            assertThat(r.decimal("$.saldo")).isEqualByComparingTo("300.00");
        }

        @Test
        @DisplayName("BOLA: a conta de outra pessoa responde 404 — consulta, extrato, depósito e saque")
        void contaDeOutro() {
            problema(bruno.api().get("/contas/" + contaDaAna), 404, "conta-nao-encontrada");
            problema(bruno.api().get("/contas/" + contaDaAna + "/extrato"), 404, "conta-nao-encontrada");
            problema(bruno.api().post("/contas/" + contaDaAna + "/depositos", valor("1.00")), 404, "conta-nao-encontrada");
            problema(bruno.api().post("/contas/" + contaDaAna + "/saques", valor("1.00")), 404, "conta-nao-encontrada");
            assertThat(ana.api().get("/contas/" + contaDaAna).decimal("$.saldo")).isEqualByComparingTo("300.00");
        }

        @Test
        @DisplayName("a listagem mostra só as contas de quem pergunta")
        void listagem() {
            Resposta r = bruno.api().get("/contas");
            assertThat(r.status()).isEqualTo(200);
            assertThat(r.json().<List<String>>read("$.itens[*].id")).containsExactly(contaDoBruno);
            assertThat(r.ler("$.totalItens", Integer.class)).isEqualTo(1);
        }

        @Test
        @DisplayName("transferir DA própria conta para a de outra pessoa é permitido")
        void transferePropria() {
            Resposta r = transferir(ana.api(), contaDaAna, contaDoBruno, "50.00");
            assertThat(r.status()).as(r.toString()).isEqualTo(201);
            assertThat(bruno.api().get("/contas/" + contaDoBruno).decimal("$.saldo")).isEqualByComparingTo("50.00");
        }

        @Test
        @DisplayName("transferir DA conta de outra pessoa → 404, e nenhum centavo se move")
        void transfereDeOutro() {
            problema(transferir(bruno.api(), contaDaAna, contaDoBruno, "50.00"), 404, "conta-nao-encontrada");
            assertThat(ana.api().get("/contas/" + contaDaAna).decimal("$.saldo")).isEqualByComparingTo("300.00");
        }

        @Test
        @DisplayName("a transferência é visível para quem envia e para quem recebe; para terceiros, 404")
        void visibilidadeDaTransferencia() {
            String id = transferir(ana.api(), contaDaAna, contaDoBruno, "10.00").ler("$.id", String.class);
            assertThat(ana.api().get("/transferencias/" + id).status()).isEqualTo(200);
            assertThat(bruno.api().get("/transferencias/" + id).status()).isEqualTo(200);
            problema(pessoa("Carla").api().get("/transferencias/" + id), 404, "transferencia-nao-encontrada");
        }

        @Test
        @DisplayName("o ADMIN (semeado de fintech.admin.*) enxerga as contas de todos")
        void admin() {
            ApiDeTeste admin = ApiDeTeste.na(porta);
            assertThat(admin.get("/contas/" + contaDaAna).status()).isEqualTo(200);
            assertThat(admin.get("/contas/" + contaDoBruno).status()).isEqualTo(200);
        }
    }
}
