package com.hospedagem.controller;

import com.hospedagem.model.Quarto;
import com.hospedagem.service.QuartoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quartos")
@RequiredArgsConstructor
public class QuartoController {

    private final QuartoService quartoService;

    @GetMapping
    public List<Quarto> listar() {
        return quartoService.listar();
    }

    @GetMapping("/{id}")
    public Quarto buscarPorId(@PathVariable Long id) {
        return quartoService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Quarto criar(@RequestBody Quarto quarto) {
        return quartoService.criar(quarto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        quartoService.deletar(id);
    }
}
