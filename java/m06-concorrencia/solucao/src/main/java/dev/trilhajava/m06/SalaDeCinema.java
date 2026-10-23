package dev.trilhajava.m06;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Solução de referência do exercício 06.02.
 *
 * Um único lock protege o array inteiro: simples e correto. A reserva múltipla precisa enxergar
 * VÁRIOS assentos de forma consistente, e com um lock por assento teríamos de adquirir vários locks
 * sempre na mesma ordem para evitar deadlock. Para uma sala de cinema, a disputa é baixa e o lock
 * único é a escolha certa. Em sistemas reais, isso vira uma transação no banco (módulo 09).
 */
public class SalaDeCinema {

    private final String[] ocupantes;
    private final ReentrantLock lock = new ReentrantLock();
    private int livres;

    public SalaDeCinema(int totalAssentos) {
        if (totalAssentos < 1) {
            throw new IllegalArgumentException("A sala precisa de pelo menos 1 assento");
        }
        this.ocupantes = new String[totalAssentos];
        this.livres = totalAssentos;
    }

    public boolean reservar(int assento, String cliente) {
        return reservarVarios(Set.of(assento), cliente);
    }

    public boolean reservarVarios(Set<Integer> assentos, String cliente) {
        assentos.forEach(this::validar);
        lock.lock();
        try {
            // check-then-act DENTRO do lock: nenhuma outra thread intercala entre verificar e reservar.
            for (int a : assentos) {
                if (ocupantes[a] != null) {
                    return false;
                }
            }
            for (int a : assentos) {
                ocupantes[a] = cliente;
            }
            livres -= assentos.size();
            return true;
        } finally {
            lock.unlock(); // sempre em finally: uma exceção não pode deixar o lock preso
        }
    }

    public Optional<String> ocupante(int assento) {
        validar(assento);
        lock.lock();
        try {
            return Optional.ofNullable(ocupantes[assento]); // leitura também sob lock: garante visibilidade
        } finally {
            lock.unlock();
        }
    }

    public int livres() {
        lock.lock();
        try {
            return livres;
        } finally {
            lock.unlock();
        }
    }

    private void validar(int assento) {
        if (assento < 0 || assento >= ocupantes.length) {
            throw new IllegalArgumentException("Assento inexistente: " + assento);
        }
    }
}
