package dev.trilhajava.m10.aulas.reembolso;

import java.util.ArrayList;
import java.util.List;

/** Fake: guarda os reembolsos numa lista e deixa o teste consultá-los. */
class RepositorioDeReembolsosEmMemoria implements RepositorioDeReembolsos {

    private final List<Reembolso> salvos = new ArrayList<>();

    @Override
    public void salvar(Reembolso reembolso) {
        salvos.add(reembolso);
    }

    List<Reembolso> todos() {
        return List.copyOf(salvos);
    }
}
