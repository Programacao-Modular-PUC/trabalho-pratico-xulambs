package com.hospedagem.controller;

import com.hospedagem.model.Aluguel;
import com.hospedagem.service.AluguelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alugueis")
@RequiredArgsConstructor
public class AluguelController {

    private final AluguelService aluguelService;

    @GetMapping
    public List<Aluguel> listar() {
        return aluguelService.listar();
    }

    @GetMapping("/{id}")
    public Aluguel buscarPorId(@PathVariable Long id) {
        return aluguelService.buscarPorId(id);
    }

    @GetMapping("/cliente/{clienteId}")
    public List<Aluguel> listarPorCliente(@PathVariable Long clienteId) {
        return aluguelService.listarPorCliente(clienteId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Aluguel criar(@RequestBody @Valid Aluguel aluguel) {
        return aluguelService.criar(aluguel);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        aluguelService.deletar(id);
    }
}
