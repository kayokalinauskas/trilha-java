package dev.trilhajava.m08.comissao;

public class CanalDesconhecidoException extends RuntimeException {

    public CanalDesconhecidoException(String canal) {
        super("Nenhuma regra de comissão para o canal: " + canal);
    }
}
