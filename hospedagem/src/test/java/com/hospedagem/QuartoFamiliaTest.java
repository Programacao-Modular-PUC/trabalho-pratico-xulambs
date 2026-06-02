package com.hospedagem;

import com.hospedagem.model.QuartoFamilia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuartoFamiliaTest {

    private QuartoFamilia quarto;

    @BeforeEach
    void setUp() {
        quarto = new QuartoFamilia();
        quarto.setValorBase(1000.0);
        quarto.setPercentualPorHospede(0.05);
        quarto.setCamasIndividuais(4);
        quarto.setCamasCasal(2);
    }

    @Test
    void calcularDiaria_2Hospedes_semDesconto() {
        // valorComHospedes = 1000 * (1 + 0.05*2) = 1100; desconto = 0
        double resultado = quarto.calcularDiaria(2, false);
        assertEquals(1100.0, resultado, 0.01);
    }

    @Test
    void calcularDiaria_4Hospedes_desconto5porcento() {
        // valorComHospedes = 1000 * (1 + 0.05*4) = 1200; desconto = 5%
        double resultado = quarto.calcularDiaria(4, false);
        assertEquals(1140.0, resultado, 0.01);
    }

    @Test
    void calcularDiaria_6Hospedes_desconto10porcento() {
        // valorComHospedes = 1000 * (1 + 0.05*6) = 1300; desconto = 10%
        double resultado = quarto.calcularDiaria(6, false);
        assertEquals(1170.0, resultado, 0.01);
    }

    @Test
    void calcularDiaria_8Hospedes_desconto15porcento() {
        // valorComHospedes = 1000 * (1 + 0.05*8) = 1400; desconto = 15%
        double resultado = quarto.calcularDiaria(8, false);
        assertEquals(1190.0, resultado, 0.01);
    }

    @Test
    void getCapacidadeMaxima_calculaTodasAsCamas() {
        // 4 individuais + 2 casal*2 = 8
        assertEquals(8, quarto.getCapacidadeMaxima());
    }
}
