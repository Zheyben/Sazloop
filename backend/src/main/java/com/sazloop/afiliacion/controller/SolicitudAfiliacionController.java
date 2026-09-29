package com.sazloop.afiliacion.controller;

import com.sazloop.afiliacion.domain.DocumentoEstablecimiento.TipoDocumento;
import com.sazloop.afiliacion.dto.SolicitudResponse;
import com.sazloop.afiliacion.service.SolicitudAfiliacionService;
import com.sazloop.afiliacion.service.SolicitudAfiliacionService.ArchivoPrivado;
import com.sazloop.identidad.service.UsuarioSesionActual;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/** Documentos privados de la solicitud; solo su propietario puede acceder. */
@RestController
@RequestMapping("/api/establecimientos/{localId}")
public class SolicitudAfiliacionController {
    private final SolicitudAfiliacionService solicitudes;
    private final UsuarioSesionActual usuarioActual;

    public SolicitudAfiliacionController(SolicitudAfiliacionService solicitudes, UsuarioSesionActual usuarioActual) {
        this.solicitudes = solicitudes;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping("/solicitud")
    public ResponseEntity<SolicitudResponse> consultar(@PathVariable Long localId, HttpServletRequest request) {
        return solicitudes.consultar(usuarioActual.id(request), localId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping(value = "/documentos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitudResponse adjuntar(@PathVariable Long localId,
                                     @RequestParam TipoDocumento tipo,
                                     @RequestParam(required = false) String numero,
                                     @RequestParam(required = false)
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate emision,
                                     @RequestParam(required = false)
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vencimiento,
                                     @RequestParam MultipartFile archivo, HttpServletRequest request) {
        return solicitudes.adjuntar(usuarioActual.id(request), localId,
                tipo, numero, emision, vencimiento, archivo);
    }

    @PostMapping("/solicitud/enviar")
    public SolicitudResponse enviar(@PathVariable Long localId, HttpServletRequest request) {
        return solicitudes.enviar(usuarioActual.id(request), localId);
    }

    @GetMapping("/documentos/{documentoId}/archivo")
    public ResponseEntity<byte[]> descargar(@PathVariable Long localId, @PathVariable Long documentoId,
                                            HttpServletRequest request) {
        ArchivoPrivado archivo = solicitudes.descargar(usuarioActual.id(request), localId, documentoId);
        MediaType tipo = switch (archivo.extension()) {
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "png" -> MediaType.IMAGE_PNG;
            default -> MediaType.IMAGE_JPEG;
        };
        return ResponseEntity.ok().contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"documento." + archivo.extension() + "\"")
                .body(archivo.contenido());
    }
}
