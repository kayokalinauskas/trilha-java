package dev.trilhajava.m11.aulas.senhas;

/**
 * Migração em lote, rodada uma vez: troca todo SHA-1 sem prefixo (importado do sistema legado)
 * por {@code {bcrypt-sha1}bcrypt(sha1)}. Depois dela, nenhum hash rápido fica no banco, mesmo das
 * contas cujos donos nunca mais entrarem.
 */
public class MigracaoDeHashesLegados {

    private final RepositorioDeContas contas;
    private final EnvelopeSha1Legado envelope;

    public MigracaoDeHashesLegados(RepositorioDeContas contas, EnvelopeSha1Legado envelope) {
        this.contas = contas;
        this.envelope = envelope;
    }

    /** Devolve quantas contas foram migradas. Rodar de novo não faz nada (é idempotente). */
    public int executar() {
        int migradas = 0;
        for (Conta conta : contas.todas()) {
            if (ehSha1Legado(conta.hashDaSenha())) {
                contas.salvar(conta.comHash("{bcrypt-sha1}" + envelope.envelopar(conta.hashDaSenha())));
                migradas++;
            }
        }
        return migradas;
    }

    /** SHA-1 em hexadecimal: 40 caracteres de 0-9 e a-f, sem o prefixo {id}. */
    static boolean ehSha1Legado(String hash) {
        if (hash == null || hash.length() != 40) {
            return false;
        }
        for (char c : hash.toCharArray()) {
            if (Character.digit(c, 16) < 0) {
                return false;
            }
        }
        return true;
    }
}
