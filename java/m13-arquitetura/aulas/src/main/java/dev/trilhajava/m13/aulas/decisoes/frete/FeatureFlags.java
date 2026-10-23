package dev.trilhajava.m13.aulas.decisoes.frete;

/** De quantos por cento dos clientes uma flag está ligada (0 a 100). Em produção, vem de um serviço de flags. */
public interface FeatureFlags {

    int percentualAtivo(String flag);
}
