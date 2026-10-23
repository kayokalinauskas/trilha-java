package dev.trilhajava.m08.pacientes;

import org.springframework.web.bind.annotation.RestController;

/**
 * Exercício 08.02 — implemente a API descrita no enunciado:
 *
 *   POST   /pacientes              201 + Location | 400 dados inválidos | 409 CPF já cadastrado
 *   GET    /pacientes/{id}         200 | 404
 *   GET    /pacientes?nome=&pagina=&tamanho=   200 com { itens, pagina, tamanho, totalItens, totalPaginas }
 *   PUT    /pacientes/{id}         200 | 400 | 404 | 409
 *   DELETE /pacientes/{id}         204 | 404
 *
 * Sugestão de organização (crie as classes que precisar neste pacote):
 *   DadosPaciente (record de entrada com Bean Validation), PacienteResposta (record de saída, CPF mascarado),
 *   @CpfValido + ValidadorDeCpf, ServicoDePacientes, RepositorioDePacientes (em memória, thread-safe).
 */
@RestController
public class PacienteController {
}
