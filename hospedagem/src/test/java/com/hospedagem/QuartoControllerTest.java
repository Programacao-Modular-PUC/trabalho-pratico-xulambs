package com.hospedagem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospedagem.controller.QuartoController;
import com.hospedagem.model.QuartoDuplo;
import com.hospedagem.model.QuartoIndividual;
import com.hospedagem.model.TipoCama;
import com.hospedagem.service.QuartoService;
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
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuartoController.class)
class QuartoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuartoService quartoService;

    private QuartoIndividual quartoIndividual;
    private QuartoDuplo quartoDuplo;

    @BeforeEach
    void setUp() {
        quartoIndividual = new QuartoIndividual();
        quartoIndividual.setId(1L);
        quartoIndividual.setValorBase(100.0);
        quartoIndividual.setNumeroCamas(1);

        quartoDuplo = new QuartoDuplo();
        quartoDuplo.setId(2L);
        quartoDuplo.setValorBase(200.0);
        quartoDuplo.setTipoCama(TipoCama.CASAL);
    }

    @Test
    void listar_semFiltro_retornaStatus200ComTodos() throws Exception {
        when(quartoService.listar()).thenReturn(List.of(quartoIndividual, quartoDuplo));

        mockMvc.perform(get("/quartos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void listar_comTipoValido_retornaStatus200Filtrado() throws Exception {
        when(quartoService.listarPorTipo("INDIVIDUAL")).thenReturn(List.of(quartoIndividual));

        mockMvc.perform(get("/quartos").param("tipo", "INDIVIDUAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("INDIVIDUAL"));
    }

    @Test
    void listar_comTipoInvalido_retornaStatus400() throws Exception {
        when(quartoService.listarPorTipo("INVALIDO"))
                .thenThrow(new IllegalArgumentException("Tipo de quarto inválido: INVALIDO"));

        mockMvc.perform(get("/quartos").param("tipo", "INVALIDO"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscarPorId_existente_retornaStatus200() throws Exception {
        when(quartoService.buscarPorId(1L)).thenReturn(quartoIndividual);

        mockMvc.perform(get("/quartos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void buscarPorId_inexistente_retornaStatus404() throws Exception {
        when(quartoService.buscarPorId(99L))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Quarto não encontrado"));

        mockMvc.perform(get("/quartos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void criar_quartoIndividual_retornaStatus201() throws Exception {
        when(quartoService.criar(any())).thenReturn(quartoIndividual);

        mockMvc.perform(post("/quartos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(quartoIndividual)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deletar_existente_retornaStatus204() throws Exception {
        mockMvc.perform(delete("/quartos/1"))
                .andExpect(status().isNoContent());
    }
}
