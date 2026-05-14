package com.hospedagem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quartos_familia")
@DiscriminatorValue("FAMILIA")
@Getter @Setter @NoArgsConstructor
public class QuartoFamilia extends Quarto {

    private int camasIndividuais = 0;
    private int camasCasal = 0;
    private int camasQueenKing = 0;

    private double percentualPorHospede = 0.05;

    @ElementCollection
    @CollectionTable(name = "quarto_familia_ambientes", joinColumns = @JoinColumn(name = "quarto_id"))
    @Column(name = "ambiente")
    private List<String> ambientes = new ArrayList<>();

    @Override
    public double calcularDiaria(int numHospedes, boolean solicitouBerco) {
        double valorComHospedes = getValorBase() * (1 + percentualPorHospede * numHospedes);
        double desconto = calcularDescontoProgressivo(numHospedes);
        return valorComHospedes * (1 - desconto);
    }

    private double calcularDescontoProgressivo(int numHospedes) {
        if (numHospedes >= 8) return 0.15;
        if (numHospedes >= 6) return 0.10;
        if (numHospedes >= 4) return 0.05;
        return 0.0;
    }

    @Override
    public int getCapacidadeMaxima() {
        return camasIndividuais + (camasCasal * 2) + (camasQueenKing * 2);
    }
}
