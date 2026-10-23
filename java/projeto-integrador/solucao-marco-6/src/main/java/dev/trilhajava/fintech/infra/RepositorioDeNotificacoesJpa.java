package dev.trilhajava.fintech.infra;

import dev.trilhajava.fintech.dominio.Notificacao;
import dev.trilhajava.fintech.dominio.RepositorioDeNotificacoes;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;

@Repository
class RepositorioDeNotificacoesJpa implements RepositorioDeNotificacoes {

    private final NotificacaoJpa jpa;

    RepositorioDeNotificacoesJpa(NotificacaoJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(Notificacao notificacao) {
        jpa.save(notificacao);
    }

    @Override
    public List<Notificacao> recentes(UUID usuario, int limite) {
        return jpa.findByUsuarioOrderByCriadaEmDesc(usuario, Limit.of(limite));
    }
}
