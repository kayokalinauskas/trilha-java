package dev.trilhajava.m13.aulas.hexagonal.aplicacao;

import dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.entrada.ComandoDeResgate;
import dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.entrada.ResgatarRecompensa;
import dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.saida.CarteirasDePontos;
import dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.saida.CatalogoDeRecompensas;
import dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.saida.PublicadorDeEventos;
import dev.trilhajava.m13.aulas.hexagonal.dominio.CarteiraDePontos;
import dev.trilhajava.m13.aulas.hexagonal.dominio.RecompensaResgatada;
import dev.trilhajava.m13.aulas.hexagonal.dominio.Recompensa;
import dev.trilhajava.m13.aulas.hexagonal.dominio.Resgate;
import java.time.Clock;
import java.time.Instant;

/**
 * Implementa a porta de entrada orquestrando o domínio e as portas de saída.
 * Sem @Service e sem @Transactional: quem registra o bean e abre a transação é a montagem.
 */
public class ServicoDeResgate implements ResgatarRecompensa {

    private final CarteirasDePontos carteiras;
    private final CatalogoDeRecompensas catalogo;
    private final PublicadorDeEventos eventos;
    private final Clock relogio;

    public ServicoDeResgate(CarteirasDePontos carteiras, CatalogoDeRecompensas catalogo,
                            PublicadorDeEventos eventos, Clock relogio) {
        this.carteiras = carteiras;
        this.catalogo = catalogo;
        this.eventos = eventos;
        this.relogio = relogio;
    }

    @Override
    public Resgate resgatar(ComandoDeResgate comando) {
        CarteiraDePontos carteira = carteiras.doCliente(comando.clienteId())
                .orElseThrow(() -> new NaoEncontradoException("carteira não encontrada: " + comando.clienteId()));
        Recompensa recompensa = catalogo.porCodigo(comando.codigoRecompensa())
                .orElseThrow(() -> new NaoEncontradoException("recompensa não encontrada: " + comando.codigoRecompensa()));

        Resgate resgate = carteira.resgatar(recompensa, Instant.now(relogio));   // a regra mora no domínio

        carteiras.salvar(carteira);
        eventos.publicar(RecompensaResgatada.de(resgate));
        return resgate;
    }
}
