package com.sazloop.reservas.repository;

import com.sazloop.reservas.domain.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByConsumidor_Usuario_IdOrderByIdDesc(Long usuarioId);
}
