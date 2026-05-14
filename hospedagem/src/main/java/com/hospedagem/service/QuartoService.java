package com.hospedagem.service;

import com.hospedagem.model.Quarto;
import com.hospedagem.model.Residencia;
import com.hospedagem.repository.QuartoRepository;
import com.hospedagem.repository.ResidenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuartoService {

    private final QuartoRepository quartoRepository;
    private final ResidenciaRepository residenciaRepository;

    public List<Quarto> listar() {
        return quartoRepository.findAll();
    }

    public List<Quarto> listarPorResidencia(Long residenciaId) {
        return quartoRepository.findByResidenciaId(residenciaId);
    }

    public Quarto buscarPorId(Long id) {
        return quartoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quarto não encontrado"));
    }

    public Quarto criar(Quarto quarto) {
        if (quarto.getResidencia() != null && quarto.getResidencia().getId() != null) {
            Residencia residencia = residenciaRepository.findById(quarto.getResidencia().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Residência não encontrada"));
            quarto.setResidencia(residencia);
        }
        return quartoRepository.save(quarto);
    }

    public void deletar(Long id) {
        buscarPorId(id);
        quartoRepository.deleteById(id);
    }
}
