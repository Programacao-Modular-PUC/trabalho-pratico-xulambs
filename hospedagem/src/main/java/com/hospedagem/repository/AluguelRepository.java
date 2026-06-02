package com.hospedagem.repository;

import com.hospedagem.model.Aluguel;
import com.hospedagem.model.StatusAluguel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AluguelRepository extends JpaRepository<Aluguel, Long> {

    List<Aluguel> findByClienteId(Long clienteId);

    List<Aluguel> findByClienteIdAndStatus(Long clienteId, StatusAluguel status);

    List<Aluguel> findByQuartoId(Long quartoId);

    @Query("SELECT a FROM Aluguel a WHERE a.quarto.id = :quartoId " +
           "AND a.status = :status " +
           "AND a.dataInicio < :dataFim " +
           "AND a.dataFim > :dataInicio")
    List<Aluguel> findConflitosReserva(@Param("quartoId") Long quartoId,
                                       @Param("dataInicio") LocalDate dataInicio,
                                       @Param("dataFim") LocalDate dataFim,
                                       @Param("status") StatusAluguel status);
}
