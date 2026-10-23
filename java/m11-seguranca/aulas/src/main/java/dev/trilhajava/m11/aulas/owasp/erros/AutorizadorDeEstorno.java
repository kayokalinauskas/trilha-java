package dev.trilhajava.m11.aulas.owasp.erros;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decide se um atendente pode estornar um valor, consultando o serviço de limites.
 * Duas políticas para quando o serviço de limites FALHA: abrir (vulnerável) ou fechar (correta).
 */
public class AutorizadorDeEstorno {

    /** O serviço remoto de limites por atendente. Pode lançar exceção (timeout, 503...). */
    @FunctionalInterface
    public interface ServicoDeLimites {
        BigDecimal limiteDe(String atendente);
    }

    private static final Logger log = LoggerFactory.getLogger(AutorizadorDeEstorno.class);

    private final ServicoDeLimites limites;
    private final boolean falharAberto;

    private AutorizadorDeEstorno(ServicoDeLimites limites, boolean falharAberto) {
        this.limites = limites;
        this.falharAberto = falharAberto;
    }

    /** VULNERÁVEL DE PROPÓSITO (A10, CWE-636): "se o serviço cair, não vamos travar o atendimento". */
    public static AutorizadorDeEstorno falhandoAberto(ServicoDeLimites limites) {
        return new AutorizadorDeEstorno(limites, true);
    }

    public static AutorizadorDeEstorno falhandoFechado(ServicoDeLimites limites) {
        return new AutorizadorDeEstorno(limites, false);
    }

    public boolean pode(String atendente, BigDecimal valor) {
        try {
            return valor.compareTo(limites.limiteDe(atendente)) <= 0;
        } catch (RuntimeException e) {
            log.warn("servico-de-limites-indisponivel atendente={} politica={}", atendente,
                    falharAberto ? "ABERTA" : "FECHADA", e);
            return falharAberto;
        }
    }
}
