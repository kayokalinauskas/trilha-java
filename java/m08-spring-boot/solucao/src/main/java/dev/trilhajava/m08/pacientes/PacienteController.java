package dev.trilhajava.m08.pacientes;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final ServicoDePacientes servico;

    public PacienteController(ServicoDePacientes servico) {
        this.servico = servico;
    }

    /** 201 Created + Location apontando para o recurso novo. */
    @PostMapping
    public ResponseEntity<PacienteResposta> cadastrar(@Valid @RequestBody DadosPaciente dados, UriComponentsBuilder uri) {
        Paciente paciente = servico.cadastrar(dados);
        URI location = uri.path("/pacientes/{id}").buildAndExpand(paciente.id()).toUri();
        return ResponseEntity.created(location).body(PacienteResposta.de(paciente));
    }

    @GetMapping("/{id}")
    public PacienteResposta buscar(@PathVariable long id) {
        return PacienteResposta.de(servico.buscar(id));
    }

    /** As anotações nos parâmetros ativam a validação de métodos do Spring MVC (400 se violadas). */
    @GetMapping
    public Pagina<PacienteResposta> listar(
            @RequestParam(defaultValue = "") String nome,
            @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int tamanho) {
        Pagina<Paciente> resultado = servico.listar(nome, pagina, tamanho);
        return new Pagina<>(
                resultado.itens().stream().map(PacienteResposta::de).toList(),
                resultado.pagina(), resultado.tamanho(), resultado.totalItens(), resultado.totalPaginas());
    }

    /** PUT substitui o recurso inteiro (idempotente: repetir dá o mesmo resultado). */
    @PutMapping("/{id}")
    public PacienteResposta atualizar(@PathVariable long id, @Valid @RequestBody DadosPaciente dados) {
        return PacienteResposta.de(servico.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable long id) {
        servico.remover(id);
    }
}
