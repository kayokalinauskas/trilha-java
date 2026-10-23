package dev.trilhajava.m11.aulas.owasp.arquivos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PathTraversalTest {

    @TempDir
    Path raiz;

    Path notas;
    Path configuracao;

    @BeforeEach
    void montarPastas() throws IOException {
        // raiz/
        //   notas/NF-2026-000123.pdf
        //   config/application.properties   ← não deveria sair por GET /notas/...
        notas = Files.createDirectories(raiz.resolve("notas"));
        Files.writeString(notas.resolve("NF-2026-000123.pdf"), "%PDF nota 123");
        configuracao = Files.createDirectories(raiz.resolve("config")).resolve("application.properties");
        Files.writeString(configuracao, "spring.datasource.password=senha-do-banco");
    }

    @Test
    void usoNormalFuncionaNasDuasVersoes() throws IOException {
        assertThat(new NotasFiscaisVulneravel(notas).baixar("NF-2026-000123.pdf")).asString(StandardCharsets.UTF_8)
                .isEqualTo("%PDF nota 123");
        assertThat(new NotasFiscaisSeguras(notas).baixar("NF-2026-000123.pdf")).asString(StandardCharsets.UTF_8)
                .isEqualTo("%PDF nota 123");
    }

    @Test
    void versaoVulneravelEntregaArquivosForaDaPasta() throws IOException {
        var vulneravel = new NotasFiscaisVulneravel(notas);

        // ../ sobe um nível
        assertThat(vulneravel.baixar("../config/application.properties")).asString(StandardCharsets.UTF_8)
                .contains("senha-do-banco");
        // caminho absoluto: Path.resolve DESCARTA a pasta base
        assertThat(vulneravel.baixar(configuracao.toString())).asString(StandardCharsets.UTF_8)
                .contains("senha-do-banco");
    }

    @Test
    void versaoSeguraRecusaTodasAsVariacoes() throws IOException {
        var segura = new NotasFiscaisSeguras(notas);

        assertThatThrownBy(() -> segura.baixar("../config/application.properties"))
                .isInstanceOf(ArquivoForaDaPastaException.class);
        assertThatThrownBy(() -> segura.baixar(configuracao.toString()))
                .isInstanceOf(ArquivoForaDaPastaException.class);
        assertThatThrownBy(() -> segura.baixar("..\\config\\application.properties"))
                .isInstanceOf(ArquivoForaDaPastaException.class);
        assertThatThrownBy(() -> segura.baixar("NF-2026-999999.pdf"))
                .isInstanceOf(ArquivoForaDaPastaException.class)
                .hasMessage("arquivo inexistente");
    }

    @Test
    void segundaCamadaPegaOQueEscaparDaPrimeira() {
        // Mesmo se alguém afrouxar o formato do nome, normalize + startsWith segura o ../
        Path alvo = notas.resolve("../config/application.properties").normalize();
        assertThat(alvo.startsWith(notas)).isFalse();
        // E startsWith compara SEGMENTOS, não texto: "notas-antigas" não começa com "notas"
        assertThat(raiz.resolve("notas-antigas/x.pdf").startsWith(notas)).isFalse();
        assertThat(raiz.resolve("notas-antigas/x.pdf").toString().startsWith(notas.toString())).isTrue();
    }
}
