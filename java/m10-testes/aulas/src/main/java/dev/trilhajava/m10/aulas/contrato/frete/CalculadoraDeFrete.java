package dev.trilhajava.m10.aulas.contrato.frete;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

/**
 * Regra de negócio do provedor, de propósito simples: o primeiro dígito do CEP dá a região.
 * <ul>
 *   <li>0 a 3 (Sudeste): R$ 15,90 + R$ 4,00 por quilo iniciado, 3 dias úteis.</li>
 *   <li>4 a 9: R$ 21,90 + R$ 6,00 por quilo iniciado, 7 dias úteis.</li>
 *   <li>CEPs que começam com 69 (parte da Região Norte): sem cobertura.</li>
 * </ul>
 */
@Service
public class CalculadoraDeFrete {

    public Cotacao cotar(PedidoDeCotacao pedido) {
        String cep = pedido.cepDestino();
        if (cep.startsWith("69")) {
            throw new CepSemCoberturaException(cep);
        }
        int quilosIniciados = (pedido.pesoEmGramas() + 999) / 1000;
        boolean sudeste = cep.charAt(0) <= '3';
        BigDecimal base = new BigDecimal(sudeste ? "15.90" : "21.90");
        BigDecimal porQuilo = new BigDecimal(sudeste ? "4.00" : "6.00");
        BigDecimal valor = base.add(porQuilo.multiply(BigDecimal.valueOf(quilosIniciados)));
        return new Cotacao(valor.toPlainString(), sudeste ? 3 : 7, "EXPRESSA", "Rápido Log");
    }
}
