package dev.trilhajava.m11.aulas.senhas;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Cadastro e login do portal do paciente.
 *
 * <ul>
 *   <li>o cadastro aplica a {@link PoliticaDeSenha} e guarda só o hash;</li>
 *   <li>um e-mail já cadastrado não gera erro visível: o dono recebe um aviso por e-mail;</li>
 *   <li>o login responde igual (e no mesmo tempo) para e-mail inexistente e senha errada;</li>
 *   <li>falhas seguidas ativam o {@link ControleDeTentativas};</li>
 *   <li>um hash de algoritmo antigo é regravado no primeiro login com sucesso.</li>
 * </ul>
 */
public class ServicoDeAcesso {

    /** Bem acima do máximo da política: nenhuma conta tem uma senha maior que isso. */
    static final int TAMANHO_MAXIMO_NO_LOGIN = 256;

    private final RepositorioDeContas contas;
    private final PasswordEncoder codificador;
    private final PoliticaDeSenha politica;
    private final ControleDeTentativas tentativas;
    private final CaixaDeEmail emails;
    /** Hash de uma senha qualquer, com o algoritmo e o custo atuais: equaliza o tempo. */
    private final String hashFicticio;

    public ServicoDeAcesso(RepositorioDeContas contas, PasswordEncoder codificador, PoliticaDeSenha politica,
            ControleDeTentativas tentativas, CaixaDeEmail emails) {
        this.contas = contas;
        this.codificador = codificador;
        this.politica = politica;
        this.tentativas = tentativas;
        this.emails = emails;
        this.hashFicticio = codificador.encode("uma senha qualquer que ninguém usa");
    }

    public void cadastrar(String email, String senha) {
        String normalizado = normalizar(email);
        List<PoliticaDeSenha.Problema> problemas = politica.avaliar(normalizado, senha);
        if (!problemas.isEmpty()) {
            throw new SenhaRecusadaException(problemas);
        }
        if (contas.buscar(normalizado).isPresent()) {
            // A tela diz "enviamos um e-mail de confirmação" nos dois casos; só o dono fica sabendo.
            emails.enviar(normalizado, "Tentativa de cadastro",
                    "Alguém tentou criar uma conta com este e-mail. Se foi você, use \"Esqueci a senha\".");
            return;
        }
        contas.salvar(new Conta(normalizado, codificador.encode(senha)));
    }

    public ResultadoDaEntrada entrar(String email, String senha) {
        String normalizado = normalizar(email);
        if (tentativas.exigeRecuperacao(normalizado)) {
            return new ResultadoDaEntrada.ExigeRedefinicao();
        }
        Optional<Instant> espera = tentativas.esperaAte(normalizado);
        if (espera.isPresent()) {
            return new ResultadoDaEntrada.AguardeAte(espera.get());
        }
        if (senha == null || senha.length() > TAMANHO_MAXIMO_NO_LOGIN) {
            // Recusa antes do hash para não gastar CPU com 1 MB de "senha"; vale para qualquer e-mail.
            tentativas.registrarFalha(normalizado);
            return new ResultadoDaEntrada.Recusada();
        }
        Optional<Conta> conta = contas.buscar(normalizado);
        String hash = conta.map(Conta::hashDaSenha).orElse(hashFicticio);
        boolean confere = codificador.matches(senha, hash) && conta.isPresent();
        if (!confere) {
            tentativas.registrarFalha(normalizado);
            return new ResultadoDaEntrada.Recusada();
        }
        tentativas.limpar(normalizado);
        if (codificador.upgradeEncoding(hash)) {
            // Único momento em que temos a senha em mãos: regrava com o algoritmo atual.
            contas.salvar(conta.get().comHash(codificador.encode(senha)));
        }
        return new ResultadoDaEntrada.Autenticada(normalizado);
    }

    static String normalizar(String email) {
        String limpo = email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
        if (limpo.indexOf('@') < 1) {
            throw new IllegalArgumentException("E-mail inválido");
        }
        return limpo;
    }
}
