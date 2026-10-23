package dev.trilhajava.m11.aulas.dados;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

@DataJpaTest
@Import({ChavesDeTeste.class, CadastroDePacientes.class, RotacaoDeChaves.class})
class PacienteCifradoJpaTest {

    @Autowired CadastroDePacientes cadastro;
    @Autowired PacienteRepository pacientes;
    @Autowired RotacaoDeChaves rotacao;
    @Autowired TestEntityManager em;
    @Autowired DataSource dataSource;

    JdbcClient jdbc;

    @BeforeEach
    void jdbc() {
        jdbc = JdbcClient.create(dataSource);
    }

    @Test
    void noBancoSoFicamOCifradoEOIndice() {
        Paciente marina = cadastro.cadastrar("Marina Souza", new Cpf("123.456.789-09"));
        em.flush();
        em.clear();

        String coluna = jdbc.sql("SELECT cpf_cifrado FROM paciente WHERE id = ?")
                .param(marina.getId()).query(String.class).single();
        assertThat(coluna).startsWith("v2:").doesNotContain("12345678909");

        Paciente lido = pacientes.findById(marina.getId()).orElseThrow();
        assertThat(lido.getCpf().digitos()).isEqualTo("12345678909");   // o conversor decifrou
    }

    @Test
    void buscaPeloBlindIndex() {
        cadastro.cadastrar("Marina Souza", new Cpf("123.456.789-09"));
        cadastro.cadastrar("Caio Lima", new Cpf("987.654.321-00"));
        em.flush();
        em.clear();

        assertThat(cadastro.buscarPorCpf(new Cpf("12345678909")))
                .hasValueSatisfying(p -> assertThat(p.getNome()).isEqualTo("Marina Souza"));
        assertThat(cadastro.buscarPorCpf(new Cpf("111.222.333-44"))).isEmpty();
    }

    @Test
    void cpfRepetidoERecusadoSemExporONumero() {
        cadastro.cadastrar("Marina Souza", new Cpf("123.456.789-09"));

        assertThatThrownBy(() -> cadastro.cadastrar("Marina S.", new Cpf("12345678909")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("já existe paciente com o CPF ***.***.***-09");
    }

    @Test
    void rotacaoRegravaEmLotesOQueFoiCifradoComAChaveAntiga() {
        CifradorDeCampo antigo = ChavesDeTeste.cifradorAntesDaRotacao();
        IndiceCego indice = new IndiceCego(ChavesDeTeste.HMAC);
        String[] cpfs = {"12345678909", "98765432100", "11122233344"};
        for (int i = 0; i < cpfs.length; i++) {
            jdbc.sql("INSERT INTO paciente (id, nome, cpf_cifrado, cpf_indice) VALUES (?, ?, ?, ?)")
                    .params(1001 + i, "Paciente " + (i + 1),
                            antigo.cifrar(cpfs[i], "paciente.cpf"),
                            indice.doCpf(new Cpf(cpfs[i]), "paciente.cpf"))
                    .update();
        }

        assertThat(rotacao.recifrarLote(2)).isEqualTo(2);
        assertThat(rotacao.recifrarLote(2)).isEqualTo(1);
        assertThat(rotacao.recifrarLote(2)).isZero();   // nada mais a fazer

        assertThat(jdbc.sql("SELECT cpf_cifrado FROM paciente").query(String.class).list())
                .hasSize(3)
                .allSatisfy(coluna -> assertThat(coluna).startsWith("v2:"));
        assertThat(cadastro.buscarPorCpf(new Cpf("98765432100")))
                .hasValueSatisfying(p -> assertThat(p.getCpf().digitos()).isEqualTo("98765432100"));
    }
}
