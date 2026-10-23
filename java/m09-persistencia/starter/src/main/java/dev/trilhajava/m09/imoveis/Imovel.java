package dev.trilhajava.m09.imoveis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Imovel {

    @Id
    private Long id;

    private String titulo;

    @Enumerated(EnumType.STRING)
    private TipoImovel tipo;

    private String cidade;

    private String bairro;

    private BigDecimal preco;

    @Column(name = "area_m2")
    private int areaM2;

    private int quartos;

    private int vagasGaragem;

    private LocalDate publicadoEm;

    protected Imovel() {
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public TipoImovel getTipo() {
        return tipo;
    }

    public String getCidade() {
        return cidade;
    }

    public String getBairro() {
        return bairro;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public int getAreaM2() {
        return areaM2;
    }

    public int getQuartos() {
        return quartos;
    }

    public int getVagasGaragem() {
        return vagasGaragem;
    }

    public LocalDate getPublicadoEm() {
        return publicadoEm;
    }
}
