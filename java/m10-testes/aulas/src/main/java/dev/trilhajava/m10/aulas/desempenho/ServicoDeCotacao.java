package dev.trilhajava.m10.aulas.desempenho;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Duas versões da mesma regra. As duas devolvem exatamente a mesma cotação;
 * a diferença está em quanto tempo cada requisição segura uma conexão do pool.
 */
@Service
public class ServicoDeCotacao {

    private final TabelaDeFrete tabela;
    private final Transportadora transportadora;

    public ServicoDeCotacao(TabelaDeFrete tabela, Transportadora transportadora) {
        this.tabela = tabela;
        this.transportadora = transportadora;
    }

    /** v1: a chamada à transportadora acontece DENTRO da transação, com a conexão presa. */
    @Transactional(readOnly = true)
    public Cotacao cotarComTransacaoLonga(String cep) {
        return cotar(cep);
    }

    /** v2: sem transação; a conexão volta ao pool assim que a consulta termina. */
    public Cotacao cotarSemSegurarConexao(String cep) {
        return cotar(cep);
    }

    private Cotacao cotar(String cep) {
        FaixaDeFrete faixa = tabela.faixaDo(cep).orElseThrow(() -> new CepNaoAtendidoException(cep));
        int prazo = transportadora.prazoEmDiasUteis(faixa.regiao());
        return new Cotacao(cep, faixa.regiao(), faixa.valorBase(), prazo);
    }
}
