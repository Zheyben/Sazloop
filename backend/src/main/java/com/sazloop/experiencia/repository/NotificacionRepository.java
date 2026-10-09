package com.sazloop.experiencia.repository;

import com.sazloop.experiencia.domain.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByDestinatario_IdOrderByIdDesc(Long usuarioId);
    boolean existsByDestinatario_IdAndTipoAndTipoReferenciaAndReferenciaId(
            Long usuarioId, Notificacion.TipoNotificacion tipo, String referencia, Long referenciaId);
}
