package com.hospedagem.relatorio;

import com.hospedagem.model.Aluguel;
import com.hospedagem.model.StatusAluguel;
import com.hospedagem.repository.AluguelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RelatorioFaturamentoMensal extends RelatorioGerencial {

    private final AluguelRepository aluguelRepository;

    @Override
    public String getNome() {
        return "Faturamento Total";
    }

    @Override
    protected List<?> coletarDados() {
        return aluguelRepository.findAll();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Map<String, Object> processar(List<?> dados) {
        List<Aluguel> alugueis = (List<Aluguel>) dados;

        double faturamento = alugueis.stream()
                .filter(a -> a.getStatus() != StatusAluguel.CANCELADO)
                .mapToDouble(Aluguel::getValorTotal)
                .sum();

        long totalAtivos = alugueis.stream()
                .filter(a -> a.getStatus() == StatusAluguel.ATIVO)
                .count();

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("relatorio", getNome());
        resultado.put("faturamentoTotal", faturamento);
        resultado.put("totalReservasAtivas", totalAtivos);
        resultado.put("totalReservas", alugueis.size());
        return resultado;
    }
}
