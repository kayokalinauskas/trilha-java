package dev.trilhajava.m08.reservas;

import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exercício 08.03 — padronize TODOS os erros da API no formato Problem Details (RFC 9457).
 * Rode: mvn test -Dtest=ErrosPadronizadosTest
 *
 * Tabela de tipos (type = https://api.hotel.dev/problemas/<slug>):
 *   ReservaNaoEncontradaException    404  reserva-nao-encontrada   "Reserva não encontrada"
 *   dados inválidos (Bean Validation) 400  dados-invalidos          "Dados inválidos"  + erros: [{campo, mensagem}]
 *   JSON malformado / tipo errado     400  requisicao-malformada    "Requisição malformada"
 *   QuartoIndisponivelException       409  quarto-indisponivel      "Quarto indisponível" + quartoId
 *   PeriodoInvalidoException          422  periodo-invalido         "Período inválido"
 *   qualquer outra exceção            500  erro-interno             "Erro interno" + codigoRastreio
 *
 * Dica: pesquise ProblemDetail, @ExceptionHandler e ResponseEntityExceptionHandler.
 */
@RestControllerAdvice
public class TratadorDeErros {
}
