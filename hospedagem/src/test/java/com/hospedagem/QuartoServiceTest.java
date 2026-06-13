package com.hospedagem;

import com.hospedagem.model.*;
import com.hospedagem.repository.QuartoRepository;
import com.hospedagem.repository.ResidenciaRepository;
import com.hospedagem.service.QuartoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuartoServiceTest {

    @Mock
    private QuartoRepository quartoRepository;

    @Mock
    private ResidenciaRepository residenciaRepository;

    @InjectMocks
    private QuartoService quartoService;

    private QuartoIndividual quartoIndividual;
    private QuartoDuplo quartoDuplo;
    private Residencia residencia;

    @BeforeEach
    void setUp() {
        residencia = new Residencia();
        residencia.setId(1L);
        residencia.setNome("Pousada");
        residencia.setEndereco("Rua X");

        quartoIndividual = new QuartoIndividual();
        quartoIndividual.setId(1L);
        quartoIndividual.setValorBase(100.0);
        quartoIndividual.setNumeroCamas(1);

        quartoDuplo = new QuartoDuplo();
        quartoDuplo.setId(2L);
        quartoDuplo.setValorBase(200.0);
    }

    @Test
    void listar_retornaTodosOsQuartos() {
        when(quartoRepository.findAll()).thenReturn(List.of(quartoIndividual, quartoDuplo));

        List<Quarto> resultado = quartoService.listar();

        assertEquals(2, resultado.size());
    }

    @Test
    void buscarPorId_existente_retornaQuarto() {
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quartoIndividual));

        Quarto resultado = quartoService.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
    }

    @Test
    void buscarPorId_inexistente_lancaNotFoundException() {
        when(quartoRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quartoService.buscarPorId(99L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void listarPorTipo_tipoIndividual_retornaListaFiltrada() {
        when(quartoRepository.findByTipo(QuartoIndividual.class)).thenReturn(List.of(quartoIndividual));

        List<Quarto> resultado = quartoService.listarPorTipo("INDIVIDUAL");

        assertEquals(1, resultado.size());
        assertInstanceOf(QuartoIndividual.class, resultado.get(0));
    }

    @Test
    void listarPorTipo_tipoDuplo_retornaListaFiltrada() {
        when(quartoRepository.findByTipo(QuartoDuplo.class)).thenReturn(List.of(quartoDuplo));

        List<Quarto> resultado = quartoService.listarPorTipo("DUPLO");

        assertEquals(1, resultado.size());
        assertInstanceOf(QuartoDuplo.class, resultado.get(0));
    }

    @Test
    void listarPorTipo_tipoInvalido_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> quartoService.listarPorTipo("INVALIDO"));

        verify(quartoRepository, never()).findByTipo(any());
    }

    @Test
    void criar_semResidencia_salvaDiretamente() {
        quartoIndividual.setResidencia(null);
        when(quartoRepository.save(any(Quarto.class))).thenReturn(quartoIndividual);

        Quarto resultado = quartoService.criar(quartoIndividual);

        assertNotNull(resultado);
        verify(residenciaRepository, never()).findById(any());
    }

    @Test
    void criar_comResidenciaValida_associaResidencia() {
        quartoIndividual.setResidencia(residencia);
        when(residenciaRepository.findById(1L)).thenReturn(Optional.of(residencia));
        when(quartoRepository.save(any(Quarto.class))).thenAnswer(inv -> inv.getArgument(0));

        Quarto resultado = quartoService.criar(quartoIndividual);

        assertNotNull(resultado.getResidencia());
        assertEquals("Pousada", resultado.getResidencia().getNome());
    }

    @Test
    void criar_comResidenciaInexistente_lancaNotFoundException() {
        quartoIndividual.setResidencia(residencia);
        when(residenciaRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quartoService.criar(quartoIndividual));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void deletar_quartoExistente_chamaDeleteById() {
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quartoIndividual));

        quartoService.deletar(1L);

        verify(quartoRepository).deleteById(1L);
    }

    @Test
    void deletar_quartoInexistente_lancaNotFoundException() {
        when(quartoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> quartoService.deletar(99L));
        verify(quartoRepository, never()).deleteById(any());
    }
}
