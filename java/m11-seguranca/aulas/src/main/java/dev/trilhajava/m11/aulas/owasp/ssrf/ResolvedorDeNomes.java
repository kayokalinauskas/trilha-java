package dev.trilhajava.m11.aulas.owasp.ssrf;

import java.net.InetAddress;
import java.net.UnknownHostException;

/** Resolve um nome em endereços IP. Em produção, o DNS do sistema; nos testes, uma tabela fixa. */
@FunctionalInterface
public interface ResolvedorDeNomes {

    InetAddress[] resolver(String host) throws UnknownHostException;

    static ResolvedorDeNomes doSistema() {
        return InetAddress::getAllByName;
    }
}
