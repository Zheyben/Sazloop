package com.sazloop.afiliacion.repository;

import com.sazloop.afiliacion.domain.SolicitudAfiliacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

/** Solicitudes de afiliación por establecimiento. */
public interface SolicitudAfiliacionRepository extends JpaRepository<SolicitudAfiliacion, Long> {
    Optional<SolicitudAfiliacion> findFirstByEstablecimiento_IdOrderByIdDesc(Long establecimientoId);
    List<SolicitudAfiliacion> findByEstadoOrderByIdAsc(SolicitudAfiliacion.EstadoSolicitud estado);
    /** Una solicitud posterior a otra ya aprobada del mismo local es una renovación. */
    boolean existsByEstablecimiento_IdAndEstadoAndIdLessThan(
            Long establecimientoId, SolicitudAfiliacion.EstadoSolicitud estado, Long id);
    boolean existsByEstablecimiento_IdAndEstado(Long establecimientoId, SolicitudAfiliacion.EstadoSolicitud estado);
}
