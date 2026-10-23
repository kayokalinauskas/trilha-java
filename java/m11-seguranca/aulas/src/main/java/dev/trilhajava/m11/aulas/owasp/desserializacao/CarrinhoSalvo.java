package dev.trilhajava.m11.aulas.owasp.desserializacao;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Um carrinho guardado com serialização nativa do Java (num cache antigo, por exemplo).
 * Mostra as duas formas de ler os bytes de volta: sem filtro (vulnerável) e com ObjectInputFilter.
 */
public record CarrinhoSalvo(String cliente, List<String> skus, BigDecimal total) implements Serializable {

    /** Só estes tipos podem aparecer no fluxo; qualquer outro é rejeitado ANTES de ser instanciado. */
    static final ObjectInputFilter FILTRO = ObjectInputFilter.Config.createFilter(
            "maxdepth=5;maxrefs=1000;maxbytes=65536;"
                    + "dev.trilhajava.m11.aulas.owasp.desserializacao.CarrinhoSalvo;"
                    // List.of(...) viaja como CollSer + Object[] e volta como List12/ListN.
                    // Para arrays, o filtro confere o tipo do ELEMENTO: Object[] casa com "java.lang.Object".
                    + "java.util.CollSer;java.lang.Object;"
                    + "java.util.ImmutableCollections$List12;java.util.ImmutableCollections$ListN;"
                    + "java.math.BigDecimal;java.math.BigInteger;java.lang.Number;"
                    + "!*");

    /** VULNERÁVEL DE PROPÓSITO: instancia qualquer classe Serializable que estiver nos bytes. */
    public static Object lerSemFiltro(byte[] bytes) throws IOException, ClassNotFoundException {
        try (var entrada = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return entrada.readObject();
        }
    }

    public static CarrinhoSalvo lerComFiltro(byte[] bytes) throws IOException, ClassNotFoundException {
        try (var entrada = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            entrada.setObjectInputFilter(FILTRO);
            return (CarrinhoSalvo) entrada.readObject();
        }
    }
}
