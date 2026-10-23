package dev.trilhajava.m03.metricas;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

/** Solução de referência do exercício 03.04. */
public final class Estatisticas {

    private Estatisticas() {
    }

    /**
     * List<Integer> NÃO é subtipo de Collection<Number> (generics são invariantes).
     * Com "? extends Number" aceitamos Collection<Integer>, Collection<Double>... e só LEMOS dela.
     */
    public static double media(Collection<? extends Number> valores) {
        if (valores == null || valores.isEmpty()) {
            throw new IllegalArgumentException("É preciso ao menos um valor");
        }
        double soma = 0;
        for (Number n : valores) {
            soma += n.doubleValue();
        }
        return soma / valores.size();
    }

    /**
     * LocalDate implementa Comparable<ChronoLocalDate>, não Comparable<LocalDate>.
     * Com "Comparable<? super T>" aceitamos tipos que se comparam com algum SUPERTIPO deles.
     * É a mesma assinatura usada por Collections.max na JDK.
     */
    public static <T extends Comparable<? super T>> T maximo(Collection<? extends T> valores) {
        Iterator<? extends T> it = valores.iterator();
        if (!it.hasNext()) {
            throw new NoSuchElementException("Coleção vazia não tem máximo");
        }
        T maior = it.next();
        while (it.hasNext()) {
            T atual = it.next();
            if (atual.compareTo(maior) > 0) {
                maior = atual;
            }
        }
        return maior;
    }

    /** PECS: a origem PRODUZ valores (extends), o destino CONSOME (super). */
    public static void copiarAcimaDe(Collection<? extends Number> origem, Collection<? super Number> destino, double limite) {
        for (Number n : origem) {
            if (n.doubleValue() > limite) {
                destino.add(n);
            }
        }
    }

    public static Map<String, Double> mediaPorSensor(Collection<? extends Leitura<? extends Number>> leituras) {
        Map<String, double[]> acumulado = new TreeMap<>(); // [soma, quantidade]
        for (Leitura<? extends Number> l : leituras) {
            double[] acc = acumulado.computeIfAbsent(l.sensor(), k -> new double[2]);
            acc[0] += l.valor().doubleValue();
            acc[1]++;
        }
        Map<String, Double> medias = new TreeMap<>();
        for (Map.Entry<String, double[]> e : acumulado.entrySet()) {
            medias.put(e.getKey(), e.getValue()[0] / e.getValue()[1]);
        }
        return medias;
    }
}
