package dev.trilhajava.fintech.aplicacao;

import dev.trilhajava.fintech.dominio.Usuario;

/** Porta: a aplicação sabe QUE precisa de um token; COMO ele é assinado (JWT, HS256...) é detalhe da infra. */
public interface EmissorDeTokens {

    TokenEmitido emitir(Usuario usuario);
}
