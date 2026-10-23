package dev.trilhajava.m15.hash;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 15.03 — hash consistente com nós virtuais")
class AnelDeHashTest {

    static final List<String> CHAVES = IntStream.range(0, 100_000).mapToObj(i -> "cliente:" + i).toList();

    static AnelDeHash anel(int virtuais, String... nos) {
        var anel = new AnelDeHash(virtuais);
        for (String no : nos) {
            anel.adicionar(no);
        }
        return anel;
    }

    static Map<String, String> donos(AnelDeHash anel) {
        Map<String, String> donos = new HashMap<>();
        for (String chave : CHAVES) {
            donos.put(chave, anel.noPara(chave));
        }
        return donos;
    }

    static Map<String, Integer> contagem(Map<String, String> donos) {
        Map<String, Integer> contagem = new HashMap<>();
        donos.values().forEach(no -> contagem.merge(no, 1, Integer::sum));
        return contagem;
    }

    @Test
    @DisplayName("anel vazio: não há a quem entregar a chave")
    void vazio() {
        assertThatThrownBy(() -> new AnelDeHash(10).noPara("x")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("pelo menos um ponto por nó")
    void virtuaisInvalidos() {
        assertThatThrownBy(() -> new AnelDeHash(0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("com um nó só, ele recebe todas as chaves")
    void umNo() {
        var anel = anel(50, "cache-a");

        assertThat(CHAVES.stream().limit(1000)).allMatch(c -> anel.noPara(c).equals("cache-a"));
    }

    @Test
    @DisplayName("determinístico: dois anéis com os mesmos nós dão o mesmo dono para cada chave")
    void deterministico() {
        var um = anel(100, "a", "b", "c");
        var outro = anel(100, "c", "a", "b");

        assertThat(CHAVES.stream().limit(5000)).allMatch(c -> um.noPara(c).equals(outro.noPara(c)));
    }

    @Test
    @DisplayName("com 200 nós virtuais, 5 nós recebem cada um entre 80% e 120% da média de chaves")
    void distribuicao() {
        Map<String, Integer> contagem = contagem(donos(anel(200, "n1", "n2", "n3", "n4", "n5")));

        assertThat(contagem).hasSize(5);
        double media = CHAVES.size() / 5.0;
        assertThat(contagem.values()).allSatisfy(n -> assertThat((double) n).isBetween(media * 0.8, media * 1.2));
    }

    @Test
    @DisplayName("adicionar um 6º nó move ~1/6 das chaves, só para o nó novo (com hash % n, mais de 70% mudariam)")
    void adicionarMovePoucas() {
        var anel = anel(200, "n1", "n2", "n3", "n4", "n5");
        Map<String, String> antes = donos(anel);

        anel.adicionar("n6");
        Map<String, String> depois = donos(anel);

        long movidas = CHAVES.stream().filter(c -> !antes.get(c).equals(depois.get(c))).count();
        assertThat((double) movidas / CHAVES.size()).isBetween(0.10, 0.22);
        assertThat(CHAVES.stream().filter(c -> !antes.get(c).equals(depois.get(c))))
                .allMatch(c -> depois.get(c).equals("n6"));

        long comModulo = CHAVES.stream()
                .filter(c -> Math.floorMod(AnelDeHash.hash(c), 5) != Math.floorMod(AnelDeHash.hash(c), 6))
                .count();
        assertThat(comModulo).as("para comparar: hash %% 5 contra hash %% 6").isGreaterThan(CHAVES.size() * 7L / 10);
    }

    @Test
    @DisplayName("remover um nó só move as chaves que eram dele")
    void removerMoveSoAsDele() {
        var anel = anel(200, "n1", "n2", "n3", "n4", "n5");
        Map<String, String> antes = donos(anel);

        anel.remover("n3");
        Map<String, String> depois = donos(anel);

        assertThat(CHAVES).allMatch(c -> antes.get(c).equals("n3") || antes.get(c).equals(depois.get(c)));
        assertThat(depois.values()).doesNotContain("n3");
    }

    @Test
    @DisplayName("adicionar um nó que já existe, ou remover um que não existe, não muda nada")
    void idempotente() {
        var anel = anel(100, "a", "b");
        Map<String, String> antes = donos(anel);

        anel.adicionar("a");
        anel.remover("z");

        assertThat(donos(anel)).isEqualTo(antes);
        assertThat(anel.nos()).containsExactlyInAnyOrder("a", "b");
    }
}
