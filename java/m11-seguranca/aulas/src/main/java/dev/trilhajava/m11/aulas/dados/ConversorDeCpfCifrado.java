package dev.trilhajava.m11.aulas.dados;

import jakarta.persistence.AttributeConverter;

/**
 * Cifra o CPF ao gravar e decifra ao ler, de forma transparente para o resto do código: a entidade
 * tem um {@link Cpf}, a coluna tem {@code v2:...}. O Spring Boot entrega ao Hibernate um contêiner
 * de beans do Spring, por isso o conversor pode receber o cifrador pelo construtor.
 */
public class ConversorDeCpfCifrado implements AttributeConverter<Cpf, String> {

    static final String CONTEXTO = "paciente.cpf";

    private final CifradorDeCampo cifrador;

    public ConversorDeCpfCifrado(CifradorDeCampo cifrador) {
        this.cifrador = cifrador;
    }

    @Override
    public String convertToDatabaseColumn(Cpf cpf) {
        return cpf == null ? null : cifrador.cifrar(cpf.digitos(), CONTEXTO);
    }

    @Override
    public Cpf convertToEntityAttribute(String coluna) {
        return coluna == null ? null : new Cpf(cifrador.decifrar(coluna, CONTEXTO));
    }
}
