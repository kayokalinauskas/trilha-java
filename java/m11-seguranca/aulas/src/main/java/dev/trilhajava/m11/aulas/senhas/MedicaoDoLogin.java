package dev.trilhajava.m11.aulas.senhas;

import java.time.Clock;
import java.util.Arrays;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Mede, como um atacante mediria de fora, o tempo do login em três situações. Se os tempos
 * forem diferentes, a resposta "e-mail ou senha inválidos" não esconde nada.
 */
public class MedicaoDoLogin {

    public static void main(String[] args) {
        PasswordEncoder codificador = Codificadores.doPortal();
        var contas = new RepositorioDeContasEmMemoria();
        var tentativas = new ControleDeTentativas(Clock.systemUTC());
        var servico = new ServicoDeAcesso(contas, codificador, new PoliticaDeSenha(senha -> false, Set.of()),
                tentativas, (para, assunto, corpo) -> { });

        servico.cadastrar("marta@email.com", "cachorro late no quintal");
        contas.salvar(new Conta("joao@email.com",
                "{bcrypt-sha1}" + new EnvelopeSha1Legado().envelopar(EnvelopeSha1Legado.sha1Hex("frase do joão"))));

        System.out.printf("conta Argon2id, senha errada:   %4d ms%n", mediana(servico, tentativas, "marta@email.com"));
        System.out.printf("e-mail inexistente:             %4d ms%n", mediana(servico, tentativas, "ninguem@email.com"));
        System.out.printf("conta legada, senha errada:     %4d ms%n", mediana(servico, tentativas, "joao@email.com"));
    }

    private static long mediana(ServicoDeAcesso servico, ControleDeTentativas tentativas, String email) {
        long[] tempos = new long[15];
        for (int i = 0; i < tempos.length; i++) {
            long inicio = System.nanoTime();
            servico.entrar(email, "uma senha errada qualquer");
            tempos[i] = (System.nanoTime() - inicio) / 1_000_000;
            tentativas.limpar(email); // só para a medição não esbarrar no bloqueio progressivo
        }
        Arrays.sort(tempos);
        return tempos[tempos.length / 2];
    }
}
