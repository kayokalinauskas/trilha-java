package dev.trilhajava.m10.aulas.arquitetura.checkpoint.consultorio.aplicacao;

import java.time.LocalDate;

/**
 * Checkpoint da aula 10.06: um caso de uso fora da convenção do time.
 * Não tem @Service e o nome termina em "Service". A sua regra precisa acusar as duas coisas.
 */
public class ConsultaService {

    public boolean atendeNoDia(LocalDate dia) {
        return dia.getDayOfWeek().getValue() <= 5;
    }
}
