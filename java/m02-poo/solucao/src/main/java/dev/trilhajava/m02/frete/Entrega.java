package dev.trilhajava.m02.frete;

/** Hierarquia fechada: o compilador conhece todas as modalidades. */
public sealed interface Entrega permits Retirada, EntregaPadrao, EntregaExpressa, EntregaAgendada {

    double PESO_MAXIMO_KG = 30.0;

    /** Regra de peso compartilhada pelas modalidades com transporte (métodos static em interface são permitidos). */
    static double validarPeso(double pesoKg) {
        if (!(pesoKg > 0) || pesoKg > PESO_MAXIMO_KG) {
            throw new IllegalArgumentException("Peso deve estar entre 0 e " + PESO_MAXIMO_KG + " kg, recebido: " + pesoKg);
        }
        return pesoKg;
    }

    static Cep exigirCep(Cep cep) {
        if (cep == null) {
            throw new IllegalArgumentException("CEP é obrigatório");
        }
        return cep;
    }
}
