package dev.trilhajava.m11.aulas.senhas;

import java.util.List;

/** A senha nova não passou na política. A mensagem lista os problemas, nunca a senha. */
public class SenhaRecusadaException extends RuntimeException {

    private final List<PoliticaDeSenha.Problema> problemas;

    public SenhaRecusadaException(List<PoliticaDeSenha.Problema> problemas) {
        super("Senha recusada: " + problemas);
        this.problemas = List.copyOf(problemas);
    }

    public List<PoliticaDeSenha.Problema> problemas() {
        return problemas;
    }
}
