package com.hospedagem;

import com.hospedagem.exception.CapacidadeExcedidaException;
import com.hospedagem.exception.DataInvalidaException;
import com.hospedagem.exception.QuartoIndisponivelException;
import com.hospedagem.exception.RecursoNaoPermitidoException;
import com.hospedagem.model.*;
import com.hospedagem.repository.AluguelRepository;
import com.hospedagem.repository.ClienteRepository;
import com.hospedagem.repository.QuartoRepository;
import com.hospedagem.service.AluguelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AluguelServiceTest {

    @Mock
    private AluguelRepository aluguelRepository;

    @Mock
    private QuartoRepository quartoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private AluguelService aluguelService;

    private Cliente cliente;
    private QuartoDuplo quartoDuplo;
    private QuartoIndividual quartoIndividual;
    private Aluguel aluguel;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João");
        cliente.setCpf("123.456.789-00");

        quartoDuplo = new QuartoDuplo();
        quartoDuplo.setId(1L);
        quartoDuplo.setValorBase(200.0);
        quartoDuplo.setAdicionalCasal(50.0);
        quartoDuplo.setTipoCama(TipoCama.CASAL);
        quartoDuplo.setPossuiBerco(true);
        quartoDuplo.setTaxaBerco(40.0);

        quartoIndividual = new QuartoIndividual();
        quartoIndividual.setId(2L);
        quartoIndividual.setValorBase(100.0);
        quartoIndividual.setNumeroCamas(1);

        aluguel = new Aluguel();
        aluguel.setCliente(cliente);
        aluguel.setQuarto(quartoDuplo);
        aluguel.setDataInicio(LocalDate.of(2026, 7, 1));
        aluguel.setDataFim(LocalDate.of(2026, 7, 5));
        aluguel.setNumHospedes(2);
        aluguel.setSolicitouBerco(false);
    }

    @Test
    void criar_dadosValidos_retornaAluguelComValorTotal() {
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quartoDuplo));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(aluguelRepository.findConflitosReserva(anyLong(), any(), any(), any()))
                .thenReturn(List.of());
        when(aluguelRepository.save(any(Aluguel.class))).thenAnswer(inv -> inv.getArgument(0));

        Aluguel resultado = aluguelService.criar(aluguel);

        // 4 dias * diária (200 + 50 adicional casal) = 4 * 250 = 1000
        assertEquals(1000.0, resultado.getValorTotal());
        assertEquals(StatusAluguel.ATIVO, resultado.getStatus());
    }

    @Test
    void criar_capacidadeExcedida_lancaCapacidadeExcedidaException() {
        aluguel.setNumHospedes(5);
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quartoDuplo));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        assertThrows(CapacidadeExcedidaException.class, () -> aluguelService.criar(aluguel));
    }

    @Test
    void criar_dataFimAnteriorDataInicio_lancaDataInvalidaException() {
        aluguel.setDataFim(LocalDate.of(2026, 6, 30));
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quartoDuplo));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        assertThrows(DataInvalidaException.class, () -> aluguelService.criar(aluguel));
    }

    @Test
    void criar_dataInicioIgualDataFim_lancaDataInvalidaException() {
        aluguel.setDataFim(aluguel.getDataInicio());
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quartoDuplo));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        assertThrows(DataInvalidaException.class, () -> aluguelService.criar(aluguel));
    }

    @Test
    void criar_quartoIndisponivel_lancaQuartoIndisponivelException() {
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quartoDuplo));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(aluguelRepository.findConflitosReserva(anyLong(), any(), any(), any()))
                .thenReturn(List.of(new Aluguel()));

        assertThrows(QuartoIndisponivelException.class, () -> aluguelService.criar(aluguel));
    }

    @Test
    void criar_bercoemQuartoIndividual_lancaRecursoNaoPermitidoException() {
        aluguel.setQuarto(quartoIndividual);
        aluguel.setSolicitouBerco(true);
        aluguel.setNumHospedes(1);
        when(quartoRepository.findById(2L)).thenReturn(Optional.of(quartoIndividual));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        assertThrows(RecursoNaoPermitidoException.class, () -> aluguelService.criar(aluguel));
    }

    @Test
    void criar_bercoemQuartoDuploSemBerco_lancaRecursoNaoPermitidoException() {
        quartoDuplo.setPossuiBerco(false);
        aluguel.setSolicitouBerco(true);
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quartoDuplo));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        assertThrows(RecursoNaoPermitidoException.class, () -> aluguelService.criar(aluguel));
    }

    @Test
    void cancelar_aluguelAtivo_mudaStatusParaCancelado() {
        Aluguel aluguelExistente = new Aluguel();
        aluguelExistente.setId(1L);
        aluguelExistente.setStatus(StatusAluguel.ATIVO);
        when(aluguelRepository.findById(1L)).thenReturn(Optional.of(aluguelExistente));
        when(aluguelRepository.save(any(Aluguel.class))).thenAnswer(inv -> inv.getArgument(0));

        Aluguel resultado = aluguelService.cancelar(1L);

        assertEquals(StatusAluguel.CANCELADO, resultado.getStatus());
    }

    @Test
    void cancelar_aluguelJaCancelado_lancaRecursoNaoPermitidoException() {
        Aluguel aluguelCancelado = new Aluguel();
        aluguelCancelado.setId(1L);
        aluguelCancelado.setStatus(StatusAluguel.CANCELADO);
        when(aluguelRepository.findById(1L)).thenReturn(Optional.of(aluguelCancelado));

        assertThrows(RecursoNaoPermitidoException.class, () -> aluguelService.cancelar(1L));
    }
}
