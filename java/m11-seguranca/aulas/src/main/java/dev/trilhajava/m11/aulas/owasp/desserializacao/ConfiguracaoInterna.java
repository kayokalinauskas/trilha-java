package dev.trilhajava.m11.aulas.owasp.desserializacao;

import java.io.Serializable;

/**
 * Uma classe qualquer do classpath, que NUNCA deveria nascer de dados do cliente.
 * Aqui ela é inofensiva; em ataques reais, o papel dela é de classes de bibliotecas cujo
 * construtor, setter ou readObject faz algo perigoso (os "gadgets").
 */
public class ConfiguracaoInterna implements Serializable {

    private static final long serialVersionUID = 1L;

    public String chave;
    public boolean modoAdministrador;
}
