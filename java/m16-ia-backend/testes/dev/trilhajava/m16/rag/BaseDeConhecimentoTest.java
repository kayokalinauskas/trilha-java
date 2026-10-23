package dev.trilhajava.m16.rag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import dev.trilhajava.m16.ModeloDeEmbeddingFalso;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 16.02 — busca semântica com trechos, cosseno e isolamento por cliente")
class BaseDeConhecimentoTest {

    static final String PIX = """
            O Pix funciona 24 horas por dia, todos os dias. O limite noturno do Pix vale das 20h às 6h, \
            é de mil reais e pode ser alterado no aplicativo com antecedência de 24 horas. \
            Transferências agendadas seguem o limite do horário em que forem executadas.""";

    static final String CARTAO = """
            A fatura do cartão fecha dez dias antes do vencimento. Compras parceladas entram na fatura \
            seguinte ao fechamento. O pagamento mínimo da fatura evita a negativação, mas gera juros rotativos.""";

    static final String PIX_BANCO_B = "No banco B o limite noturno do Pix é de quinhentos reais, sem exceções.";

    @Nested
    @DisplayName("dividir em trechos")
    class Dividir {

        @Test
        @DisplayName("janelas de N palavras com sobreposição")
        void comSobreposicao() {
            assertThat(BaseDeConhecimento.dividir("a b c d e f g h i j", 4, 1))
                    .containsExactly("a b c d", "d e f g", "g h i j");
        }

        @Test
        @DisplayName("sem sobreposição, o último trecho fica menor")
        void semSobreposicao() {
            assertThat(BaseDeConhecimento.dividir("a b c d e f g h i j", 4, 0))
                    .containsExactly("a b c d", "e f g h", "i j");
        }

        @Test
        @DisplayName("não gera um trecho final que seja só sobreposição")
        void semTrechoRepetido() {
            assertThat(BaseDeConhecimento.dividir("a b c d e f g", 4, 1)).containsExactly("a b c d", "d e f g");
            assertThat(BaseDeConhecimento.dividir("a b c", 4, 1)).containsExactly("a b c");
        }

        @Test
        @DisplayName("espaços e quebras de linha repetidos contam como um só separador")
        void espacos() {
            assertThat(BaseDeConhecimento.dividir("  a   b\n\nc\td  ", 2, 0)).containsExactly("a b", "c d");
        }

        @Test
        @DisplayName("texto vazio não gera trechos")
        void vazio() {
            assertThat(BaseDeConhecimento.dividir("   ", 4, 1)).isEmpty();
            assertThat(BaseDeConhecimento.dividir(null, 4, 1)).isEmpty();
        }

