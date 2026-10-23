package dev.trilhajava.m11.aulas.senhas;

import java.util.ArrayList;
import java.util.List;

/** Fake da caixa de saída: guarda os e-mails numa lista para o teste conferir. */
class CaixaDeEmailEmMemoria implements CaixaDeEmail {

    record Email(String para, String assunto, String corpo) {
    }

    final List<Email> enviados = new ArrayList<>();

    @Override
    public void enviar(String para, String assunto, String corpo) {
        enviados.add(new Email(para, assunto, corpo));
    }

    Email ultimo() {
        return enviados.getLast();
    }
}
