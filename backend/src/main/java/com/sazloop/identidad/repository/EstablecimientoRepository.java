package com.sazloop.identidad.repository;

import com.sazloop.identidad.domain.Establecimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

/** Establecimientos asociados a una empresa. */
public interface EstablecimientoRepository extends JpaRepository<Establecimiento, Long> {
    List<Establecimiento> findByEmpresa_IdOrderByIdAsc(Long empresaId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Establecimiento e where e.id = :id")
    Optional<Establecimiento> bloquear(@Param("id") Long id);
}
