package com.sazloop.afiliacion.controller;

import com.sazloop.afiliacion.dto.*;
import com.sazloop.afiliacion.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/administradores")
public class RevisionAfiliacionController {
    private final AdministradorSesionActual sesion;
    private final RevisionAfiliacionService servicio;
    public RevisionAfiliacionController(AdministradorSesionActual sesion, RevisionAfiliacionService servicio) {
        this.sesion = sesion;
        this.servicio = servicio;
    }

    @GetMapping("/solicitudes")
    public List<SolicitudRevisionResponse> pendientes(HttpServletRequest request) {
        sesion.obtener(request);
        return servicio.pendientes();
    }

    @GetMapping("/establecimientos")
    public List<EstablecimientoAdminResponse> establecimientos(HttpServletRequest request) {
        sesion.obtener(request);
        return servicio.establecimientos();
    }

    @GetMapping("/solicitudes/{id}")
    public SolicitudRevisionResponse consultar(@PathVariable Long id, HttpServletRequest request) {
        sesion.obtener(request);
        return servicio.consultar(id);
    }

    @GetMapping("/documentos/{id}/archivo")
    public ResponseEntity<byte[]> archivo(@PathVariable Long id, HttpServletRequest request) {
        sesion.obtener(request);
        SolicitudAfiliacionService.ArchivoPrivado archivo = servicio.descargar(id);
        MediaType tipo = switch (archivo.extension()) {
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "png" -> MediaType.IMAGE_PNG;
            default -> MediaType.IMAGE_JPEG;
        };
        return ResponseEntity.ok().contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"documento." + archivo.extension() + "\"")
                .body(archivo.contenido());
    }

    @PostMapping("/documentos/{id}/revision")
    public SolicitudRevisionResponse revisar(@PathVariable Long id, @Valid @RequestBody RevisionRequest entrada,
                                               HttpServletRequest request) {
        var admin = sesion.obtener(request);
        return servicio.revisar(admin, id, entrada);
    }

    @PostMapping("/solicitudes/{id}/resolucion")
    public SolicitudRevisionResponse resolver(@PathVariable Long id, @Valid @RequestBody ResolucionRequest entrada,
                                              HttpServletRequest request) {
        return servicio.resolver(sesion.obtener(request), id, entrada);
    }

    @PostMapping("/solicitudes/{id}/decision")
    public SolicitudRevisionResponse decidir(@PathVariable Long id, @Valid @RequestBody DecisionRequest entrada,
                                               HttpServletRequest request) {
        return servicio.decidir(sesion.obtener(request), id, entrada);
    }
}
