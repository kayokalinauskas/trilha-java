package dev.trilhajava.m11.aulas.autorizacao;

/** Os papéis da API. No token, vêm na claim "papeis"; no Spring Security, viram ROLE_PACIENTE etc. */
public enum Papel {
    PACIENTE,
    MEDICO,
    ATENDENTE
}
