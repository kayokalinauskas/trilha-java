package dev.trilhajava.m03.biblioteca;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Solução de referência do exercício 03.03. */
public class Biblioteca {

    public static final int PRAZO_DIAS = 14;
    public static final int LIMITE_ATIVOS = 3;
    private static final BigDecimal MULTA_DIA = new BigDecimal("2.00");
    private static final BigDecimal MULTA_MAXIMA = new BigDecimal("50.00");

    private final Map<String, Livro> acervo = new HashMap<>();
    private final Map<String, Integer> disponiveis = new HashMap<>();
    private final Map<Long, Emprestimo> ativos = new LinkedHashMap<>();
    private long proximoId = 1;

    public void cadastrar(Livro livro, int exemplares) {
        if (livro == null) {
            throw new IllegalArgumentException("Livro é obrigatório");
        }
        if (exemplares < 1) {
            throw new IllegalArgumentException("Exemplares deve ser pelo menos 1");
        }
        acervo.put(livro.isbn(), livro);
        disponiveis.merge(livro.isbn(), exemplares, Integer::sum);
    }

    public int disponiveis(String isbn) {
        exigirLivro(isbn);
        return disponiveis.get(isbn);
    }

    public Emprestimo emprestar(String isbn, String usuarioId, LocalDate data) {
        exigirLivro(isbn);
        List<Emprestimo> doUsuario = ativosDo(usuarioId);
        for (Emprestimo e : doUsuario) {
            if (e.atrasadoEm(data)) {
                throw new UsuarioComAtrasoException(usuarioId);
            }
        }
        if (doUsuario.size() >= LIMITE_ATIVOS) {
            throw new LimiteEmprestimosException(usuarioId, LIMITE_ATIVOS);
        }
        if (disponiveis.get(isbn) == 0) {
            throw new LivroIndisponivelException(isbn);
        }
        // Só altera o estado depois de TODAS as validações: uma falha nunca deixa a biblioteca inconsistente.
        disponiveis.merge(isbn, -1, Integer::sum);
        Emprestimo emprestimo = new Emprestimo(proximoId++, isbn, usuarioId, data, data.plusDays(PRAZO_DIAS));
        ativos.put(emprestimo.id(), emprestimo);
        return emprestimo;
    }

    public BigDecimal devolver(long emprestimoId, LocalDate data) {
        Emprestimo emprestimo = ativos.remove(emprestimoId);
        if (emprestimo == null) {
            throw new EmprestimoNaoEncontradoException(emprestimoId);
        }
        disponiveis.merge(emprestimo.isbn(), 1, Integer::sum);
        long diasAtraso = Math.max(0, ChronoUnit.DAYS.between(emprestimo.dataPrevista(), data));
        BigDecimal multa = MULTA_DIA.multiply(BigDecimal.valueOf(diasAtraso));
        return multa.min(MULTA_MAXIMA);
    }

    private List<Emprestimo> ativosDo(String usuarioId) {
        List<Emprestimo> resultado = new ArrayList<>();
        for (Emprestimo e : ativos.values()) {
            if (e.usuarioId().equals(usuarioId)) {
                resultado.add(e);
            }
        }
        return resultado;
    }

    private void exigirLivro(String isbn) {
        if (!acervo.containsKey(isbn)) {
            throw new LivroNaoEncontradoException(isbn);
        }
    }
}
