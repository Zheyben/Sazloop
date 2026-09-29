package com.sazloop.afiliacion.repository;

import com.sazloop.afiliacion.domain.DocumentoEstablecimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Archivos presentados en una solicitud. */
public interface DocumentoEstablecimientoRepository extends JpaRepository<DocumentoEstablecimiento, Long> {
    List<DocumentoEstablecimiento> findBySolicitudAfiliacion_IdOrderByIdAsc(Long solicitudId);
}
