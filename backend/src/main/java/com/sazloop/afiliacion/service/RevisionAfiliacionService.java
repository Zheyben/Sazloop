package com.sazloop.afiliacion.service;

import com.sazloop.afiliacion.domain.*;
import com.sazloop.afiliacion.domain.EvaluacionAfiliacion.DecisionAfiliacion;
import com.sazloop.afiliacion.domain.RevisionDocumento.ResultadoRevision;
import com.sazloop.afiliacion.domain.SolicitudAfiliacion.EstadoSolicitud;
import com.sazloop.afiliacion.dto.*;
import com.sazloop.afiliacion.repository.*;
import com.sazloop.identidad.domain.Establecimiento;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;

/** Revisión humana de cada archivo y resolución de la solicitud (RF-07 y RF-08). */
@Service
public class RevisionAfiliacionService {
    private final SolicitudAfiliacionRepository solicitudes;
    private final DocumentoEstablecimientoRepository documentos;
    private final RevisionDocumentoRepository revisiones;
    private final EvaluacionAfiliacionRepository evaluaciones;
    private final AlmacenDocumentoService almacen;

    public RevisionAfiliacionService(SolicitudAfiliacionRepository solicitudes,
            DocumentoEstablecimientoRepository documentos, RevisionDocumentoRepository revisiones,
            EvaluacionAfiliacionRepository evaluaciones, AlmacenDocumentoService almacen) {
        this.solicitudes = solicitudes;
        this.documentos = documentos;
        this.revisiones = revisiones;
        this.evaluaciones = evaluaciones;
        this.almacen = almacen;
    }

    @Transactional(readOnly = true)
    public List<SolicitudRevisionResponse> pendientes() {
        return solicitudes.findByEstadoOrderByIdAsc(EstadoSolicitud.EN_REVISION)
                .stream().map(this::respuesta).toList();
    }

    @Transactional(readOnly = true)
    public SolicitudRevisionResponse consultar(Long id) { return respuesta(solicitud(id)); }

    @Transactional(readOnly = true)
    public SolicitudAfiliacionService.ArchivoPrivado descargar(Long documentoId) {
        DocumentoEstablecimiento doc = documento(documentoId);
        String nombre = doc.getUrlArchivo();
        return new SolicitudAfiliacionService.ArchivoPrivado(
                almacen.leer(nombre), nombre.substring(nombre.lastIndexOf('.') + 1));
    }

    @Transactional
    public SolicitudRevisionResponse revisar(AdministradorPlataforma admin,
            Long documentoId, RevisionRequest entrada) {
        DocumentoEstablecimiento doc = documento(documentoId);
        SolicitudAfiliacion solicitud = doc.getSolicitudAfiliacion();
        exigirEnRevision(solicitud);
        if (revisiones.findFirstByDocumento_IdOrderByIdDesc(documentoId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El documento ya fue revisado");
        }
        if (entrada.resultado() == ResultadoRevision.RECHAZADO &&
                (entrada.observaciones() == null || entrada.observaciones().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica las observaciones del rechazo");
        }
        if (entrada.tipo() != null) doc.setTipoDocumento(entrada.tipo());
        if (entrada.numero() != null) doc.setNumeroDocumento(entrada.numero().trim());
        if (entrada.emision() != null) doc.setFechaEmision(entrada.emision());
        if (entrada.vencimiento() != null) doc.setFechaVencimiento(entrada.vencimiento());
        if (doc.getFechaEmision() != null && doc.getFechaVencimiento() != null &&
                doc.getFechaVencimiento().isBefore(doc.getFechaEmision()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vencimiento anterior a emisión");
        revisiones.save(RevisionDocumento.builder().documento(doc).administrador(admin)
                .resultado(entrada.resultado()).observaciones(entrada.observaciones()).build());
        return respuesta(solicitud);
    }

    @Transactional
    public SolicitudRevisionResponse decidir(AdministradorPlataforma admin, Long id, DecisionRequest entrada) {
        SolicitudAfiliacion solicitud = solicitud(id);
        exigirEnRevision(solicitud);
        List<DocumentoEstablecimiento> lista = documentos.findBySolicitudAfiliacion_IdOrderByIdAsc(id);
        if (lista.isEmpty() || lista.stream().anyMatch(d ->
                revisiones.findFirstByDocumento_IdOrderByIdDesc(d.getId()).isEmpty())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Revisa todos los documentos antes de decidir");
        }
        if (entrada.decision() == DecisionAfiliacion.APROBADA) {
            boolean noAprobable = lista.stream().anyMatch(d ->
                    revisiones.findFirstByDocumento_IdOrderByIdDesc(d.getId()).orElseThrow().getResultado()
                            != ResultadoRevision.APROBADO ||
                    d.getFechaVencimiento() != null && d.getFechaVencimiento().isBefore(LocalDate.now()));
            if (noAprobable) throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede aprobar con documentos rechazados o vencidos");
        }
        if (evaluaciones.existsBySolicitudAfiliacion_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La solicitud ya fue resuelta");
        }
        EvaluacionAfiliacion evaluacion = evaluaciones.save(EvaluacionAfiliacion.builder()
                .solicitudAfiliacion(solicitud).administrador(admin)
                .decision(entrada.decision()).motivo(entrada.motivo().trim()).build());
        solicitud.setEvaluacion(evaluacion);
        solicitud.setEstado(entrada.decision() == DecisionAfiliacion.APROBADA
                ? EstadoSolicitud.APROBADA : EstadoSolicitud.RECHAZADA);
        if (entrada.decision() == DecisionAfiliacion.APROBADA) {
            solicitud.getEstablecimiento().setEstado(Establecimiento.EstadoEstablecimiento.ACTIVO);
        }
        return respuesta(solicitud);
    }

    private SolicitudRevisionResponse respuesta(SolicitudAfiliacion s) {
        List<SolicitudRevisionResponse.DocumentoEnRevision> lista =
                documentos.findBySolicitudAfiliacion_IdOrderByIdAsc(s.getId()).stream().map(d -> {
                    RevisionDocumento r = revisiones.findFirstByDocumento_IdOrderByIdDesc(d.getId()).orElse(null);
                    return new SolicitudRevisionResponse.DocumentoEnRevision(d.getId(),
                            d.getTipoDocumento().name(), d.getNumeroDocumento(), d.getFechaEmision(),
                            d.getFechaVencimiento(), r == null ? null : r.getResultado(),
                            r == null ? null : r.getObservaciones());
                }).toList();
        return new SolicitudRevisionResponse(s.getId(), s.getEstablecimiento().getId(),
                s.getEstablecimiento().getNombre(), s.getEstado(), lista);
    }

    private SolicitudAfiliacion solicitud(Long id) {
        return solicitudes.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    private DocumentoEstablecimiento documento(Long id) {
        return documentos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    private static void exigirEnRevision(SolicitudAfiliacion solicitud) {
        if (solicitud.getEstado() != EstadoSolicitud.EN_REVISION)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La solicitud no está en revisión");
    }
}
