package dev.trilhajava.m11.aulas.dados;

/**
 * O dado cifrado não pôde ser aberto: foi adulterado, foi copiado para outro campo, está truncado
 * ou usa uma versão de chave que o chaveiro não tem. Em todos os casos, o certo é falhar alto e
 * nunca devolver um valor "parecido".
 */
public class FalhaAoDecifrarException extends RuntimeException {

    public FalhaAoDecifrarException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
