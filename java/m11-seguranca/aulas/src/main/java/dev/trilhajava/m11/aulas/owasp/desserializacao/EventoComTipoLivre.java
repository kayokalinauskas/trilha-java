package dev.trilhajava.m11.aulas.owasp.desserializacao;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * VULNERÁVEL DE PROPÓSITO (A08, CWE-502). Não copie.
 * Um evento de webhook em que o CLIENTE diz qual classe Java instanciar para "detalhes".
 */
public class EventoComTipoLivre {

    public String pedido;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    public Object detalhes;
}
