package dev.trilhajava.fintech.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.trilhajava.fintech.PostgresDeTeste;
import dev.trilhajava.fintech.dominio.Conta;
import dev.trilhajava.fintech.dominio.Cpf;
import dev.trilhajava.fintech.dominio.Papel;
import dev.trilhajava.fintech.dominio.TipoConta;
import dev.trilhajava.fintech.dominio.Usuario;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

/**
 * Teste de FATIA de persistência: só JPA + Flyway, contra PostgreSQL real. Cada teste roda numa
 * transação desfeita no final (@DataJpaTest é @Transactional), então os testes não se enxergam.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE) // não troque o PostgreSQL por um banco em memória
@Import({RepositorioDeContasJpa.class, RepositorioDeUsuariosJpa.class, PostgresDeTeste.class})
@EnabledIfDockerAvailable
@DisplayName("Repositório de contas (fatia JPA sobre PostgreSQL)")
class RepositorioDeContasJpaTest {

    static final Instant AGORA = Instant.parse("2026-09-28T13:00:00Z");

    @Autowired
    RepositorioDeContasJpa repositorio;

    @Autowired
    RepositorioDeUsuariosJpa usuarios;

    UUID ana;
    UUID bruno;

    /** Marco 4: dono_id é chave estrangeira — os donos precisam existir. */
    @BeforeEach
    void donos() {
        ana = usuario("ana@exemplo.com");
        bruno = usuario("bruno@exemplo.com");
    }

    UUID usuario(String email) {
        var usuario = new Usuario(UUID.randomUUID(), email, email, "{noop}x", Papel.CLIENTE, AGORA);
        usuarios.salvar(usuario);
        return usuario.id();
    }

    Conta conta(String cpf, TipoConta tipo, Instant criadaEm) {
        return conta(cpf, tipo, criadaEm, ana);
    }

    Conta conta(String cpf, TipoConta tipo, Instant criadaEm, UUID dono) {
        return new Conta(UUID.randomUUID(), "Ana", new Cpf(cpf), tipo, criadaEm, dono);
    }

    @Test
    @DisplayName("salva e busca, com o CPF convertido de/para texto")
    void salvaEBusca() {
        Conta conta = conta("52998224725", TipoConta.CORRENTE, AGORA);
        repositorio.salvar(conta);

        assertThat(repositorio.buscar(conta.id())).hasValueSatisfying(c -> {
            assertThat(c.cpf()).isEqualTo(new Cpf("52998224725"));
            assertThat(c.saldo()).isEqualByComparingTo("0");
        });
        assertThat(repositorio.existe(new Cpf("52998224725"), TipoConta.CORRENTE)).isTrue();
        assertThat(repositorio.existe(new Cpf("52998224725"), TipoConta.POUPANCA)).isFalse();
    }

    @Test
    @DisplayName("lista por CPF em ordem de abertura, com paginação")
    void lista() {
        Conta poupanca = conta("11144477735", TipoConta.POUPANCA, AGORA.plusSeconds(60));
        Conta corrente = conta("11144477735", TipoConta.CORRENTE, AGORA);
        repositorio.salvar(poupanca);
        repositorio.salvar(corrente);

        assertThat(repositorio.listar(new Cpf("11144477735"), null, 0, 10)).extracting(Conta::id)
                .containsExactly(corrente.id(), poupanca.id());
        assertThat(repositorio.listar(new Cpf("11144477735"), null, 1, 1)).extracting(Conta::id).containsExactly(poupanca.id());
        assertThat(repositorio.contar(new Cpf("11144477735"), null)).isEqualTo(2);
    }

    @Test
    @DisplayName("a constraint UNIQUE (cpf, tipo) do banco barra a conta duplicada")
    void unique() {
        repositorio.salvar(conta("39053344705", TipoConta.CORRENTE, AGORA));
        assertThatThrownBy(() -> repositorio.salvar(conta("39053344705", TipoConta.CORRENTE, AGORA)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("marco 4: filtra por dono (com e sem CPF)")
    void porDono() {
        Conta daAna = conta("86288366757", TipoConta.CORRENTE, AGORA, ana);
        Conta doBruno = conta("71428793860", TipoConta.CORRENTE, AGORA, bruno);
        repositorio.salvar(daAna);
        repositorio.salvar(doBruno);

        assertThat(repositorio.listar(null, bruno, 0, 10)).extracting(Conta::id).containsExactly(doBruno.id());
        assertThat(repositorio.contar(null, bruno)).isEqualTo(1);
        assertThat(repositorio.listar(new Cpf("86288366757"), bruno, 0, 10)).isEmpty();
        assertThat(repositorio.contar(new Cpf("86288366757"), ana)).isEqualTo(1);
    }
}
