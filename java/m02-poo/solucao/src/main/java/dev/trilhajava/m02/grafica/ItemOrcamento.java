package dev.trilhajava.m02.grafica;

/** Uma forma com a quantidade pedida. Imutável — um record faria o mesmo papel. */
public final class ItemOrcamento {

    private final Forma forma;
    private final int quantidade;

    public ItemOrcamento(Forma forma, int quantidade) {
        if (forma == null) {
            throw new IllegalArgumentException("Forma é obrigatória");
        }
        if (quantidade < 1) {
            throw new IllegalArgumentException("Quantidade deve ser pelo menos 1, recebido: " + quantidade);
        }
        this.forma = forma;
        this.quantidade = quantidade;
    }

    public Forma forma() {
        return forma;
    }

    public int quantidade() {
        return quantidade;
    }

    public double areaTotal() {
        return forma.area() * quantidade;
    }
}
