package dev.trilhajava.fintech.api;

import dev.trilhajava.fintech.aplicacao.ServicoDeNotificacoes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notificacoes")
@Tag(name = "Notificações", description = "Avisos gerados de forma assíncrona a partir dos eventos")
public class NotificacaoController {

    public record Lista(List<NotificacaoResponse> itens) {
    }

    private final ServicoDeNotificacoes servico;

    public NotificacaoController(ServicoDeNotificacoes servico) {
        this.servico = servico;
    }

    @GetMapping
    @Operation(summary = "As 50 notificações mais recentes de quem está autenticado",
            description = "Consistência eventual: uma transferência aparece aqui alguns instantes depois de concluída.")
    public Lista minhas(Authentication autenticacao) {
        return new Lista(servico.recentes(Solicitantes.de(autenticacao)).stream().map(NotificacaoResponse::de).toList());
    }
}
