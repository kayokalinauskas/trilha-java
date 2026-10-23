package dev.trilhajava.m08.comissao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Escolhe a regra pelo canal, aplica o teto e arredonda.
 *
 * Ela não conhece nenhuma regra concreta: o Spring injeta todas as implementações de
 * RegraDeComissao que encontrar. Isso é o "O" do SOLID (aberto para extensão, fechado
 * para modificação) conseguido com injeção de dependências.
 */
@Service
public class CalculadoraDeComissao {

    private final Map<String, RegraDeComissao> regrasPorCanal;
    private final BigDecimal teto;

    public CalculadoraDeComissao(List<RegraDeComissao> regras, ComissaoProperties properties) {
        this.regrasPorCanal = regras.stream().collect(Collectors.toUnmodifiableMap(
                RegraDeComissao::canal,
                Function.identity(),
                (a, b) -> {
                    // Falha na SUBIDA da aplicação, não na primeira venda do canal.
                    throw new IllegalStateException("Duas regras para o canal " + a.canal() + ": "
                            + a.getClass().getSimpleName() + " e " + b.getClass().getSimpleName());
                }));
        this.teto = properties.tetoPorVenda();
    }

    public BigDecimal calcular(Venda venda) {
        Objects.requireNonNull(venda, "venda");
        if (venda.valor().signum() < 0) {
            throw new IllegalArgumentException("O valor da venda não pode ser negativo: " + venda.valor());
        }
        RegraDeComissao regra = regrasPorCanal.get(venda.canal());
        if (regra == null) {
            throw new CanalDesconhecidoException(venda.canal());
        }
        return regra.calcular(venda).min(teto).setScale(2, RoundingMode.HALF_UP);
    }

    /** Total de comissão por vendedor, em ordem alfabética de vendedor. */
    public Map<String, BigDecimal> totalPorVendedor(Collection<Venda> vendas) {
        return vendas.stream().collect(Collectors.groupingBy(
                Venda::vendedor,
                TreeMap::new,
                Collectors.reducing(BigDecimal.ZERO, this::calcular, BigDecimal::add)));
    }
}
