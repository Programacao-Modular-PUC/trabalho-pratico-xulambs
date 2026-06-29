package com.hospedagem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospedagem.controller.ClienteController;
import com.hospedagem.model.Cliente;
import com.hospedagem.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService clienteService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João");
        cliente.setCpf("123.456.789-00");
        cliente.setEmail("joao@email.com");
    }

    @Test
    void listar_retornaStatus200ComLista() throws Exception {
        when(clienteService.listar()).thenReturn(List.of(cliente));

        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("João"));
    }

    @Test
    void buscarPorId_existente_retornaStatus200() throws Exception {
        when(clienteService.buscarPorId(1L)).thenReturn(cliente);

        mockMvc.perform(get("/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("123.456.789-00"));
    }

    @Test
    void buscarPorId_inexistente_retornaStatus404() throws Exception {
        when(clienteService.buscarPorId(99L))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Cliente não encontrado"));

        mockMvc.perform(get("/clientes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void criar_dadosValidos_retornaStatus201() throws Exception {
        when(clienteService.criar(any(Cliente.class))).thenReturn(cliente);

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void criar_cpfDuplicado_retornaStatus409() throws Exception {
        when(clienteService.criar(any(Cliente.class)))
                .thenThrow(new ResponseStatusException(CONFLICT, "CPF já cadastrado"));

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isConflict());
    }

    @Test
    void criar_nomeEmBranco_retornaStatus400() throws Exception {
        cliente.setNome("");

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizar_dadosValidos_retornaStatus200() throws Exception {
        when(clienteService.atualizar(eq(1L), any(Cliente.class))).thenReturn(cliente);

        mockMvc.perform(put("/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João"));
    }

    @Test
    void deletar_existente_retornaStatus204() throws Exception {
        mockMvc.perform(delete("/clientes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletar_inexistente_retornaStatus404() throws Exception {
        doThrow(new ResponseStatusException(NOT_FOUND, "Cliente não encontrado"))
                .when(clienteService).deletar(99L);

        mockMvc.perform(delete("/clientes/99"))
                .andExpect(status().isNotFound());
    }
}
