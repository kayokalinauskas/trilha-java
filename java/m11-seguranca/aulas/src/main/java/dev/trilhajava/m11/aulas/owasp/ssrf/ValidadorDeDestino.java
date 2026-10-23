package dev.trilhajava.m11.aulas.owasp.ssrf;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Set;

/**
 * Valida a URL de webhook que um lojista cadastra. A ordem das verificações importa:
 * 1) a URL é interpretada por um parser (URI), nunca comparada como texto;
 * 2) esquema, porta e ausência de usuário/senha na URL;
 * 3) o host está na allowlist de domínios;
 * 4) TODOS os IPs para os quais o nome resolve são públicos.
 * O resultado traz o IP aprovado: quem faz a chamada deve conectar NESSE IP (evita DNS rebinding).
 */
public class ValidadorDeDestino {

    /** Um destino aprovado: a URL e o endereço que foi conferido. */
    public record DestinoAprovado(URI uri, InetAddress endereco) {
    }

    private final Set<String> dominiosPermitidos;
    private final ResolvedorDeNomes dns;

    /**
     * @param dominiosPermitidos domínios exatos ("api.parceiro.com.br") ou com curinga só no começo
     *                           ("*.parceiro.com.br", que NÃO inclui o próprio "parceiro.com.br")
     */
    public ValidadorDeDestino(Set<String> dominiosPermitidos, ResolvedorDeNomes dns) {
        this.dominiosPermitidos = Set.copyOf(dominiosPermitidos);
        this.dns = dns;
    }

    public DestinoAprovado validar(String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new DestinoRecusadoException("URL malformada");
        }
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new DestinoRecusadoException("só https é aceito");
        }
        if (uri.getRawUserInfo() != null) {
            throw new DestinoRecusadoException("URL com usuário/senha");
        }
        if (uri.getPort() != -1 && uri.getPort() != 443) {
            throw new DestinoRecusadoException("porta não permitida: " + uri.getPort());
        }
        String host = uri.getHost();
        if (host == null) {
            throw new DestinoRecusadoException("URL sem host");
        }
        host = host.toLowerCase(Locale.ROOT);
        if (host.endsWith(".")) {
            host = host.substring(0, host.length() - 1);
        }
        if (!dominioPermitido(host)) {
            throw new DestinoRecusadoException("domínio fora da lista: " + host);
        }
        InetAddress[] enderecos;
        try {
            enderecos = dns.resolver(host);
        } catch (UnknownHostException e) {
            throw new DestinoRecusadoException("domínio não resolve: " + host);
        }
        for (InetAddress endereco : enderecos) {
            if (!publico(endereco)) {
                throw new DestinoRecusadoException(host + " resolve para endereço interno " + endereco.getHostAddress());
            }
        }
        return new DestinoAprovado(uri, enderecos[0]);
    }

    private boolean dominioPermitido(String host) {
        for (String permitido : dominiosPermitidos) {
            if (permitido.startsWith("*.")) {
                if (host.endsWith(permitido.substring(1))) {
                    return true;
                }
            } else if (host.equals(permitido)) {
                return true;
            }
        }
        return false;
    }

    /** Público = nenhuma das faixas privadas, locais, de loopback ou de metadados de nuvem. */
    static boolean publico(InetAddress ip) {
        if (ip.isAnyLocalAddress() || ip.isLoopbackAddress() || ip.isLinkLocalAddress()
                || ip.isSiteLocalAddress() || ip.isMulticastAddress()) {
            return false; // 0.0.0.0, 127/8, ::1, 169.254/16 (metadados), fe80::/10, 10/8, 172.16/12, 192.168/16
        }
        byte[] b = ip.getAddress();
        if (ip instanceof Inet4Address) {
            int primeiro = b[0] & 0xff;
            int segundo = b[1] & 0xff;
            boolean redeZero = primeiro == 0;                                  // 0.0.0.0/8
            boolean cgnat = primeiro == 100 && segundo >= 64 && segundo <= 127; // 100.64.0.0/10
            return !redeZero && !cgnat;
        }
        if (ip instanceof Inet6Address) {
            boolean unicoLocal = (b[0] & 0xfe) == 0xfc;                         // fc00::/7
            return !unicoLocal;
        }
        return false;
    }
}
