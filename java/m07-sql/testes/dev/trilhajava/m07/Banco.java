package dev.trilhajava.m07;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.h2.jdbcx.JdbcDataSource;

/**
 * Banco H2 em memória em modo PostgreSQL, um por teste. Usado pelos testes do módulo 07.
 * (No módulo 10 você troca isso por um PostgreSQL de verdade com Testcontainers.)
 */
public final class Banco implements AutoCloseable {

    private final JdbcDataSource dataSource = new JdbcDataSource();
    private final Connection conexaoMantida; // mantém o banco em memória vivo durante o teste

    public Banco() {
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH");
        try {
            conexaoMantida = dataSource.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public JdbcDataSource dataSource() {
        return dataSource;
    }

    /** Executa um script com vários comandos separados por ';'. */
    public void executar(String script) throws SQLException {
        try (Statement st = conexaoMantida.createStatement()) {
            for (String comando : removerComentarios(script).split(";")) {
                String limpo = comando.strip();
                if (!limpo.isEmpty()) {
                    st.execute(limpo);
                }
            }
        }
    }

    public void executarArquivo(String caminhoNoClasspath) throws SQLException {
        executar(ler(caminhoNoClasspath));
    }

    /** Linhas do resultado; cada linha é a lista de colunas, já convertidas para String (null continua null). */
    public List<List<String>> consultar(String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = conexaoMantida.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                List<List<String>> linhas = new ArrayList<>();
                while (rs.next()) {
                    List<String> linha = new ArrayList<>();
                    for (int c = 1; c <= meta.getColumnCount(); c++) {
                        Object v = rs.getObject(c);
                        linha.add(v == null ? null : normalizar(v));
                    }
                    linhas.add(linha);
                }
                return linhas;
            }
        }
    }

    public List<List<String>> consultarArquivo(String caminhoNoClasspath) throws SQLException {
        return consultar(removerComentarios(ler(caminhoNoClasspath)).strip().replaceAll(";\\s*$", ""));
    }

    public List<String> nomesDasColunas(String caminhoNoClasspath) throws SQLException {
        String sql = removerComentarios(ler(caminhoNoClasspath)).strip().replaceAll(";\\s*$", "");
        try (PreparedStatement ps = conexaoMantida.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<String> nomes = new ArrayList<>();
            for (int c = 1; c <= rs.getMetaData().getColumnCount(); c++) {
                nomes.add(rs.getMetaData().getColumnLabel(c).toLowerCase());
            }
            return nomes;
        }
    }

    private static String normalizar(Object v) {
        if (v instanceof java.math.BigDecimal bd) {
            return bd.stripTrailingZeros().toPlainString();
        }
        if (v instanceof Double || v instanceof Float) {
            return new java.math.BigDecimal(v.toString()).stripTrailingZeros().toPlainString();
        }
        return v.toString();
    }

    private static String removerComentarios(String sql) {
        return sql.replaceAll("(?m)--.*$", "");
    }

    public static String ler(String caminho) {
        try (InputStream in = Banco.class.getClassLoader().getResourceAsStream(caminho)) {
            if (in == null) {
                throw new IllegalStateException("Arquivo não encontrado no classpath: " + caminho);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public Connection novaConexao() throws SQLException {
        return DriverManager.getConnection(dataSource.getURL());
    }

    @Override
    public void close() throws SQLException {
        conexaoMantida.close();
    }
}
