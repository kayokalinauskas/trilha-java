package dev.trilhajava.m12.aulas.fundamentos;

import java.util.UUID;

/**
 * Uma ordem para UM destinatário, no imperativo ({@code EmitirNotaFiscal}).
 * O destinatário define o contrato e pode recusar o pedido ("CNPJ inválido").
 */
public interface Comando {

    UUID idDoComando();
}
