package dev.trilhajava.m13.aulas.padroes;

import dev.trilhajava.m13.aulas.padroes.MeioDePagamento.Boleto;
import dev.trilhajava.m13.aulas.padroes.MeioDePagamento.Cartao;
import dev.trilhajava.m13.aulas.padroes.MeioDePagamento.Pix;
import dev.trilhajava.m13.aulas.padroes.fornecedor.PagaFacilSandbox;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Roda cada padrão da aula isoladamente, sem Spring. Uso:
 * {@code java -cp target/classes dev.trilhajava.m13.aulas.padroes.DemonstracaoDosPadroes <parte>}
 * em que parte é tarifas, builder, adapter, decorators, antifraude, estados ou retorno.
 */
public class DemonstracaoDosPadroes {

    public static void main(String[] args) {
        String parte = args.length > 0 ? args[0] : "tarifas";
        switch (parte) {
            case "tarifas" -> tarifas();
            case "builder" -> builder();
            case "adapter" -> adapter();
            case "decorators" -> decorators();
            case "antifraude" -> antifraude();
            case "estados" -> estados();
            case "retorno" -> retorno();
            default -> System.out.println("parte desconhecida: " + parte);
        }
    }

    static void tarifas() {
        BigDecimal valor = new BigDecimal("250.00");
        List<MeioDePagamento> meios = List.of(
                new Pix("loja@exemplo.com.br"),
                new Cartao("VISA", 1),
                new Cartao("VISA", 6),
                new Boleto(LocalDate.of(2026, 10, 20)));
        for (MeioDePagamento meio : meios) {
            System.out.println(meio + " -> taxa R$ " + Tarifas.taxa(meio, valor));
        }
    }

    static void builder() {
        SolicitacaoDeCobranca solicitacao = SolicitacaoDeCobranca
                .builder("PED-2001", "cliente-42", new BigDecimal("189.90"), new Pix("loja@exemplo.com.br"))
                .descricao("2 camisetas")
                .metadado("canal", "app")
                .build();
        System.out.println(solicitacao);
        try {
            SolicitacaoDeCobranca.builder("PED-2002", "cliente-42", new BigDecimal("10.999"), new Cartao("ELO", 1))
                    .build();
        } catch (IllegalArgumentException e) {
            System.out.println("recusado pelo record: " + e.getMessage());
        }
    }

    static void adapter() {
        GatewayDePagamento gateway = new AdaptadorPagaFacil(new PagaFacilSandbox(0));
        System.out.println(gateway.cobrar(pedido("PED-3001", "199.90")));
        System.out.println(gateway.cobrar(pedido("PED-3002", "12000.00")));
        System.out.println(gateway.cobrar(pedido("PED-3001", "199.90"))); // mesma chave: mesma transação
    }

    static void decorators() {
        PagaFacilSandbox sandbox = new PagaFacilSandbox(2); // as duas primeiras chamadas falham com 503
        GatewayDePagamento gateway = new GatewayComRegistro(
                new GatewayComRetentativa(new AdaptadorPagaFacil(sandbox), 3, Duration.ofMillis(100)),
                mensagem -> System.out.println("[registro] " + mensagem));
        System.out.println("resultado: " + gateway.cobrar(pedido("PED-4001", "79.90")));
        System.out.println("chamadas ao fornecedor: " + sandbox.chamadasRecebidas()
                + ", transações criadas: " + sandbox.transacoesCriadas());
    }

    static void antifraude() {
        AnaliseAntifraude analise = new AnaliseAntifraude(List.of(
                s -> s.valor().compareTo(new BigDecimal("5000.00")) > 0 && s.meio() instanceof Boleto
                        ? Optional.of("boleto acima de R$ 5.000,00") : Optional.empty(),
                s -> s.clienteId().equals("cliente-666")
                        ? Optional.of("cliente bloqueado") : Optional.empty()));
        Boleto boleto = new Boleto(LocalDate.of(2026, 10, 20));
        List<SolicitacaoDeCobranca> casos = List.of(
                SolicitacaoDeCobranca.builder("PED-5001", "cliente-42", new BigDecimal("300.00"), boleto).build(),
                SolicitacaoDeCobranca.builder("PED-5002", "cliente-42", new BigDecimal("7500.00"), boleto).build(),
                SolicitacaoDeCobranca.builder("PED-5003", "cliente-666", new BigDecimal("7500.00"), boleto).build(),
                SolicitacaoDeCobranca.builder("PED-5004", "cliente-666", new BigDecimal("50.00"), boleto).build());
        for (SolicitacaoDeCobranca s : casos) {
            System.out.println(s.pedidoId() + " -> " + analise.avaliar(s).orElse("aprovado pela antifraude"));
        }
    }

    static void estados() {
        EstadoDaCobranca estado = new EstadoDaCobranca.Pendente();
        System.out.println(estado);
        estado = estado.autorizar("PF-0001");
        System.out.println(estado);
        estado = estado.capturar(Instant.parse("2026-10-06T13:00:00Z"));
        System.out.println(estado);
        try {
            estado.autorizar("PF-0002");
        } catch (EstadoDaCobranca.TransicaoInvalidaException e) {
            System.out.println("recusado: " + e.getMessage());
        }
        System.out.println(estado.estornar("cliente desistiu"));
    }

    static void retorno() {
        record Liquidacao(String codigo, BigDecimal valor, String status) {
        }
        LeitorDeRetorno<Liquidacao> leitor = new LeitorDeRetorno<>("codigo;valor;status", 3,
                colunas -> new Liquidacao(colunas[0], new BigDecimal(colunas[1]), colunas[2]));
        LeitorDeRetorno.Leitura<Liquidacao> leitura = leitor.ler(List.of(
                "codigo;valor;status",
                "PF-0001;199.90;LIQUIDADO",
                "PF-0002;abc;LIQUIDADO",
                "",
                "PF-0003;59.90",
                "PF-0004;79.90;ESTORNADO"));
        leitura.registros().forEach(System.out::println);
        leitura.erros().forEach(erro -> System.out.println("erro: " + erro));
    }

    private static SolicitacaoDeCobranca pedido(String pedidoId, String valor) {
        return SolicitacaoDeCobranca
                .builder(pedidoId, "cliente-42", new BigDecimal(valor), new Cartao("MASTERCARD", 1))
                .build();
    }
}
