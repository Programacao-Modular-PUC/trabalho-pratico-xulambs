package com.hospedagem;

import com.hospedagem.model.Residencia;
import com.hospedagem.repository.ResidenciaRepository;
import com.hospedagem.service.ResidenciaService;
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
class ResidenciaServiceTest {

    @Mock
    private ResidenciaRepository repository;

    @InjectMocks
    private ResidenciaService residenciaService;

    private Residencia residencia;

    @BeforeEach
    void setUp() {
        residencia = new Residencia();
        residencia.setId(1L);
        residencia.setNome("Pousada Central");
        residencia.setEndereco("Rua A, 123");
    }

    @Test
    void listar_retornaTodasAsResidencias() {
        when(repository.findAll()).thenReturn(List.of(residencia));

        List<Residencia> resultado = residenciaService.listar();

        assertEquals(1, resultado.size());
        assertEquals("Pousada Central", resultado.get(0).getNome());
    }

    @Test
    void criar_retornaResidenciaSalva() {
        when(repository.save(any(Residencia.class))).thenReturn(residencia);

        Residencia resultado = residenciaService.criar(residencia);

        assertNotNull(resultado);
        assertEquals("Pousada Central", resultado.getNome());
        verify(repository).save(residencia);
    }

    @Test
    void buscarPorId_existente_retornaResidencia() {
        when(repository.findById(1L)).thenReturn(Optional.of(residencia));

        Residencia resultado = residenciaService.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("Pousada Central", resultado.getNome());
    }

    @Test
    void buscarPorId_inexistente_lancaNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> residenciaService.buscarPorId(99L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void atualizar_residenciaExistente_retornaResidenciaAtualizada() {
        Residencia dados = new Residencia();
        dados.setNome("Pousada Nova");
        dados.setEndereco("Avenida B, 456");

        when(repository.findById(1L)).thenReturn(Optional.of(residencia));
        when(repository.save(any(Residencia.class))).thenAnswer(inv -> inv.getArgument(0));

        Residencia resultado = residenciaService.atualizar(1L, dados);

        assertEquals("Pousada Nova", resultado.getNome());
        assertEquals("Avenida B, 456", resultado.getEndereco());
    }

    @Test
    void atualizar_residenciaInexistente_lancaNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> residenciaService.atualizar(99L, new Residencia()));
    }

    @Test
    void deletar_residenciaExistente_chamaDeleteById() {
        when(repository.findById(1L)).thenReturn(Optional.of(residencia));

        residenciaService.deletar(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deletar_residenciaInexistente_lancaNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> residenciaService.deletar(99L));
        verify(repository, never()).deleteById(any());
    }
}
