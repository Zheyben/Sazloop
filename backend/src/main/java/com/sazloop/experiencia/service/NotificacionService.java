package com.sazloop.experiencia.service;

import com.sazloop.experiencia.domain.Notificacion;
import com.sazloop.experiencia.repository.NotificacionRepository;
import com.sazloop.identidad.domain.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacionService {
    private final NotificacionRepository avisos;
    public NotificacionService(NotificacionRepository avisos) { this.avisos = avisos; }

    @Transactional
    public void crear(Usuario destinatario, Notificacion.TipoNotificacion tipo,
            String titulo, String mensaje, String referencia, Long referenciaId) {
        avisos.save(Notificacion.builder().destinatario(destinatario).tipo(tipo)
                .titulo(recortar(titulo, 120)).mensaje(recortar(mensaje, 1000)).tipoReferencia(referencia)
                .referenciaId(referenciaId).build());
    }

    /** Los nombres de locales son libres; el aviso nunca debe fallar por su longitud. */
    private static String recortar(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 1) + "…";
    }

    @Transactional
    public void crearUnaVez(Usuario destinatario, Notificacion.TipoNotificacion tipo,
            String titulo, String mensaje, String referencia, Long referenciaId) {
        if (!avisos.existsByDestinatario_IdAndTipoAndTipoReferenciaAndReferenciaId(
                destinatario.getId(), tipo, referencia, referenciaId))
            crear(destinatario, tipo, titulo, mensaje, referencia, referenciaId);
    }

    @Transactional(readOnly = true)
    public List<Aviso> listar(Long usuarioId) {
        return avisos.findByDestinatario_IdOrderByIdDesc(usuarioId).stream()
                .map(a -> new Aviso(a.getId(), a.getTipo(), a.getTitulo(), a.getMensaje(),
                        a.getFechaCreacion(), a.isLeida())).toList();
    }

    @Transactional
    public void leerTodas(Long usuarioId) {
        LocalDateTime ahora = LocalDateTime.now();
        avisos.findByDestinatario_IdOrderByIdDesc(usuarioId).stream().filter(a -> !a.isLeida()).forEach(a -> {
            a.setLeida(true);
            a.setFechaLectura(ahora);
        });
    }

    @Transactional
    public void leer(Long usuarioId, Long id) {
        Notificacion aviso = avisos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!aviso.getDestinatario().getId().equals(usuarioId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (!aviso.isLeida()) {
            aviso.setLeida(true);
            aviso.setFechaLectura(LocalDateTime.now());
        }
    }

    public record Aviso(Long id, Notificacion.TipoNotificacion tipo,
            String titulo, String mensaje, LocalDateTime fecha, boolean leida) {}
}
