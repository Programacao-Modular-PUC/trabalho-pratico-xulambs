package com.hospedagem.repository;

import com.hospedagem.model.Aluguel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AluguelRepository extends JpaRepository<Aluguel, Long> {
    List<Aluguel> findByClienteId(Long clienteId);
    List<Aluguel> findByQuartoId(Long quartoId);
}
