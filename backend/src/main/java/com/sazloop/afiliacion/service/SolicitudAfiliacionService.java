package com.sazloop.afiliacion.service;

import com.sazloop.afiliacion.domain.DocumentoEstablecimiento;
import com.sazloop.afiliacion.domain.DocumentoEstablecimiento.TipoDocumento;
import com.sazloop.afiliacion.domain.SolicitudAfiliacion;
import com.sazloop.afiliacion.domain.SolicitudAfiliacion.EstadoSolicitud;
import com.sazloop.afiliacion.dto.DocumentoResponse;
import com.sazloop.afiliacion.dto.SolicitudResponse;
import com.sazloop.afiliacion.repository.DocumentoEstablecimientoRepository;
import com.sazloop.afiliacion.repository.SolicitudAfiliacionRepository;
import com.sazloop.identidad.domain.Establecimiento;
import com.sazloop.identidad.repository.EstablecimientoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Prepara y envía la documentación de un establecimiento para revisión manual. */
@Service
public class SolicitudAfiliacionService {
    private final EstablecimientoRepository establecimientos;
    private final SolicitudAfiliacionRepository solicitudes;
    private final DocumentoEstablecimientoRepository documentos;
    private final AlmacenDocumentoService almacen;

    public SolicitudAfiliacionService(EstablecimientoRepository establecimientos,
                                     SolicitudAfiliacionRepository solicitudes,
                                     DocumentoEstablecimientoRepository documentos,
                                     AlmacenDocumentoService almacen) {
        this.establecimientos = establecimientos;
        this.solicitudes = solicitudes;
        this.documentos = documentos;
        this.almacen = almacen;
    }

    @Transactional(readOnly = true)
    public Optional<SolicitudResponse> consultar(Long usuarioId, Long localId) {
        localPropio(usuarioId, localId);
        return solicitudes.findFirstByEstablecimiento_IdOrderByIdDesc(localId).map(this::respuesta);
    }

    @Transactional
    public SolicitudResponse adjuntar(Long usuarioId, Long localId, TipoDocumento tipo,
                                     String numero, LocalDate emision, LocalDate vencimiento,
                                     MultipartFile archivo) {
        Establecimiento local = localPropio(usuarioId, localId);
        if (local.getEstado() != Establecimiento.EstadoEstablecimiento.PENDIENTE_APROBACION) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El local no admite una solicitud nueva");
        }
        SolicitudAfiliacion solicitud = solicitudes.findFirstByEstablecimiento_IdOrderByIdDesc(localId)
                .orElse(null);
        if (solicitud == null || solicitud.getEstado() == EstadoSolicitud.RECHAZADA) {
            solicitud = solicitudes.save(SolicitudAfiliacion.builder().establecimiento(local).build());
        } else if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La solicitud ya fue enviada");
        }
        if (emision != null && vencimiento != null && vencimiento.isBefore(emision)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de vencimiento precede a la emisión");
        }

        String nombre = almacen.guardar(archivo);
        DocumentoEstablecimiento documento = DocumentoEstablecimiento.builder()
                .solicitudAfiliacion(solicitud)
                .tipoDocumento(tipo)
                .numeroDocumento(limpiar(numero))
                .fechaEmision(emision)
                .fechaVencimiento(vencimiento)
                .urlArchivo(nombre)
                .build();
        try {
            documentos.saveAndFlush(documento);
            return respuesta(solicitud);
        } catch (RuntimeException ex) {
            almacen.borrar(nombre);
            throw ex;
        }
    }

    @Transactional
    public SolicitudResponse enviar(Long usuarioId, Long localId) {
        localPropio(usuarioId, localId);
        SolicitudAfiliacion solicitud = solicitudes.findFirstByEstablecimiento_IdOrderByIdDesc(localId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Adjunta documentos primero"));
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE ||
                documentos.findBySolicitudAfiliacion_IdOrderByIdAsc(solicitud.getId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La solicitud no está lista para enviarse");
        }
        solicitud.setEstado(EstadoSolicitud.EN_REVISION);
        return respuesta(solicitud);
    }

    @Transactional(readOnly = true)
    public ArchivoPrivado descargar(Long usuarioId, Long localId, Long documentoId) {
        localPropio(usuarioId, localId);
        DocumentoEstablecimiento documento = documentos.findById(documentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!documento.getSolicitudAfiliacion().getEstablecimiento().getId().equals(localId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        String nombre = documento.getUrlArchivo();
        String extension = nombre.substring(nombre.lastIndexOf('.') + 1);
        return new ArchivoPrivado(almacen.leer(nombre), extension);
    }

    private Establecimiento localPropio(Long usuarioId, Long localId) {
        Establecimiento local = establecimientos.findById(localId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Long propietarioId = local.getEmpresa().getPropietario().getUsuario().getId();
        if (!usuarioId.equals(propietarioId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return local;
    }

    private SolicitudResponse respuesta(SolicitudAfiliacion solicitud) {
        List<DocumentoResponse> lista = documentos.findBySolicitudAfiliacion_IdOrderByIdAsc(solicitud.getId())
                .stream().map(d -> new DocumentoResponse(d.getId(), d.getTipoDocumento(),
                        d.getNumeroDocumento(), d.getFechaEmision(), d.getFechaVencimiento()))
                .toList();
        return new SolicitudResponse(solicitud.getId(), solicitud.getEstablecimiento().getId(),
                solicitud.getEstado(), lista, solicitud.getEvaluacion() == null
                        ? null : solicitud.getEvaluacion().getMotivo());
    }

    private static String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public record ArchivoPrivado(byte[] contenido, String extension) {
    }
}
