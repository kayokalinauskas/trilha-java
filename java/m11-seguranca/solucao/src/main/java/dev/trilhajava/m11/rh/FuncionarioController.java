package dev.trilhajava.m11.rh;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/funcionarios")
public class FuncionarioController {

    static final String PROBLEMAS = "https://api.rh.dev/problemas/";

    public record NovoSalario(@NotNull @Positive @DecimalMax("1000000.00") BigDecimal salario) {}

    private final ServicoDeFuncionarios servico;

    public FuncionarioController(ServicoDeFuncionarios servico) {
        this.servico = servico;
    }

    @GetMapping
    public List<FuncionarioResposta> listar(Authentication quem) {
        return servico.listar(quem);
    }

    @GetMapping("/{id}")
    public FuncionarioResposta buscar(@PathVariable long id, Authentication quem) {
        return servico.buscar(id, quem);
    }

    @PutMapping("/{id}/salario")
    public FuncionarioResposta alterarSalario(@PathVariable long id, @Valid @RequestBody NovoSalario corpo,
            Authentication quem) {
        return servico.alterarSalario(id, corpo.salario(), quem);
    }

    @ExceptionHandler
    ProblemDetail naoEncontrado(FuncionarioNaoEncontradoException e) {
        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Funcionário não encontrado");
        problema.setType(URI.create(PROBLEMAS + "nao-encontrado"));
        return problema;
    }

    @ExceptionHandler
    ProblemDetail naoPermitida(OperacaoNaoPermitidaException e) {
        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
        problema.setType(URI.create(PROBLEMAS + "operacao-nao-permitida"));
        return problema;
    }
}
