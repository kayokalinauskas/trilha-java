package dev.trilhajava.m09.matriculas;

import java.time.Instant;

/** Projeção para a lista de chamada: só o que a tela precisa, direto da consulta. */
public record AlunoMatriculado(String nome, String email, Instant matriculadaEm) {
}
