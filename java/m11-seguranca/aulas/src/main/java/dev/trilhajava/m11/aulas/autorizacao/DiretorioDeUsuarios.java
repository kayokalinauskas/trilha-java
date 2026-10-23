package dev.trilhajava.m11.aulas.autorizacao;

import java.util.Map;
import org.springframework.stereotype.Component;

/** Quem trabalha em qual clínica. Em produção, viria do serviço de identidade ou de uma tabela. */
@Component
public class DiretorioDeUsuarios {

    private record Vinculo(String clinicaId, Papel papel) {}

    private final Map<String, Vinculo> usuarios = Map.of(
            "med-carla", new Vinculo("sol", Papel.MEDICO),
            "med-diego", new Vinculo("sol", Papel.MEDICO),
            "med-heitor", new Vinculo("lua", Papel.MEDICO),
            "pac-ana", new Vinculo("sol", Papel.PACIENTE),
            "pac-bruno", new Vinculo("sol", Papel.PACIENTE),
            "pac-julia", new Vinculo("lua", Papel.PACIENTE));

    public boolean ehDaClinica(String usuario, String clinicaId, Papel papel) {
        Vinculo vinculo = usuarios.get(usuario);
        return vinculo != null && vinculo.clinicaId().equals(clinicaId) && vinculo.papel() == papel;
    }
}
