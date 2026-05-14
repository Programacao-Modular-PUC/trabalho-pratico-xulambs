package com.hospedagem.controller;

import com.hospedagem.model.Quarto;
import com.hospedagem.model.Residencia;
import com.hospedagem.service.QuartoService;
import com.hospedagem.service.ResidenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/residencias")
@RequiredArgsConstructor
public class ResidenciaController {

    private final ResidenciaService residenciaService;
    private final QuartoService quartoService;

    @GetMapping
    public List<Residencia> listar() {
        return residenciaService.listar();
    }

    @GetMapping("/{id}")
    public Residencia buscarPorId(@PathVariable Long id) {
        return residenciaService.buscarPorId(id);
    }

    @GetMapping("/{id}/quartos")
    public List<Quarto> listarQuartos(@PathVariable Long id) {
        return quartoService.listarPorResidencia(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Residencia criar(@RequestBody @Valid Residencia residencia) {
        return residenciaService.criar(residencia);
    }

    @PutMapping("/{id}")
    public Residencia atualizar(@PathVariable Long id, @RequestBody @Valid Residencia dados) {
        return residenciaService.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        residenciaService.deletar(id);
    }
}
