package com.sazloop.identidad.controller;

import com.sazloop.identidad.dto.EmpresaRequest;
import com.sazloop.identidad.dto.EmpresaResponse;
import com.sazloop.identidad.dto.EstablecimientoRequest;
import com.sazloop.identidad.dto.EstablecimientoResponse;
import com.sazloop.identidad.service.EmpresaService;
import com.sazloop.identidad.service.SesionUsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** Solo la persona que inició sesión gestiona su propia empresa y locales. */
@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {
    private final EmpresaService empresas;
    private final SesionUsuarioService sesiones;

    public EmpresaController(EmpresaService empresas, SesionUsuarioService sesiones) {
        this.empresas = empresas;
        this.sesiones = sesiones;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpresaResponse registrar(@Valid @RequestBody EmpresaRequest entrada, HttpServletRequest request) {
        return empresas.registrarEmpresa(usuarioId(request), entrada);
    }

    @GetMapping("/actual")
    public ResponseEntity<EmpresaResponse> actual(HttpServletRequest request) {
        return empresas.obtenerEmpresa(usuarioId(request))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/actual/establecimientos")
    @ResponseStatus(HttpStatus.CREATED)
    public EstablecimientoResponse registrarLocal(@Valid @RequestBody EstablecimientoRequest entrada,
                                                   HttpServletRequest request) {
        return empresas.registrarEstablecimiento(usuarioId(request), entrada);
    }

    @GetMapping("/actual/establecimientos")
    public List<EstablecimientoResponse> listarLocales(HttpServletRequest request) {
        return empresas.listarEstablecimientos(usuarioId(request));
    }

    private Long usuarioId(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null || !(sesion.getAttribute("usuarioId") instanceof Long id)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inicia sesión");
        }
        sesiones.obtener(id);
        return id;
    }
}
