package dev.trilhajava.m13.frete;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Cota todas as modalidades e aplica as promoções, em ordem. Não conhece nenhuma modalidade nem
 * promoção concreta: o Spring injeta todos os beans das duas interfaces. Uma modalidade nova é uma
 * classe nova, sem mudar esta (princípio aberto/fechado).
 */
@Service
public class MotorDeFrete {

    private static final Comparator<Cotacao> MAIS_BARATA_PRIMEIRO = Comparator.comparing(Cotacao::valor)
            .thenComparingInt(Cotacao::prazoEmDiasUteis)
            .thenComparing(Cotacao::modalidade);

    private final List<ModalidadeDeFrete> modalidades;
    private final List<RegraDePromocao> promocoes;

    /** As promoções chegam na ordem do @Order de cada uma. */
    public MotorDeFrete(List<ModalidadeDeFrete> modalidades, List<RegraDePromocao> promocoes) {
        this.modalidades = List.copyOf(modalidades);
        this.promocoes = List.copyOf(promocoes);
    }

    public List<Cotacao> cotar(Pedido pedido) {
        return modalidades.stream()
                .flatMap(modalidade -> modalidade.cotar(pedido).stream())
                .map(cotacao -> comPromocoes(pedido, cotacao))
                .sorted(MAIS_BARATA_PRIMEIRO)
                .toList();
    }

    private Cotacao comPromocoes(Pedido pedido, Cotacao cotacao) {
        Cotacao resultado = cotacao;
        for (RegraDePromocao promocao : promocoes) {
            resultado = promocao.aplicar(pedido, resultado);
        }
        return resultado;
    }
}
