package dev.trilhajava.fintech.dominio;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Grava o value object Cpf como texto (11 dígitos) e reconstrói ao ler. */
@Converter
public class CpfConverter implements AttributeConverter<Cpf, String> {

    @Override
    public String convertToDatabaseColumn(Cpf cpf) {
        return cpf == null ? null : cpf.numero();
    }

    @Override
    public Cpf convertToEntityAttribute(String numero) {
        return numero == null ? null : new Cpf(numero);
    }
}
