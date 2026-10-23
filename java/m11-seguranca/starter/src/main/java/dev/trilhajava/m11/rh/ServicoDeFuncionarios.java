package dev.trilhajava.m11.rh;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Exercício 11.03 — autorização no nível do objeto: quem vê quem, quem vê salários, quem altera salários.
 *
 * <p>Os requisitos e as regras estão no enunciado, no site. O {@link FuncionarioController} já chama os três
 * métodos abaixo; por isso eles existem desde o início, com um retorno provisório que você substitui.
 */
@Service
public class ServicoDeFuncionarios {

    // Implemente aqui o exercício

    public List<FuncionarioResposta> listar(Authentication quem) {
        return List.of();
    }

    public FuncionarioResposta buscar(long id, Authentication quem) {
        return null;
    }

    public FuncionarioResposta alterarSalario(long id, BigDecimal novoSalario, Authentication quem) {
        return null;
    }
}
