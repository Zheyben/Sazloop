package com.sazloop.afiliacion.repository;

import com.sazloop.afiliacion.domain.SolicitudAfiliacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Solicitudes de afiliación por establecimiento. */
public interface SolicitudAfiliacionRepository extends JpaRepository<SolicitudAfiliacion, Long> {
    Optional<SolicitudAfiliacion> findFirstByEstablecimiento_IdOrderByIdDesc(Long establecimientoId);
}
