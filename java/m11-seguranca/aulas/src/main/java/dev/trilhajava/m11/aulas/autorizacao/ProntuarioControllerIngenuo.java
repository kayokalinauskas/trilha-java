package dev.trilhajava.m11.aulas.autorizacao;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * NÃO COPIE. A primeira versão da API, com os dois bugs mais comuns: BOLA no GET e mass assignment no POST.
 * Fica no projeto, em /v0, para os testes demonstrarem a falha. Tem autenticação (exige JWT válido) e só isso.
 */
@RestController
@RequestMapping("/v0/prontuarios")
public class ProntuarioControllerIngenuo {

    private final RepositorioDeProntuarios repositorio;

    public ProntuarioControllerIngenuo(RepositorioDeProntuarios repositorio) {
        this.repositorio = repositorio;
    }

    @GetMapping("/{id}")
    public Prontuario buscar(@PathVariable long id) {
        return repositorio.porId(id)                               // qualquer id, de qualquer clínica
                .orElseThrow(() -> new ProntuarioNaoEncontradoException(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Prontuario criar(@RequestBody Prontuario corpo) {          // o JSON vira a entidade inteira
        return repositorio.salvar(corpo);
    }
}
