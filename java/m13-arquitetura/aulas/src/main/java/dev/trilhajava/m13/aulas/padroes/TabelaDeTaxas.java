package dev.trilhajava.m13.aulas.padroes;

import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * O decorator que o Spring escreve para você: com {@code @Cacheable}, o bean injetado é um
 * proxy que consulta o cache antes de chamar este objeto. Mas só quem chama PELO proxy
 * passa pelo cache. A chamada interna em {@link #simular} vai direto ao método.
 */
public class TabelaDeTaxas {

    private final AtomicInteger consultasAoBanco = new AtomicInteger();

    @Cacheable("taxas")
    public BigDecimal percentualVigente(String meio) {
        consultasAoBanco.incrementAndGet(); // imagine aqui um SELECT na tabela de taxas
        return switch (meio) {
            case "PIX" -> new BigDecimal("0.99");
            case "CARTAO" -> new BigDecimal("2.49");
            case "BOLETO" -> BigDecimal.ZERO;
            default -> throw new IllegalArgumentException("meio desconhecido: " + meio);
        };
    }

    public List<BigDecimal> simular(List<String> meios) {
        return meios.stream().map(this::percentualVigente).toList(); // this: não passa pelo proxy
    }

    public int consultasAoBanco() {
        return consultasAoBanco.get();
    }
}
