package dev.trilhajava.m11.aulas.senhas;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm;

/**
 * Mede quanto custa conferir uma senha com cada algoritmo e parâmetro, NA máquina onde roda.
 * Rode no tipo de servidor de produção: o número do seu notebook não serve para decidir.
 */
public class CalibracaoDeCusto {

    private static final String SENHA = "ipê amarelo na praça da sé às seis";

    public static void main(String[] args) throws NoSuchAlgorithmException {
        System.out.printf("%-34s %10s%n", "algoritmo e parâmetros", "mediana");
        for (int custo = 10; custo <= 13; custo++) {
            imprimir("BCrypt custo " + custo, new BCryptPasswordEncoder(custo));
        }
        imprimir("Argon2id m=19 MiB t=2 p=1", new Argon2PasswordEncoder(16, 32, 1, 19_456, 2));
        imprimir("Argon2id m=46 MiB t=1 p=1", new Argon2PasswordEncoder(16, 32, 1, 47_104, 1));
        imprimir("Argon2id m=64 MiB t=3 p=1", new Argon2PasswordEncoder(16, 32, 1, 65_536, 3));
        imprimir("PBKDF2-SHA256 600.000 iterações",
                new Pbkdf2PasswordEncoder("", 16, 600_000, SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256));
        System.out.printf("%-34s %10s%n", "SHA-256 puro (1 hash)", String.format("%.4f ms", sha256Ms()));
    }

    /** Mediana, em milissegundos, de {@code repeticoes} conferências (matches) depois do aquecimento. */
    public static double medianaMs(PasswordEncoder codificador, int repeticoes) {
        String hash = codificador.encode(SENHA);
        codificador.matches(SENHA, hash); // aquecimento: JIT e alocação da memória
        double[] tempos = new double[repeticoes];
        for (int i = 0; i < repeticoes; i++) {
            long inicio = System.nanoTime();
            codificador.matches(SENHA, hash);
            tempos[i] = (System.nanoTime() - inicio) / 1_000_000.0;
        }
        Arrays.sort(tempos);
        return tempos[repeticoes / 2];
    }

    private static void imprimir(String nome, PasswordEncoder codificador) {
        System.out.printf("%-34s %10s%n", nome, String.format("%.0f ms", medianaMs(codificador, 7)));
    }

    private static double sha256Ms() throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] entrada = SENHA.getBytes(StandardCharsets.UTF_8);
        int vezes = 1_000_000;
        long inicio = System.nanoTime();
        for (int i = 0; i < vezes; i++) {
            sha256.digest(entrada);
        }
        return (System.nanoTime() - inicio) / 1_000_000.0 / vezes;
    }
}
