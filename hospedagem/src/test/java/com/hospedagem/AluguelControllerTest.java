package com.hospedagem;

import com.hospedagem.controller.AluguelController;
import com.hospedagem.exception.QuartoIndisponivelException;
import com.hospedagem.model.*;
import com.hospedagem.service.AluguelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AluguelController.class)
class AluguelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AluguelService aluguelService;

    private Aluguel aluguel;

    @BeforeEach
    void setUp() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Ana");
        cliente.setCpf("111.111.111-11");

        QuartoDuplo quarto = new QuartoDuplo();
        quarto.setId(1L);
        quarto.setValorBase(200.0);
        quarto.setTipoCama(TipoCama.CASAL);

        aluguel = new Aluguel();
        aluguel.setId(1L);
        aluguel.setCliente(cliente);
        aluguel.setQuarto(quarto);
        aluguel.setDataInicio(LocalDate.of(2026, 7, 1));
        aluguel.setDataFim(LocalDate.of(2026, 7, 5));
        aluguel.setNumHospedes(2);
        aluguel.setSolicitouBerco(false);
        aluguel.setValorTotal(800.0);
        aluguel.setStatus(StatusAluguel.ATIVO);
    }

    @Test
    void listar_retornaStatus200ComLista() throws Exception {
        when(aluguelService.listar()).thenReturn(List.of(aluguel));

        mockMvc.perform(get("/alugueis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void buscarPorId_existente_retornaStatus200() throws Exception {
        when(aluguelService.buscarPorId(1L)).thenReturn(aluguel);

        mockMvc.perform(get("/alugueis/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(800.0));
    }

    @Test
    void buscarPorId_inexistente_retornaStatus404() throws Exception {
        when(aluguelService.buscarPorId(99L))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Aluguel não encontrado"));

        mockMvc.perform(get("/alugueis/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarPorCliente_retornaStatus200() throws Exception {
        when(aluguelService.listarPorCliente(1L)).thenReturn(List.of(aluguel));

        mockMvc.perform(get("/alugueis/cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ATIVO"));
    }

    @Test
    void listarHistorico_retornaStatus200() throws Exception {
        when(aluguelService.listarHistoricoPorCliente(1L)).thenReturn(List.of(aluguel));

        mockMvc.perform(get("/alugueis/historico/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void criar_dadosValidos_retornaStatus201() throws Exception {
        when(aluguelService.criar(any(Aluguel.class))).thenReturn(aluguel);

        String json = """
                {
                  "cliente": {"id": 1},
                  "quarto": {"tipo": "DUPLO", "id": 1},
                  "dataInicio": "2026-07-01",
                  "dataFim": "2026-07-05",
                  "numHospedes": 2,
                  "solicitouBerco": false
                }
                """;

        mockMvc.perform(post("/alugueis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorTotal").value(800.0));
    }

    @Test
    void criar_quartoIndisponivel_retornaStatus409() throws Exception {
        when(aluguelService.criar(any(Aluguel.class)))
                .thenThrow(new QuartoIndisponivelException("Quarto indisponível"));

        String json = """
                {
                  "cliente": {"id": 1},
                  "quarto": {"tipo": "DUPLO", "id": 1},
                  "dataInicio": "2026-07-01",
                  "dataFim": "2026-07-05",
                  "numHospedes": 2,
                  "solicitouBerco": false
                }
                """;

        mockMvc.perform(post("/alugueis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    void cancelar_aluguelAtivo_retornaStatus200() throws Exception {
        aluguel.setStatus(StatusAluguel.CANCELADO);
        when(aluguelService.cancelar(1L)).thenReturn(aluguel);

        mockMvc.perform(patch("/alugueis/1/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    void deletar_existente_retornaStatus204() throws Exception {
        mockMvc.perform(delete("/alugueis/1"))
                .andExpect(status().isNoContent());
    }
}
