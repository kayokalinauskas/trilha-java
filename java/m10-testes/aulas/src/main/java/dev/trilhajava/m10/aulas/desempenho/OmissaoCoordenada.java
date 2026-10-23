package dev.trilhajava.m10.aulas.desempenho;

import java.util.ArrayList;
import java.util.List;

/**
 * Uma simulação determinística (sem rede, sem threads) da "omissão coordenada".
 *
 * <p>O cenário: um cliente quer mandar uma requisição a cada 10 ms durante 60 s. O servidor
 * responde em 2 ms, exceto entre os segundos 30 e 33, quando fica congelado (uma pausa longa de GC,
 * por exemplo): tudo o que chega nesse intervalo só é respondido aos 33,002 s.
 *
 * <p>As duas medições olham para o MESMO servidor e diferem só em quando o relógio começa a contar.
 */
public final class OmissaoCoordenada {

    static final long INTERVALO_MS = 10;
    static final long DURACAO_MS = 60_000;
    static final long ATENDIMENTO_MS = 2;
    static final long INICIO_DA_PAUSA_MS = 30_000;
    static final long FIM_DA_PAUSA_MS = 33_000;

    private OmissaoCoordenada() {
    }

    /** Em que instante sai a resposta de uma requisição que chega ao servidor em {@code chegada}. */
    static long respostaPara(long chegada) {
        if (chegada >= INICIO_DA_PAUSA_MS && chegada < FIM_DA_PAUSA_MS) {
            return FIM_DA_PAUSA_MS + ATENDIMENTO_MS;
        }
        return chegada + ATENDIMENTO_MS;
    }

    /**
     * Cliente "fechado": só envia a próxima requisição depois de receber a anterior. Enquanto
     * espera, os envios previstos para aquele intervalo simplesmente não acontecem, e ninguém
     * mede a espera que eles teriam sofrido.
     */
    public static long[] medicaoDoClienteFechado() {
        List<Long> latencias = new ArrayList<>();
        long proximoEnvio = 0;
        while (proximoEnvio < DURACAO_MS) {
            long resposta = respostaPara(proximoEnvio);
            latencias.add(resposta - proximoEnvio);
            long proximoHorario = proximoEnvio + INTERVALO_MS;
            while (proximoHorario < resposta) {
                proximoHorario += INTERVALO_MS; // os horários perdidos são pulados
            }
            proximoEnvio = proximoHorario;
        }
        return latencias.stream().mapToLong(Long::longValue).toArray();
    }

    /**
     * Medição a partir do horário pretendido: cada requisição tinha hora marcada para sair, e a
     * latência conta desde essa hora, como um usuário de verdade perceberia.
     */
    public static long[] medicaoPeloHorarioPretendido() {
        int total = (int) (DURACAO_MS / INTERVALO_MS);
        long[] latencias = new long[total];
        for (int i = 0; i < total; i++) {
            long horarioPretendido = i * INTERVALO_MS;
            latencias[i] = respostaPara(horarioPretendido) - horarioPretendido;
        }
        return latencias;
    }

    public static void main(String[] args) {
        System.out.println("Cliente fechado:     " + ResumoDeLatencias.de(medicaoDoClienteFechado()));
        System.out.println("Horário pretendido:  " + ResumoDeLatencias.de(medicaoPeloHorarioPretendido()));
    }
}
