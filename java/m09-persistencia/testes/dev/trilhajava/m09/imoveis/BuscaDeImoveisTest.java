package dev.trilhajava.m09.imoveis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;

/** O gabarito é calculado em Java puro sobre os dados lidos com SQL (sem passar pelo seu código). */
@SpringBootTest
@DisplayName("Exercício 09.04 — Busca de imóveis com Specifications, paginação e ordenação")
class BuscaDeImoveisTest {

    record Linha(long id, String tipo, String cidade, String bairro, BigDecimal preco, int area, int quartos,
            int vagas, LocalDate publicado) {
    }

    static final Comparator<Linha> RECENTES = Comparator.comparing(Linha::publicado).reversed().thenComparing(Linha::id);
    static final Comparator<Linha> MENOR_PRECO = Comparator.comparing(Linha::preco).thenComparing(Linha::id);
    static final Comparator<Linha> MAIOR_AREA = Comparator.comparingInt(Linha::area).reversed().thenComparing(Linha::id);

    @Autowired
    BuscaDeImoveis busca;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    EntityManagerFactory emf;

    List<Linha> todos;
    Statistics estatisticas;

    @BeforeEach
    void carregar() {
        todos = jdbc.query("select * from imovel", (rs, n) -> new Linha(rs.getLong("id"), rs.getString("tipo"),
                rs.getString("cidade"), rs.getString("bairro"), rs.getBigDecimal("preco"), rs.getInt("area_m2"),
                rs.getInt("quartos"), rs.getInt("vagas_garagem"), rs.getObject("publicado_em", LocalDate.class)));
        estatisticas = emf.unwrap(SessionFactory.class).getStatistics();
        estatisticas.clear();
    }

    List<Long> esperado(Predicate<Linha> filtro, Comparator<Linha> ordem) {
        return todos.stream().filter(filtro).sorted(ordem).map(Linha::id).toList();
    }

    static List<Long> ids(Page<ImovelResumo> pagina) {
        return pagina.getContent().stream().map(ImovelResumo::id).toList();
    }

    static FiltroDeImoveis filtro(String cidade, TipoImovel tipo, String min, String max, Integer quartos,
            Boolean garagem, Set<String> bairros) {
        return new FiltroDeImoveis(cidade, tipo, min == null ? null : new BigDecimal(min),
                max == null ? null : new BigDecimal(max), quartos, garagem, bairros);
    }

    @Test
    @DisplayName("sem filtros: todos os imóveis, os mais recentes primeiro, paginados, em até 2 comandos SQL")
    void semFiltros() {
        Page<ImovelResumo> p = busca.buscar(FiltroDeImoveis.vazio(), 0, 10, null);

        assertThat(ids(p)).isEqualTo(esperado(l -> true, RECENTES).subList(0, 10));
        assertThat(p.getTotalElements()).isEqualTo(36);
        assertThat(p.getTotalPages()).isEqualTo(4);
        assertThat(estatisticas.getPrepareStatementCount()).as("comandos SQL (consulta + contagem)").isLessThanOrEqualTo(2);
        ImovelResumo primeiro = p.getContent().getFirst();
        assertThat(primeiro.titulo()).isNotBlank();
        assertThat(primeiro.preco()).isPositive();
    }

    @Test
    @DisplayName("cidade (sem diferenciar maiúsculas) + tipo + preço máximo, do menor preço ao maior")
    void cidadeTipoPreco() {
        Page<ImovelResumo> p = busca.buscar(filtro("são paulo", TipoImovel.APARTAMENTO, null, "700000", null, null, null),
                0, 50, "menor-preco");

        List<Long> esperado = esperado(l -> l.cidade().toLowerCase(Locale.ROOT).equals("são paulo")
                && l.tipo().equals("APARTAMENTO") && l.preco().compareTo(new BigDecimal("700000")) <= 0, MENOR_PRECO);
        assertThat(esperado).as("o cenário precisa ter resultados").isNotEmpty();
        assertThat(ids(p)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("quartos mínimos + com garagem, da maior área à menor")
    void quartosGaragem() {
        Page<ImovelResumo> p = busca.buscar(filtro(null, null, null, null, 3, true, null), 0, 50, "maior-area");
        List<Long> esperado = esperado(l -> l.quartos() >= 3 && l.vagas() > 0, MAIOR_AREA);
        assertThat(esperado).isNotEmpty();
        assertThat(ids(p)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("sem garagem + faixa de preço")
    void semGaragem() {
        Page<ImovelResumo> p = busca.buscar(filtro(null, null, "300000", "600000", null, false, null), 0, 50, "menor-preco");
        List<Long> esperado = esperado(l -> l.vagas() == 0 && l.preco().compareTo(new BigDecimal("300000")) >= 0
                && l.preco().compareTo(new BigDecimal("600000")) <= 0, MENOR_PRECO);
        assertThat(esperado).isNotEmpty();
        assertThat(ids(p)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("lista de bairros, do maior preço ao menor")
    void bairros() {
        Page<ImovelResumo> p = busca.buscar(filtro(null, null, null, null, null, null, Set.of("Pinheiros", "Botafogo", "Batel")),
                0, 50, "maior-preco");
        List<Long> esperado = esperado(l -> Set.of("Pinheiros", "Botafogo", "Batel").contains(l.bairro()),
                Comparator.comparing(Linha::preco).reversed().thenComparing(Linha::id));
        assertThat(esperado).isNotEmpty();
        assertThat(ids(p)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("paginação: a última página traz o restante e a ordem é estável entre páginas")
    void paginacao() {
        List<Long> todosOrdenados = esperado(l -> true, MENOR_PRECO);
        Page<ImovelResumo> ultima = busca.buscar(FiltroDeImoveis.vazio(), 3, 10, "menor-preco");
        assertThat(ids(ultima)).isEqualTo(todosOrdenados.subList(30, 36));
        assertThat(ultima.isLast()).isTrue();
        assertThat(ids(busca.buscar(FiltroDeImoveis.vazio(), 1, 10, "menor-preco"))).isEqualTo(todosOrdenados.subList(10, 20));
    }

    @Test
    @DisplayName("texto malicioso no filtro é tratado como dado: nenhum resultado")
    void injecao() {
        Page<ImovelResumo> p = busca.buscar(filtro("x' or '1'='1", null, null, null, null, null, null), 0, 10, null);
        assertThat(p.getTotalElements()).isZero();
    }

    @ParameterizedTest(name = "ordenação \"{0}\" → IllegalArgumentException")
    @ValueSource(strings = {"preco", "titulo desc", "id; drop table imovel"})
    @DisplayName("ordenação fora da lista branca é recusada")
    void ordenacaoInvalida(String ordenacao) {
        assertThatThrownBy(() -> busca.buscar(FiltroDeImoveis.vazio(), 0, 10, ordenacao))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("página negativa ou tamanho fora de 1..50 → IllegalArgumentException")
    void paginaInvalida() {
        assertThatThrownBy(() -> busca.buscar(FiltroDeImoveis.vazio(), -1, 10, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> busca.buscar(FiltroDeImoveis.vazio(), 0, 0, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> busca.buscar(FiltroDeImoveis.vazio(), 0, 51, null)).isInstanceOf(IllegalArgumentException.class);
    }
}
