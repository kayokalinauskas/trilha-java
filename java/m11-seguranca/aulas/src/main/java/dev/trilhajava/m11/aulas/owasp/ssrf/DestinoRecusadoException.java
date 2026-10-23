package dev.trilhajava.m11.aulas.owasp.ssrf;

/** URL de webhook recusada. A mensagem vai para o log; o cliente recebe só "URL não permitida" (400). */
public class DestinoRecusadoException extends RuntimeException {

    public DestinoRecusadoException(String motivo) {
        super(motivo);
    }
}
