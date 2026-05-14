package com.hospedagem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "quartos_duplos")
@DiscriminatorValue("DUPLO")
@Getter @Setter @NoArgsConstructor
public class QuartoDuplo extends Quarto {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCama tipoCama = TipoCama.CASAL;

    private boolean possuiBerco = false;
    private double taxaBerco = 0.0;
    private double adicionalCasal = 0.0;
    private double adicionalQueenKing = 0.0;

    @Override
    public double calcularDiaria(int numHospedes, boolean solicitouBerco) {
        double adicionalConforto = (tipoCama == TipoCama.CASAL) ? adicionalCasal : adicionalQueenKing;
        double taxaBercoAplicada = (possuiBerco && solicitouBerco) ? taxaBerco : 0.0;
        return getValorBase() + adicionalConforto + taxaBercoAplicada;
    }

    @Override
    public int getCapacidadeMaxima() {
        return 2;
    }
}
