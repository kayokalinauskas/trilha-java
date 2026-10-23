package dev.trilhajava.m11.aulas.dados;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Raiz da aula 11.08 para as fatias de teste ({@code @DataJpaTest} procura esta classe subindo
 * pelos pacotes). Os beans de criptografia ({@link CifradorDeCampo}, {@link IndiceCego}) não são
 * declarados aqui: em produção eles vêm de uma configuração que carrega as DEKs embrulhadas e as
 * abre no KMS; nos testes, de {@code ChavesDeTeste}.
 */
@SpringBootApplication
public class AplicacaoDeDados {
}
