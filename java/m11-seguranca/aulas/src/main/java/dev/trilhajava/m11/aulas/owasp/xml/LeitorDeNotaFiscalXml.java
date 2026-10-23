package dev.trilhajava.m11.aulas.owasp.xml;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

/**
 * Lê o XML de uma nota fiscal enviado por um fornecedor e devolve o nome do emitente.
 * Duas fábricas de parser: a padrão (vulnerável a XXE) e a endurecida.
 */
public final class LeitorDeNotaFiscalXml {

    private final DocumentBuilderFactory fabrica;

    private LeitorDeNotaFiscalXml(DocumentBuilderFactory fabrica) {
        this.fabrica = fabrica;
    }

    /** VULNERÁVEL DE PROPÓSITO (A02, CWE-611). Configuração padrão: aceita DOCTYPE e entidades externas. */
    public static LeitorDeNotaFiscalXml padrao() {
        return new LeitorDeNotaFiscalXml(DocumentBuilderFactory.newInstance());
    }

    /** A recomendação do OWASP XXE Prevention Cheat Sheet: proibir DOCTYPE por completo. */
    public static LeitorDeNotaFiscalXml endurecido() throws ParserConfigurationException {
        var fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        fabrica.setXIncludeAware(false);
        fabrica.setExpandEntityReferences(false);
        return new LeitorDeNotaFiscalXml(fabrica);
    }

    public String emitente(String xml) throws Exception {
        Document documento = fabrica.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        return documento.getElementsByTagName("emitente").item(0).getTextContent();
    }
}