        @Test
        @DisplayName("parâmetros impossíveis são rejeitados, inclusive no construtor")
        void parametrosInvalidos() {
            assertThatThrownBy(() -> BaseDeConhecimento.dividir("a b", 0, 0)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> BaseDeConhecimento.dividir("a b", 4, 4)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> BaseDeConhecimento.dividir("a b", 4, -1)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new BaseDeConhecimento(new ModeloDeEmbeddingFalso(), 10, 10))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("similaridade de cosseno")
    class Cosseno {

        @Test
        @DisplayName("mesma direção dá 1, perpendiculares dão 0 e opostos dão -1")
        void casosBasicos() {
            assertThat(BaseDeConhecimento.cosseno(new float[] {1, 2, 3}, new float[] {2, 4, 6})).isCloseTo(1.0, within(1e-9));
            assertThat(BaseDeConhecimento.cosseno(new float[] {1, 0}, new float[] {0, 5})).isCloseTo(0.0, within(1e-9));
            assertThat(BaseDeConhecimento.cosseno(new float[] {1, 1}, new float[] {-1, -1})).isCloseTo(-1.0, within(1e-9));
        }

        @Test
        @DisplayName("um exemplo calculado à mão")
        void exemplo() {
            // (1*2 + 2*0 + 2*1) / (3 * √5) = 4 / 6,7082 = 0,5963
            assertThat(BaseDeConhecimento.cosseno(new float[] {1, 2, 2}, new float[] {2, 0, 1})).isCloseTo(0.5963, within(1e-4));
        }

        @Test
        @DisplayName("vetor zerado dá 0, e não NaN")
        void vetorZerado() {
            assertThat(BaseDeConhecimento.cosseno(new float[] {0, 0}, new float[] {1, 1})).isEqualTo(0.0);
        }

        @Test
        @DisplayName("dimensões diferentes indicam modelos diferentes: erro")
        void dimensoesDiferentes() {
            assertThatThrownBy(() -> BaseDeConhecimento.cosseno(new float[] {1, 2}, new float[] {1, 2, 3}))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("indexar e buscar")
    class Buscar {

        ModeloDeEmbeddingFalso modelo;
        BaseDeConhecimento base;

        @BeforeEach
        void indexa() {
            modelo = new ModeloDeEmbeddingFalso();
            base = new BaseDeConhecimento(modelo, 12, 3);
            base.indexar("banco-a", "pix", PIX);
            base.indexar("banco-a", "cartao", CARTAO);
            base.indexar("banco-b", "pix", PIX_BANCO_B);
        }

        @Test
        @DisplayName("o trecho mais parecido com a pergunta vem primeiro")
        void maisRelevantePrimeiro() {
            List<Trecho> trechos = base.buscar("banco-a", "Qual o limite noturno do Pix?", 3, 0.2);

            assertThat(trechos).isNotEmpty();
            assertThat(trechos.getFirst().documentoId()).isEqualTo("pix");
            assertThat(trechos.getFirst().texto()).contains("limite noturno");
            assertThat(trechos).extracting(Trecho::similaridade).isSortedAccordingTo((a, b) -> Double.compare(b, a));
        }

        @Test
        @DisplayName("um cliente nunca vê os documentos de outro, mesmo que sejam a melhor resposta")
        void isolamentoPorTenant() {
            assertThat(base.buscar("banco-b", "limite noturno do Pix", 10, 0.0))
                    .extracting(Trecho::texto)
                    .isNotEmpty()
                    .allMatch(PIX_BANCO_B::contains);
            assertThat(base.buscar("banco-a", "quinhentos reais sem exceções", 10, 0.0))
                    .extracting(Trecho::texto)
                    .noneMatch(t -> t.contains("banco B"));
            assertThat(base.buscar("banco-c", "limite noturno do Pix", 10, 0.0)).isEmpty();
        }

        @Test
        @DisplayName("abaixo do limiar não entra: sem contexto bom, a lista vem vazia")
        void limiar() {
            assertThat(base.buscar("banco-a", "como investir em ações na bolsa?", 5, 0.3)).isEmpty();
        }

        @Test
        @DisplayName("topK limita a quantidade de trechos")
        void topK() {
            assertThat(base.buscar("banco-a", "fatura do cartão e limite do Pix", 2, 0.0)).hasSize(2);
            assertThatThrownBy(() -> base.buscar("banco-a", "pix", 0, 0.0)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("cada documento é enviado ao modelo numa única chamada, em lote")
        void embeddingEmLote() {
            // 3 documentos indexados no @BeforeEach: 3 chamadas, uma por documento, com todos os trechos juntos.
            assertThat(modelo.chamadas()).isEqualTo(3);
            assertThat(modelo.textosRecebidos()).isEqualTo(base.totalDeTrechos());
        }

        @Test
        @DisplayName("reindexar substitui os trechos antigos, sem duplicar")
        void reindexar() {
            int antes = base.totalDeTrechos();

            base.indexar("banco-a", "pix", "O limite noturno do Pix agora é de dois mil reais.");

            assertThat(base.totalDeTrechos()).isLessThan(antes);
            assertThat(base.buscar("banco-a", "limite noturno do Pix", 10, 0.0))
                    .extracting(Trecho::texto)
                    .noneMatch(t -> t.contains("mil reais e pode"))
                    .anyMatch(t -> t.contains("dois mil reais"));
        }

        @Test
        @DisplayName("o mesmo id em outro cliente é outro documento")
        void mesmoIdEmOutroTenant() {
            base.remover("banco-b", "pix");

            assertThat(base.buscar("banco-b", "limite noturno do Pix", 10, 0.0)).isEmpty();
            assertThat(base.buscar("banco-a", "limite noturno do Pix", 10, 0.2)).isNotEmpty();
        }

        @Test
        @DisplayName("tenant e pergunta são obrigatórios")
        void obrigatorios() {
            assertThatThrownBy(() -> base.buscar(" ", "pix", 3, 0.0)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> base.buscar("banco-a", "", 3, 0.0)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> base.indexar(null, "x", "texto")).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
