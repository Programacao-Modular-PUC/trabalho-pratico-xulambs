package com.hospedagem.relatorio;

import com.hospedagem.model.Aluguel;
import com.hospedagem.repository.AluguelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RelatorioClientesFrequentes extends RelatorioGerencial {

    private final AluguelRepository aluguelRepository;

    @Override
    public String getNome() {
        return "Clientes Mais Frequentes";
    }

    @Override
    protected List<?> coletarDados() {
        return aluguelRepository.findAll();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Map<String, Object> processar(List<?> dados) {
        List<Aluguel> alugueis = (List<Aluguel>) dados;

        List<Map<String, Object>> ranking = alugueis.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getCliente().getId(),
                        Collectors.toList()
                ))
                .entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .limit(10)
                .map(entry -> {
                    Aluguel primeiro = entry.getValue().get(0);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("clienteId", primeiro.getCliente().getId());
                    item.put("nome", primeiro.getCliente().getNome());
                    item.put("totalReservas", entry.getValue().size());
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("relatorio", getNome());
        resultado.put("ranking", ranking);
        return resultado;
    }
}
