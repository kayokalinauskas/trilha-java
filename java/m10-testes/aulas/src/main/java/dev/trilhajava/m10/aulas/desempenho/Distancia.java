package dev.trilhajava.m10.aulas.desempenho;

/**
 * Distância em linha reta entre dois pontos do globo (fórmula de haversine), usada para estimar
 * a distância do centro de distribuição até o endereço de entrega. É uma conta pura e barata:
 * o tipo de código em que um microbenchmark mal escrito mede o nada.
 */
public final class Distancia {

    private static final double RAIO_DA_TERRA_KM = 6371.0;

    private Distancia() {
    }

    public static double km(double latOrigem, double lonOrigem, double latDestino, double lonDestino) {
        double dLat = Math.toRadians(latDestino - latOrigem);
        double dLon = Math.toRadians(lonDestino - lonOrigem);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(latOrigem)) * Math.cos(Math.toRadians(latDestino))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * RAIO_DA_TERRA_KM * Math.asin(Math.sqrt(a));
    }

    /**
     * Distância em linha reta numa grade plana em quilômetros (a cidade projetada num mapa).
     * Serve para distâncias curtas, dentro de uma mesma cidade, e é bem mais barata que a haversine.
     */
    public static double kmNaGrade(double xOrigem, double yOrigem, double xDestino, double yDestino) {
        double dx = xDestino - xOrigem;
        double dy = yDestino - yOrigem;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
