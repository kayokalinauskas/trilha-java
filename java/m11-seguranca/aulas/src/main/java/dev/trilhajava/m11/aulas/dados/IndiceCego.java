package dev.trilhajava.m11.aulas.dados;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

/**
 * Blind index: um HMAC-SHA256 do valor normalizado, com uma chave própria (diferente da chave que
 * cifra). O mesmo CPF sempre gera o mesmo índice, então dá para criar um índice no banco e buscar
 * com {@code WHERE cpf_indice = ?} sem decifrar nada. Sem a chave, o índice não serve para testar
 * CPFs por força bruta, ao contrário de um SHA-256 puro.
 */
public final class IndiceCego {

    private final SecretKey chaveHmac;

    public IndiceCego(SecretKey chaveHmac) {
        if (chaveHmac.getEncoded().length < 32) {
            throw new IllegalArgumentException("use uma chave HMAC de pelo menos 256 bits");
        }
        this.chaveHmac = chaveHmac;
    }

    /** O contexto separa campos: o mesmo valor em "paciente.cpf" e "medico.cpf" gera índices diferentes. */
    public String calcular(String valorNormalizado, String contexto) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(chaveHmac);
            mac.update(contexto.getBytes(StandardCharsets.UTF_8));
            mac.update((byte) 0);   // separador entre o contexto e o valor
            byte[] resultado = mac.doFinal(valorNormalizado.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resultado);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 indisponível", e);
        }
    }

    /** O CPF entra sempre normalizado (só os 11 dígitos): "123.456.789-09" e "12345678909" batem. */
    public String doCpf(Cpf cpf, String contexto) {
        return calcular(cpf.digitos(), contexto);
    }
}
