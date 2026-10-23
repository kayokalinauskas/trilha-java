package dev.trilhajava.m10.aulas.reembolso;

/** Onde os reembolsos concluídos ficam registrados. */
public interface RepositorioDeReembolsos {

    void salvar(Reembolso reembolso);
}
