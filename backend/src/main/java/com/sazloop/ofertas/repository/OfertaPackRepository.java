package com.sazloop.ofertas.repository;

import com.sazloop.ofertas.domain.OfertaPack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface OfertaPackRepository extends JpaRepository<OfertaPack, Long> {
    List<OfertaPack> findByPackSorpresa_Establecimiento_IdAndEstado(
            Long localId, OfertaPack.EstadoOferta estado);
    List<OfertaPack> findByEstado(OfertaPack.EstadoOferta estado);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OfertaPack o where o.id = :id")
    Optional<OfertaPack> bloquear(@Param("id") Long id);
}
