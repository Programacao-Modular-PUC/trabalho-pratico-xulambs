package com.hospedagem;

import com.hospedagem.model.Cliente;
import com.hospedagem.repository.ClienteRepository;
import com.hospedagem.service.ClienteService;
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
class ClienteServiceTest {

    @Mock
    private ClienteRepository repository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Maria");
        cliente.setCpf("111.222.333-44");
        cliente.setEmail("maria@email.com");
        cliente.setTelefone("11999999999");
    }

    @Test
    void listar_retornaListaDeClientes() {
        when(repository.findAll()).thenReturn(List.of(cliente));

        List<Cliente> resultado = clienteService.listar();

        assertEquals(1, resultado.size());
        assertEquals("Maria", resultado.get(0).getNome());
    }

    @Test
    void criar_cpfNovo_retornaClienteSalvo() {
        when(repository.findByCpf(cliente.getCpf())).thenReturn(Optional.empty());
        when(repository.save(any(Cliente.class))).thenReturn(cliente);

        Cliente resultado = clienteService.criar(cliente);

        assertNotNull(resultado);
        assertEquals("Maria", resultado.getNome());
        verify(repository).save(cliente);
    }

    @Test
    void criar_cpfDuplicado_lancaConflictException() {
        when(repository.findByCpf(cliente.getCpf())).thenReturn(Optional.of(cliente));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> clienteService.criar(cliente));

        assertEquals(409, ex.getStatusCode().value());
        verify(repository, never()).save(any());
    }

    @Test
    void buscarPorId_existente_retornaCliente() {
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));

        Cliente resultado = clienteService.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("Maria", resultado.getNome());
    }

    @Test
    void buscarPorId_inexistente_lancaNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> clienteService.buscarPorId(99L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void atualizar_clienteExistente_retornaClienteAtualizado() {
        Cliente dados = new Cliente();
        dados.setNome("Maria Silva");
        dados.setEmail("nova@email.com");
        dados.setTelefone("11888888888");

        when(repository.findById(1L)).thenReturn(Optional.of(cliente));
        when(repository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        Cliente resultado = clienteService.atualizar(1L, dados);

        assertEquals("Maria Silva", resultado.getNome());
        assertEquals("nova@email.com", resultado.getEmail());
        assertEquals("11888888888", resultado.getTelefone());
    }

    @Test
    void atualizar_clienteInexistente_lancaNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> clienteService.atualizar(99L, new Cliente()));
    }

    @Test
    void deletar_clienteExistente_chamaDeleteById() {
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));

        clienteService.deletar(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deletar_clienteInexistente_lancaNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> clienteService.deletar(99L));
        verify(repository, never()).deleteById(any());
    }
}
