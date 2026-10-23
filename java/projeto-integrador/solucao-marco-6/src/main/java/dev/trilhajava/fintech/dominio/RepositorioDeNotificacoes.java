package dev.trilhajava.fintech.dominio;

import java.util.List;
import java.util.UUID;

/** Marco 5. */
public interface RepositorioDeNotificacoes {

    void salvar(Notificacao notificacao);

    /** As mais recentes primeiro. */
    List<Notificacao> recentes(UUID usuario, int limite);
}
