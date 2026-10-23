package dev.trilhajava.m11.aulas.autorizacao;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** A versão corrigida. O controller só traduz HTTP: a identidade vem do token, as regras moram no serviço. */
@RestController
@RequestMapping("/prontuarios")
public class ProntuarioController {

    private final ServicoDeProntuarios servico;
    private final ServicoDeExames exames;

    public ProntuarioController(ServicoDeProntuarios servico, ServicoDeExames exames) {
        this.servico = servico;
        this.exames = exames;
    }

    @GetMapping
    public List<ProntuarioResposta> listar(Authentication quem) {
        return servico.listar(Solicitante.de(quem));
    }

    @GetMapping("/{id}")
    public ProntuarioResposta buscar(@PathVariable long id, Authentication quem) {
        return servico.buscar(Solicitante.de(quem), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProntuarioResposta criar(@Valid @RequestBody NovoProntuario corpo, Authentication quem) {
        return servico.criar(Solicitante.de(quem), corpo);
    }

    @PutMapping("/{id}/anotacoes")
    public ProntuarioResposta anotar(@PathVariable long id, @Valid @RequestBody NovasAnotacoes corpo,
            Authentication quem) {
        return servico.atualizarAnotacoes(Solicitante.de(quem), id, corpo);
    }

    @GetMapping("/{id}/exames")
    public List<String> exames(@PathVariable long id) {
        return exames.exames(id);
    }
}
