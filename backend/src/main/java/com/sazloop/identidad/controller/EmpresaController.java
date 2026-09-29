package com.sazloop.identidad.controller;

import com.sazloop.identidad.dto.EmpresaRequest;
import com.sazloop.identidad.dto.EmpresaResponse;
import com.sazloop.identidad.dto.EstablecimientoRequest;
import com.sazloop.identidad.dto.EstablecimientoResponse;
import com.sazloop.identidad.service.EmpresaService;
import com.sazloop.identidad.service.UsuarioSesionActual;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Solo la persona que inició sesión gestiona su propia empresa y locales. */
@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {
    private final EmpresaService empresas;
    private final UsuarioSesionActual usuarioActual;

    public EmpresaController(EmpresaService empresas, UsuarioSesionActual usuarioActual) {
        this.empresas = empresas;
        this.usuarioActual = usuarioActual;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpresaResponse registrar(@Valid @RequestBody EmpresaRequest entrada, HttpServletRequest request) {
        return empresas.registrarEmpresa(usuarioActual.id(request), entrada);
    }

    @GetMapping("/actual")
    public ResponseEntity<EmpresaResponse> actual(HttpServletRequest request) {
        return empresas.obtenerEmpresa(usuarioActual.id(request))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/actual/establecimientos")
    @ResponseStatus(HttpStatus.CREATED)
    public EstablecimientoResponse registrarLocal(@Valid @RequestBody EstablecimientoRequest entrada,
                                                   HttpServletRequest request) {
        return empresas.registrarEstablecimiento(usuarioActual.id(request), entrada);
    }

    @GetMapping("/actual/establecimientos")
    public List<EstablecimientoResponse> listarLocales(HttpServletRequest request) {
        return empresas.listarEstablecimientos(usuarioActual.id(request));
    }
}
