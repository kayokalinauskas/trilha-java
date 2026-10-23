package dev.trilhajava.m10.aulas.desempenho;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CotacaoDeFreteController {

    private final ServicoDeCotacao servico;

    public CotacaoDeFreteController(ServicoDeCotacao servico) {
        this.servico = servico;
    }

    @GetMapping("/v1/fretes/cotacao")
    public Cotacao cotarV1(@RequestParam String cep) {
        return servico.cotarComTransacaoLonga(cep);
    }

    @GetMapping("/v2/fretes/cotacao")
    public Cotacao cotarV2(@RequestParam String cep) {
        return servico.cotarSemSegurarConexao(cep);
    }
}
