package com.hospedagem.service;

import com.hospedagem.model.Residencia;
import com.hospedagem.repository.ResidenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResidenciaService {

    private final ResidenciaRepository repository;

    public List<Residencia> listar() {
        return repository.findAll();
    }

    public Residencia buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Residência não encontrada"));
    }

    public Residencia criar(Residencia residencia) {
        return repository.save(residencia);
    }

    public Residencia atualizar(Long id, Residencia dados) {
        Residencia existente = buscarPorId(id);
        existente.setNome(dados.getNome());
        existente.setEndereco(dados.getEndereco());
        return repository.save(existente);
    }

    public void deletar(Long id) {
        buscarPorId(id);
        repository.deleteById(id);
    }
}
