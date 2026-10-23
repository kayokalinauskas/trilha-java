package dev.trilhajava.m12.aulas.rabbitmq;

/**
 * Gera o PDF do laudo de um exame de imagem (leva segundos). Lança LaudoInvalidoException
 * quando o exame nunca vai gerar laudo (falha permanente); qualquer outra exceção é tratada
 * como transitória.
 */
public interface GeradorDeLaudo {

    void gerar(ExameConcluido exame);
}
