package dev.trilhajava.m09.imoveis;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;

/**
 * Cada filtro é uma Specification pequena e testável. Filtro ausente → unrestricted() (não restringe).
 * Os valores viram PARÂMETROS da consulta: nada de concatenar texto em SQL/JPQL.
 */
final class EspecificacoesDeImovel {

    private EspecificacoesDeImovel() {
    }

    static Specification<Imovel> cidade(String cidade) {
        if (cidade == null || cidade.isBlank()) {
            return Specification.unrestricted();
        }
        return (raiz, consulta, cb) -> cb.equal(cb.lower(raiz.get("cidade")), cidade.strip().toLowerCase(Locale.ROOT));
    }

    static Specification<Imovel> tipo(TipoImovel tipo) {
        return tipo == null ? Specification.unrestricted() : (raiz, consulta, cb) -> cb.equal(raiz.get("tipo"), tipo);
    }

    static Specification<Imovel> precoEntre(BigDecimal minimo, BigDecimal maximo) {
        Specification<Imovel> spec = Specification.unrestricted();
        if (minimo != null) {
            spec = spec.and((raiz, consulta, cb) -> cb.greaterThanOrEqualTo(raiz.get("preco"), minimo));
        }
        if (maximo != null) {
            spec = spec.and((raiz, consulta, cb) -> cb.lessThanOrEqualTo(raiz.get("preco"), maximo));
        }
        return spec;
    }

    static Specification<Imovel> quartosNoMinimo(Integer quartos) {
        return quartos == null ? Specification.unrestricted()
                : (raiz, consulta, cb) -> cb.greaterThanOrEqualTo(raiz.get("quartos"), quartos);
    }

    static Specification<Imovel> garagem(Boolean comGaragem) {
        if (comGaragem == null) {
            return Specification.unrestricted();
        }
        return comGaragem
                ? (raiz, consulta, cb) -> cb.greaterThan(raiz.get("vagasGaragem"), 0)
                : (raiz, consulta, cb) -> cb.equal(raiz.get("vagasGaragem"), 0);
    }

    static Specification<Imovel> bairros(Set<String> bairros) {
        return bairros == null || bairros.isEmpty() ? Specification.unrestricted()
                : (raiz, consulta, cb) -> raiz.get("bairro").in(bairros);
    }

    static Specification<Imovel> de(FiltroDeImoveis f) {
        return Specification.allOf(
                cidade(f.cidade()),
                tipo(f.tipo()),
                precoEntre(f.precoMin(), f.precoMax()),
                quartosNoMinimo(f.quartosMin()),
                garagem(f.comGaragem()),
                bairros(f.bairros()));
    }
}
