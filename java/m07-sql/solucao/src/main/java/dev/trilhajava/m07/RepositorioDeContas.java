package dev.trilhajava.m07;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * Solução de referência do exercício 07.04.
 *
 * É exatamente isto que o Spring (JdbcTemplate, @Transactional) e o JPA fazem por você nos módulos 08 e 09:
 * pegar conexão, desligar o autocommit, executar, commit/rollback e fechar tudo.
 */
public class RepositorioDeContas {

    private final DataSource dataSource;

    public RepositorioDeContas(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public long abrir(String titular, BigDecimal saldoInicial) {
        String sql = "INSERT INTO conta (titular, saldo) VALUES (?, ?)";
        try (Connection c = dataSource.getConnection();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, titular);             // o driver envia o texto como DADO, nunca como SQL
            ps.setBigDecimal(2, saldoInicial);
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return chaves.getLong(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao abrir conta", e);
        }
    }

    public Optional<BigDecimal> saldo(long contaId) {
        try (Connection c = dataSource.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT saldo FROM conta WHERE id = ?")) {
            ps.setLong(1, contaId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getBigDecimal("saldo")) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao consultar saldo da conta " + contaId, e);
        }
    }

    public List<Movimento> movimentos(long contaId) {
        String sql = "SELECT conta_id, valor, descricao FROM movimento WHERE conta_id = ? ORDER BY id";
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, contaId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Movimento> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(new Movimento(rs.getLong("conta_id"), rs.getBigDecimal("valor"), rs.getString("descricao")));
                }
                return List.copyOf(lista);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar movimentos da conta " + contaId, e);
        }
    }

    public void transferir(long origem, long destino, BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor deve ser positivo");
        }
        if (origem == destino) {
            throw new IllegalArgumentException("Origem e destino devem ser contas diferentes");
        }
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false); // a partir daqui, nada é definitivo até o commit
            try {
                // UPDATE condicional: verifica e debita em UM comando atômico (sem check-then-act).
                if (atualizarSaldo(c, origem, valor.negate(), valor) == 0) {
                    if (saldoNaConexao(c, origem).isEmpty()) {
                        throw new ContaNaoEncontradaException(origem);
                    }
                    throw new SaldoInsuficienteException(origem);
                }
                if (atualizarSaldo(c, destino, valor, null) == 0) {
                    throw new ContaNaoEncontradaException(destino);
                }
                registrar(c, origem, valor.negate(), "Transferência enviada para a conta " + destino);
                registrar(c, destino, valor, "Transferência recebida da conta " + origem);
                c.commit();
            } catch (RuntimeException | SQLException e) {
                c.rollback(); // desfaz o débito se algo falhar depois dele
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro na transferência", e);
        }
    }

    /** Soma "delta" ao saldo; se saldoMinimo não for nulo, só atualiza se saldo >= saldoMinimo. Devolve linhas afetadas. */
    private static int atualizarSaldo(Connection c, long conta, BigDecimal delta, BigDecimal saldoMinimo) throws SQLException {
        String sql = saldoMinimo == null
                ? "UPDATE conta SET saldo = saldo + ? WHERE id = ?"
                : "UPDATE conta SET saldo = saldo + ? WHERE id = ? AND saldo >= ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, delta);
            ps.setLong(2, conta);
            if (saldoMinimo != null) {
                ps.setBigDecimal(3, saldoMinimo);
            }
            return ps.executeUpdate();
        }
    }

    private static Optional<BigDecimal> saldoNaConexao(Connection c, long conta) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT saldo FROM conta WHERE id = ?")) {
            ps.setLong(1, conta);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getBigDecimal(1)) : Optional.empty();
            }
        }
    }

    private static void registrar(Connection c, long conta, BigDecimal valor, String descricao) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO movimento (conta_id, valor, descricao) VALUES (?, ?, ?)")) {
            ps.setLong(1, conta);
            ps.setBigDecimal(2, valor);
            ps.setString(3, descricao);
            ps.executeUpdate();
        }
    }
}
