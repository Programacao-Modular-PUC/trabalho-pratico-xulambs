package com.hospedagem.repository;

import com.hospedagem.model.Quarto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuartoRepository extends JpaRepository<Quarto, Long> {

    List<Quarto> findByResidenciaId(Long residenciaId);

    @Query("SELECT q FROM Quarto q WHERE TYPE(q) = :tipo")
    List<Quarto> findByTipo(@Param("tipo") Class<? extends Quarto> tipo);
}
