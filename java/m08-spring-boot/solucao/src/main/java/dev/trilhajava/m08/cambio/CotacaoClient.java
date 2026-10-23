package dev.trilhajava.m08.cambio;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * HTTP interface: você declara o contrato e o Spring gera a implementação (como um repositório
 * do Spring Data, mas para HTTP). Nada de montar URL, headers e JSON à mão.
 */
@HttpExchange(accept = "application/json")
public interface CotacaoClient {

    @GetExchange("/cotacoes/{moeda}")
    Cotacao buscar(@PathVariable String moeda);
}
