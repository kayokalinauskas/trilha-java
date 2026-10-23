package dev.trilhajava.m13.aulas.resiliencia;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.resilience.annotation.EnableResilientMethods;

/**
 * Liga @Retryable e @ConcurrencyLimit (Spring Framework 7). Sem @EnableResilientMethods,
 * as anotações compilam e são simplesmente ignoradas: nenhum erro, nenhuma repetição.
 * O ClienteDaOperadora e a PoliticaDeResiliencia vêm de fora (da aplicação ou do teste).
 */
@Configuration
@EnableResilientMethods
public class ConfiguracaoDeResiliencia {

    @Bean
    VerificadorDeElegibilidade verificadorDeElegibilidade(ClienteDaOperadora operadora,
                                                          PoliticaDeResiliencia politica) {
        return new VerificadorDeElegibilidade(operadora, politica);
    }

    @Bean
    AutorizacaoDeProcedimentos autorizacaoDeProcedimentos(ClienteDaOperadora operadora) {
        return new AutorizacaoDeProcedimentos(operadora);
    }

    @Bean
    ConsultaProtegidaDeElegibilidade consultaProtegidaDeElegibilidade(VerificadorDeElegibilidade verificador) {
        return new ConsultaProtegidaDeElegibilidade(verificador);
    }
}
