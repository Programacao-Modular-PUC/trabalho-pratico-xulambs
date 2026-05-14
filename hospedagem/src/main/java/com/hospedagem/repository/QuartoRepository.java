package com.hospedagem.repository;

import com.hospedagem.model.Quarto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuartoRepository extends JpaRepository<Quarto, Long> {
    List<Quarto> findByResidenciaId(Long residenciaId);
}
