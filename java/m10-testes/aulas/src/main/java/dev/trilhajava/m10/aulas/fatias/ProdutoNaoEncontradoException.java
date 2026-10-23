package dev.trilhajava.m10.aulas.fatias;

public class ProdutoNaoEncontradoException extends RuntimeException {

    public ProdutoNaoEncontradoException(String sku) {
        super("Não existe produto com o SKU " + sku);
    }
}
