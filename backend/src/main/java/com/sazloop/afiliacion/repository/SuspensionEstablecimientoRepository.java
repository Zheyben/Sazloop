package com.sazloop.afiliacion.repository;

import com.sazloop.afiliacion.domain.SuspensionEstablecimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Suspensiones de cada local; la activa se cierra al aprobar su renovación. */
public interface SuspensionEstablecimientoRepository extends JpaRepository<SuspensionEstablecimiento, Long> {
    List<SuspensionEstablecimiento> findByEstablecimiento_IdAndActivaTrue(Long establecimientoId);
    Optional<SuspensionEstablecimiento> findFirstByEstablecimiento_IdAndActivaTrueOrderByIdDesc(Long establecimientoId);
}
