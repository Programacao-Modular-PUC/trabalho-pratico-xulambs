package com.hospedagem;

import com.hospedagem.model.QuartoIndividual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuartoIndividualTest {

    private QuartoIndividual quarto;

    @BeforeEach
    void setUp() {
        quarto = new QuartoIndividual();
        quarto.setValorBase(100.0);
        quarto.setNumeroCamas(1);
        quarto.setAdicionalPorCama(30.0);
    }

    @Test
    void calcularDiaria_umaCAma_retornaValorBase() {
        double resultado = quarto.calcularDiaria(1, false);
        assertEquals(100.0, resultado);
    }

    @Test
    void calcularDiaria_duasCamas_retornaValorBaseComAdicional() {
        quarto.setNumeroCamas(2);
        double resultado = quarto.calcularDiaria(2, false);
        assertEquals(130.0, resultado);
    }

    @Test
    void calcularDiaria_tresCamas_retornaValorBaseComDoisAdicionais() {
        quarto.setNumeroCamas(3);
        double resultado = quarto.calcularDiaria(3, false);
        assertEquals(160.0, resultado);
    }

    @Test
    void getCapacidadeMaxima_umaCama_retornaUm() {
        assertEquals(1, quarto.getCapacidadeMaxima());
    }

    @Test
    void getCapacidadeMaxima_retornaNumeroDeCamas() {
        quarto.setNumeroCamas(2);
        assertEquals(2, quarto.getCapacidadeMaxima());
    }

    @Test
    void calcularDiaria_ignoraBerco_retornaMesmoValor() {
        double semBerco = quarto.calcularDiaria(1, false);
        double comBerco = quarto.calcularDiaria(1, true);
        assertEquals(semBerco, comBerco);
    }
}
