package dev.trilhajava.m11.aulas.owasp.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.SAXParseException;

class XxeTest {

    static final String NOTA_NORMAL = "<nota><emitente>Café do Vale Ltda</emitente></nota>";

    @TempDir
    Path pasta;

    String notaComEntidadeExterna() throws Exception {
        Path segredo = pasta.resolve("segredo.txt");
        Files.writeString(segredo, "DB_PASSWORD=senha-do-banco");
        return """
                <?xml version="1.0"?>
                <!DOCTYPE nota [ <!ENTITY conteudo SYSTEM "%s"> ]>
                <nota><emitente>&conteudo;</emitente></nota>
                """.formatted(segredo.toUri());
    }

    @Test
    void notaNormalFuncionaNasDuasConfiguracoes() throws Exception {
        assertThat(LeitorDeNotaFiscalXml.padrao().emitente(NOTA_NORMAL)).isEqualTo("Café do Vale Ltda");
        assertThat(LeitorDeNotaFiscalXml.endurecido().emitente(NOTA_NORMAL)).isEqualTo("Café do Vale Ltda");
    }

    @Test
    void parserPadraoLeArquivoDoServidor() throws Exception {
        assertThat(LeitorDeNotaFiscalXml.padrao().emitente(notaComEntidadeExterna()))
                .isEqualTo("DB_PASSWORD=senha-do-banco");
    }

    @Test
    void parserEndurecidoRecusaODoctype() throws Exception {
        String xml = notaComEntidadeExterna();
        assertThatThrownBy(() -> LeitorDeNotaFiscalXml.endurecido().emitente(xml))
                .isInstanceOf(SAXParseException.class)
                .hasMessageContaining("http://apache.org/xml/features/disallow-doctype-decl"); // o texto em volta segue o idioma da JVM
    }
}
