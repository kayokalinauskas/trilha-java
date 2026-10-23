package dev.trilhajava.m11.rh;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * Autorização no nível do objeto. Três regras que valem para qualquer API:
 * <ol>
 *   <li>quem não pode VER um recurso recebe 404 — o mesmo de um id inexistente (não confirma que ele existe);</li>
 *   <li>quem pode ver mas não pode MUDAR recebe 403;</li>
 *   <li>a identidade vem do {@link Authentication}, nunca de um parâmetro que o cliente escolhe.</li>
 * </ol>
 */
@Service
public class ServicoDeFuncionarios {

    private final RepositorioDeFuncionarios repositorio;

    public ServicoDeFuncionarios(RepositorioDeFuncionarios repositorio) {
        this.repositorio = repositorio;
    }

    public List<FuncionarioResposta> listar(Authentication quem) {
        Funcionario eu = euMesmo(quem);
        return repositorio.todos().stream()
                .filter(alvo -> podeVer(quem, eu, alvo))
                .map(alvo -> resposta(quem, eu, alvo))
                .toList();
    }

    public FuncionarioResposta buscar(long id, Authentication quem) {
        Funcionario eu = euMesmo(quem);
        Funcionario alvo = visivel(id, quem, eu);
        return resposta(quem, eu, alvo);
    }

    public FuncionarioResposta alterarSalario(long id, BigDecimal novoSalario, Authentication quem) {
        Funcionario eu = euMesmo(quem);
        Funcionario alvo = visivel(id, quem, eu);    // primeiro: pode ver? se não, 404
        if (!temPapel(quem, "RH")) {
            throw new OperacaoNaoPermitidaException("Só o RH altera salários");
        }
        if (alvo.id() == eu.id()) {
            throw new OperacaoNaoPermitidaException("Ninguém altera o próprio salário (segregação de funções)");
        }
        return FuncionarioResposta.comSalario(repositorio.salvar(alvo.comSalario(novoSalario)));
    }

    // ------------------------------------------------------------------ regras

    private Funcionario visivel(long id, Authentication quem, Funcionario eu) {
        return repositorio.porId(id)
                .filter(alvo -> podeVer(quem, eu, alvo))
                .orElseThrow(() -> new FuncionarioNaoEncontradoException(id));
    }

    private static boolean podeVer(Authentication quem, Funcionario eu, Funcionario alvo) {
        return temPapel(quem, "RH")
                || alvo.id() == eu.id()
                || (temPapel(quem, "GESTOR") && Long.valueOf(eu.id()).equals(alvo.gestorId()));
    }

    private static FuncionarioResposta resposta(Authentication quem, Funcionario eu, Funcionario alvo) {
        boolean veSalario = temPapel(quem, "RH") || alvo.id() == eu.id();
        return veSalario ? FuncionarioResposta.comSalario(alvo) : FuncionarioResposta.semSalario(alvo);
    }

    private Funcionario euMesmo(Authentication quem) {
        // Um login sem cadastro de funcionário é um erro de configuração, não um "não encontrado".
        return repositorio.porUsuario(quem.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário sem cadastro de funcionário: " + quem.getName()));
    }

    private static boolean temPapel(Authentication quem, String papel) {
        return quem.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(("ROLE_" + papel)::equals);
    }
}
