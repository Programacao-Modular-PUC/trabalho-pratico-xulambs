package com.hospedagem.service;

import com.hospedagem.exception.CapacidadeExcedidaException;
import com.hospedagem.exception.DataInvalidaException;
import com.hospedagem.exception.QuartoIndisponivelException;
import com.hospedagem.exception.RecursoNaoPermitidoException;
import com.hospedagem.model.*;
import com.hospedagem.notificacao.EventoAluguel;
import com.hospedagem.notificacao.GerenciadorNotificacoes;
import com.hospedagem.repository.AluguelRepository;
import com.hospedagem.repository.ClienteRepository;
import com.hospedagem.repository.QuartoRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AluguelService {

    private final AluguelRepository aluguelRepository;
    private final QuartoRepository quartoRepository;
    private final ClienteRepository clienteRepository;
    private final GerenciadorNotificacoes gerenciadorNotificacoes;

    public List<Aluguel> listar() {
        List<Aluguel> alugueis = aluguelRepository.findAll();
        alugueis.forEach(a -> a.setQuarto((Quarto) Hibernate.unproxy(a.getQuarto())));
        return alugueis;
    }

    public Aluguel buscarPorId(Long id) {
        Aluguel a = aluguelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluguel não encontrado"));
        a.setQuarto((Quarto) Hibernate.unproxy(a.getQuarto()));
        return a;
    }

    public List<Aluguel> listarPorCliente(Long clienteId) {
        List<Aluguel> alugueis = aluguelRepository.findByClienteIdAndStatus(clienteId, StatusAluguel.ATIVO);
        alugueis.forEach(a -> a.setQuarto((Quarto) Hibernate.unproxy(a.getQuarto())));
        return alugueis;
    }

    public List<Aluguel> listarHistoricoPorCliente(Long clienteId) {
        List<Aluguel> alugueis = aluguelRepository.findByClienteId(clienteId);
        alugueis.forEach(a -> a.setQuarto((Quarto) Hibernate.unproxy(a.getQuarto())));
        return alugueis;
    }

    public Aluguel criar(Aluguel aluguel) {
        Quarto quarto = quartoRepository.findById(aluguel.getQuarto().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quarto não encontrado"));

        Cliente cliente = clienteRepository.findById(aluguel.getCliente().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));

        validarDatas(aluguel.getDataInicio(), aluguel.getDataFim());
        validarCapacidade(aluguel.getNumHospedes(), quarto);
        validarBerco(aluguel.isSolicitouBerco(), quarto);
        validarDisponibilidade(quarto.getId(), aluguel.getDataInicio(), aluguel.getDataFim());

        long numDias = ChronoUnit.DAYS.between(aluguel.getDataInicio(), aluguel.getDataFim());
        double valorDiaria = quarto.calcularDiaria(aluguel.getNumHospedes(), aluguel.isSolicitouBerco());

        aluguel.setQuarto(quarto);
        aluguel.setCliente(cliente);
        aluguel.setValorTotal(valorDiaria * numDias);
        aluguel.setStatus(StatusAluguel.ATIVO);

        Aluguel salvo = aluguelRepository.save(aluguel);
        gerenciadorNotificacoes.notificar(EventoAluguel.RESERVA_CRIADA, salvo);
        return salvo;
    }

    public Aluguel cancelar(Long id) {
        Aluguel aluguel = buscarPorId(id);
        if (aluguel.getStatus() == StatusAluguel.CANCELADO) {
            throw new RecursoNaoPermitidoException("Aluguel já está cancelado");
        }
        aluguel.setStatus(StatusAluguel.CANCELADO);
        Aluguel cancelado = aluguelRepository.save(aluguel);
        gerenciadorNotificacoes.notificar(EventoAluguel.RESERVA_CANCELADA, cancelado);
        return cancelado;
    }

    public void deletar(Long id) {
        buscarPorId(id);
        aluguelRepository.deleteById(id);
    }

    private void validarDatas(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new DataInvalidaException("Datas de início e fim são obrigatórias");
        }
        if (!dataFim.isAfter(dataInicio)) {
            throw new DataInvalidaException("Data de fim deve ser posterior à data de início");
        }
    }

    private void validarCapacidade(int numHospedes, Quarto quarto) {
        if (numHospedes > quarto.getCapacidadeMaxima()) {
            throw new CapacidadeExcedidaException(
                "Número de hóspedes (" + numHospedes + ") excede a capacidade máxima do quarto (" +
                quarto.getCapacidadeMaxima() + ")");
        }
    }

    private void validarBerco(boolean solicitouBerco, Quarto quarto) {
        if (!solicitouBerco) return;
        if (quarto instanceof QuartoIndividual) {
            throw new RecursoNaoPermitidoException("Berço não é permitido em quarto individual");
        }
        if (quarto instanceof QuartoDuplo duplo && !duplo.isPossuiBerco()) {
            throw new RecursoNaoPermitidoException("Este quarto duplo não possui berço disponível");
        }
    }

    private void validarDisponibilidade(Long quartoId, LocalDate dataInicio, LocalDate dataFim) {
        List<Aluguel> conflitos = aluguelRepository.findConflitosReserva(
                quartoId, dataInicio, dataFim, StatusAluguel.ATIVO);
        if (!conflitos.isEmpty()) {
            throw new QuartoIndisponivelException(
                "Quarto indisponível para o período de " + dataInicio + " a " + dataFim);
        }
    }
}
