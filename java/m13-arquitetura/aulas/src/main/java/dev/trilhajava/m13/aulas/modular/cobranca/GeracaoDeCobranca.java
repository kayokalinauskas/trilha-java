package dev.trilhajava.m13.aulas.modular.cobranca;

import dev.trilhajava.m13.aulas.modular.agenda.eventos.ConsultaRealizada;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Reage à consulta realizada: pede a autorização à operadora e registra a cobrança.
 * Se a operadora falhar, a exceção sobe, a publicação fica incompleta no registro e pode
 * ser reenviada. Por isso o ouvinte é idempotente: no reenvio, não cobra duas vezes.
 */
@Component
class GeracaoDeCobranca {

    private final OperadoraDeConvenio operadora;
    private final Cobrancas cobrancas;

    GeracaoDeCobranca(OperadoraDeConvenio operadora, Cobrancas cobrancas) {
        this.operadora = operadora;
        this.cobrancas = cobrancas;
    }

    @ApplicationModuleListener
    void quandoRealizada(ConsultaRealizada evento) {
        if (cobrancas.daConsulta(evento.consulta()).isPresent()) {
            return;   // reentrega de um evento já tratado
        }
        String autorizacao = operadora.autorizar(evento.consulta(), evento.valor());
        cobrancas.registrar(new Cobranca(evento.consulta(), evento.paciente(), evento.valor(), autorizacao));
    }
}
