package com.hospedagem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "quartos_individuais")
@DiscriminatorValue("INDIVIDUAL")
@Getter @Setter @NoArgsConstructor
public class QuartoIndividual extends Quarto {

    @Column(nullable = false)
    private int numeroCamas = 1;

    private double adicionalPorCama = 0.0;

    @Override
    public double calcularDiaria(int numHospedes, boolean solicitouBerco) {
        if (numeroCamas <= 1) {
            return getValorBase();
        }
        return getValorBase() + (numeroCamas - 1) * adicionalPorCama;
    }

    @Override
    public int getCapacidadeMaxima() {
        return numeroCamas;
    }
}
