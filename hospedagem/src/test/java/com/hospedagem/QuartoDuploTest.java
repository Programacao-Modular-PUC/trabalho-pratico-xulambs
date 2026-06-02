package com.hospedagem;

import com.hospedagem.model.QuartoDuplo;
import com.hospedagem.model.TipoCama;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuartoDuploTest {

    private QuartoDuplo quarto;

    @BeforeEach
    void setUp() {
        quarto = new QuartoDuplo();
        quarto.setValorBase(200.0);
        quarto.setTipoCama(TipoCama.CASAL);
        quarto.setAdicionalCasal(50.0);
        quarto.setAdicionalQueenKing(80.0);
        quarto.setPossuiBerco(true);
        quarto.setTaxaBerco(40.0);
    }

    @Test
    void calcularDiaria_tipoCasal_semBerco_retornaValorComAdicionalCasal() {
        double resultado = quarto.calcularDiaria(2, false);
        assertEquals(250.0, resultado);
    }

    @Test
    void calcularDiaria_tipoCasal_comBerco_possuiBerco_retornaValorComTaxaBerco() {
        double resultado = quarto.calcularDiaria(2, true);
        assertEquals(290.0, resultado);
    }

    @Test
    void calcularDiaria_comBerco_semPossuirBerco_naoCobraTaxaBerco() {
        quarto.setPossuiBerco(false);
        double resultado = quarto.calcularDiaria(2, true);
        assertEquals(250.0, resultado);
    }

    @Test
    void calcularDiaria_tipoQueen_retornaValorComAdicionalQueenKing() {
        quarto.setTipoCama(TipoCama.QUEEN);
        double resultado = quarto.calcularDiaria(2, false);
        assertEquals(280.0, resultado);
    }

    @Test
    void calcularDiaria_tipoKing_retornaValorComAdicionalQueenKing() {
        quarto.setTipoCama(TipoCama.KING);
        double resultado = quarto.calcularDiaria(2, false);
        assertEquals(280.0, resultado);
    }

    @Test
    void getCapacidadeMaxima_retornaDois() {
        assertEquals(2, quarto.getCapacidadeMaxima());
    }
}
