package com.hospedagem.service;

import com.hospedagem.model.Aluguel;
import com.hospedagem.model.Cliente;
import com.hospedagem.model.Quarto;
import com.hospedagem.repository.AluguelRepository;
import com.hospedagem.repository.ClienteRepository;
import com.hospedagem.repository.QuartoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AluguelService {

    private final AluguelRepository aluguelRepository;
    private final QuartoRepository quartoRepository;
    private final ClienteRepository clienteRepository;

    public List<Aluguel> listar() {
        return aluguelRepository.findAll();
    }

    public Aluguel buscarPorId(Long id) {
        return aluguelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluguel não encontrado"));
    }

    public List<Aluguel> listarPorCliente(Long clienteId) {
        return aluguelRepository.findByClienteId(clienteId);
    }

    public Aluguel criar(Aluguel aluguel) {
        Quarto quarto = quartoRepository.findById(aluguel.getQuarto().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quarto não encontrado"));

        Cliente cliente = clienteRepository.findById(aluguel.getCliente().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));

        if (aluguel.getNumHospedes() > quarto.getCapacidadeMaxima()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Número de hóspedes (" + aluguel.getNumHospedes() +
                ") excede a capacidade máxima do quarto (" + quarto.getCapacidadeMaxima() + ")");
        }

        if (aluguel.getDataFim().isBefore(aluguel.getDataInicio()) ||
            aluguel.getDataFim().isEqual(aluguel.getDataInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Data de fim deve ser posterior à data de início");
        }

        long numDias = ChronoUnit.DAYS.between(aluguel.getDataInicio(), aluguel.getDataFim());
        double valorDiaria = quarto.calcularDiaria(aluguel.getNumHospedes(), aluguel.isSolicitouBerco());

        aluguel.setQuarto(quarto);
        aluguel.setCliente(cliente);
        aluguel.setValorTotal(valorDiaria * numDias);

        return aluguelRepository.save(aluguel);
    }

    public void deletar(Long id) {
        buscarPorId(id);
        aluguelRepository.deleteById(id);
    }
}
