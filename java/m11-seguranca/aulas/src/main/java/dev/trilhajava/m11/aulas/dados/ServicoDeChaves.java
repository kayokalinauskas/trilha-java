package dev.trilhajava.m11.aulas.dados;

import javax.crypto.SecretKey;

/**
 * O que a aplicação precisa de um KMS (AWS KMS, Google Cloud KMS, Azure Key Vault, Vault Transit):
 * gerar uma chave de dados nova e desembrulhar uma chave guardada. A chave mestra (KEK) nunca sai
 * do KMS; a aplicação só vê a chave de dados (DEK) e a versão embrulhada dela.
 */
public interface ServicoDeChaves {

    /** Uma DEK recém-gerada: a versão em claro (para usar agora) e a embrulhada (para guardar). */
    record ChaveDeDadosGerada(SecretKey emClaro, byte[] embrulhada) {}

    ChaveDeDadosGerada gerarChaveDeDados();

    SecretKey desembrulhar(byte[] embrulhada);
}
