package com.sazloop.reservas.repository;

import com.sazloop.reservas.domain.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByConsumidor_Usuario_IdOrderByIdDesc(Long usuarioId);
    List<Reserva> findByOferta_PackSorpresa_Establecimiento_Id(Long localId);
    List<Reserva> findByEstadoAndOferta_FinRecojoLessThanEqual(
            Reserva.EstadoReserva estado, LocalDateTime fin);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reserva r where r.id = :id")
    Optional<Reserva> bloquear(Long id);
}
