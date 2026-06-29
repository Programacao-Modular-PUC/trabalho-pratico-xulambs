package com.hospedagem.relatorio;

import com.hospedagem.model.Aluguel;
import com.hospedagem.model.Quarto;
import com.hospedagem.model.StatusAluguel;
import com.hospedagem.repository.AluguelRepository;
import com.hospedagem.repository.QuartoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RelatorioTaxaOcupacao extends RelatorioGerencial {

    private final AluguelRepository aluguelRepository;
    private final QuartoRepository quartoRepository;

    @Override
    public String getNome() {
        return "Taxa de Ocupação";
    }

    @Override
    protected List<?> coletarDados() {
        List<Object> dados = new ArrayList<>();
        dados.add(aluguelRepository.findAll());
        dados.add(quartoRepository.findAll());
        return dados;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Map<String, Object> processar(List<?> dados) {
        List<Aluguel> alugueis = (List<Aluguel>) dados.get(0);
        List<Quarto> quartos   = (List<Quarto>)  dados.get(1);

        long totalQuartos = quartos.size();
        long quartosOcupados = alugueis.stream()
                .filter(a -> a.getStatus() == StatusAluguel.ATIVO)
                .map(a -> a.getQuarto().getId())
                .collect(Collectors.toSet())
                .size();

        double taxa = totalQuartos == 0 ? 0 : (quartosOcupados * 100.0) / totalQuartos;

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("relatorio", getNome());
        resultado.put("totalQuartos", totalQuartos);
        resultado.put("quartosOcupados", quartosOcupados);
        resultado.put("taxaOcupacaoPercent", String.format("%.1f%%", taxa));
        return resultado;
    }
}
