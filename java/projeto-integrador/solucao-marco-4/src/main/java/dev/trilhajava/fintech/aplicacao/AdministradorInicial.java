package dev.trilhajava.fintech.aplicacao;

import dev.trilhajava.fintech.dominio.Papel;
import dev.trilhajava.fintech.dominio.RepositorioDeUsuarios;
import dev.trilhajava.fintech.dominio.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Cria o primeiro ADMIN a partir de fintech.admin.email / fintech.admin.senha (variáveis de ambiente
 * em produção). Nada de usuário e senha "admin/admin" no código ou numa migração SQL.
 */
@Component
public class AdministradorInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdministradorInicial.class);

    private final ServicoDeUsuarios servico;
    private final RepositorioDeUsuarios usuarios;
    private final String email;
    private final String senha;

    public AdministradorInicial(ServicoDeUsuarios servico, RepositorioDeUsuarios usuarios,
            @Value("${fintech.admin.email:}") String email, @Value("${fintech.admin.senha:}") String senha) {
        this.servico = servico;
        this.usuarios = usuarios;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(ApplicationArguments argumentos) {
        if (email.isBlank() || senha.isBlank()) {
            log.info("fintech.admin.* não configurado: nenhum administrador inicial criado");
            return;
        }
        if (usuarios.existePorEmail(Usuario.normalizarEmail(email))) {
            return;
        }
        servico.criar("Administrador", email, senha, Papel.ADMIN);
        log.info("Administrador inicial criado: {}", email);   // o e-mail sim; a senha, nunca
    }
}
