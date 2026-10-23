package dev.trilhajava.m12.aulas.esquemas;

/** Falha permanente: o evento tem um tipo (ou versão) que este consumidor não sabe ler. Vai para a DLT. */
public class TipoDeEventoDesconhecidoException extends RuntimeException {

    public TipoDeEventoDesconhecidoException(String mensagem) {
        super(mensagem);
    }
}
