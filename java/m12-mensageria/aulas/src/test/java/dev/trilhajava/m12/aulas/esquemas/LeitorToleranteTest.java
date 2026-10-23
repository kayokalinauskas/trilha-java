package dev.trilhajava.m12.aulas.esquemas;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeitorToleranteTest {

    private final LeitorDePedidoConfirmado leitor = Exemplos.leitor();

    @Test
    void ignoraCampoNovoEMapeiaEnumNovoParaDesconhecida() {
        PedidoConfirmado pedido = leitor.ler(Exemplos.ler("v2-com-campos-novos.json"));

        assertThat(pedido.pedidoId()).isEqualTo("PED-300");
        assertThat(pedido.formaDePagamento()).isEqualTo(FormaDePagamento.DESCONHECIDA);
        assertThat(pedido.valorTotal().quantia()).isEqualByComparingTo("89.90");
    }

    @Test
    void aceitaONomeAntigoDoCepDuranteAMigracao() {
        PedidoConfirmado pedido = leitor.ler(Exemplos.ler("v2-cep-com-nome-antigo.json"));

        assertThat(pedido.cepDeEntrega()).isEqualTo("80010-000");
        assertThat(pedido.formaDePagamento()).isEqualTo(FormaDePagamento.DESCONHECIDA);
    }

    @Test
    void oMapperEstritoQuebraComOMesmoEvento() {
        var estrito = new LeitorDePedidoConfirmado(MapeadoresJson.estrito(),
                List.of(new PedidoConfirmadoV1ParaV2()));

        assertThatThrownBy(() -> estrito.ler(Exemplos.ler("v2-com-campos-novos.json")))
                .isInstanceOf(InvalidFormatException.class)
                .hasMessageContaining("PIX_PARCELADO");
    }
}
