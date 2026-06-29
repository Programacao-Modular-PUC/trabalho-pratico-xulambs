package com.hospedagem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospedagem.controller.ResidenciaController;
import com.hospedagem.model.QuartoIndividual;
import com.hospedagem.model.Residencia;
import com.hospedagem.service.QuartoService;
import com.hospedagem.service.ResidenciaService;
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
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResidenciaController.class)
class ResidenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResidenciaService residenciaService;

    @MockBean
    private QuartoService quartoService;

    private Residencia residencia;

    @BeforeEach
    void setUp() {
        residencia = new Residencia();
        residencia.setId(1L);
        residencia.setNome("Pousada Central");
        residencia.setEndereco("Rua A, 123");
    }

    @Test
    void listar_retornaStatus200ComLista() throws Exception {
        when(residenciaService.listar()).thenReturn(List.of(residencia));

        mockMvc.perform(get("/residencias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Pousada Central"));
    }

    @Test
    void buscarPorId_existente_retornaStatus200() throws Exception {
        when(residenciaService.buscarPorId(1L)).thenReturn(residencia);

        mockMvc.perform(get("/residencias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endereco").value("Rua A, 123"));
    }

    @Test
    void buscarPorId_inexistente_retornaStatus404() throws Exception {
        when(residenciaService.buscarPorId(99L))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Residência não encontrada"));

        mockMvc.perform(get("/residencias/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarQuartos_retornaStatus200ComLista() throws Exception {
        QuartoIndividual quarto = new QuartoIndividual();
        quarto.setId(1L);
        quarto.setValorBase(100.0);
        quarto.setNumeroCamas(1);
        when(quartoService.listarPorResidencia(1L)).thenReturn(List.of(quarto));

        mockMvc.perform(get("/residencias/1/quartos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void criar_dadosValidos_retornaStatus201() throws Exception {
        when(residenciaService.criar(any(Residencia.class))).thenReturn(residencia);

        mockMvc.perform(post("/residencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(residencia)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void criar_nomeEmBranco_retornaStatus400() throws Exception {
        residencia.setNome("");

        mockMvc.perform(post("/residencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(residencia)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizar_dadosValidos_retornaStatus200() throws Exception {
        when(residenciaService.atualizar(eq(1L), any(Residencia.class))).thenReturn(residencia);

        mockMvc.perform(put("/residencias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(residencia)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Pousada Central"));
    }

    @Test
    void deletar_existente_retornaStatus204() throws Exception {
        mockMvc.perform(delete("/residencias/1"))
                .andExpect(status().isNoContent());
    }
}
