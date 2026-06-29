package com.hospedagem.controller;

import com.hospedagem.relatorio.RelatorioClientesFrequentes;
import com.hospedagem.relatorio.RelatorioFaturamentoMensal;
import com.hospedagem.relatorio.RelatorioTaxaOcupacao;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioFaturamentoMensal relatorioFaturamento;
    private final RelatorioTaxaOcupacao relatorioOcupacao;
    private final RelatorioClientesFrequentes relatorioClientes;

    @GetMapping("/faturamento")
    public Map<String, Object> faturamento() {
        return relatorioFaturamento.gerar();
    }

    @GetMapping("/ocupacao")
    public Map<String, Object> ocupacao() {
        return relatorioOcupacao.gerar();
    }

    @GetMapping("/clientes-frequentes")
    public Map<String, Object> clientesFrequentes() {
        return relatorioClientes.gerar();
    }
}
