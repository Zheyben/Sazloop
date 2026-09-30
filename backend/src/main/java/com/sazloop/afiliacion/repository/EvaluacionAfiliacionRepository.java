package com.sazloop.afiliacion.repository;

import com.sazloop.afiliacion.domain.EvaluacionAfiliacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluacionAfiliacionRepository extends JpaRepository<EvaluacionAfiliacion, Long> {
    boolean existsBySolicitudAfiliacion_Id(Long solicitudId);
}
