package com.sazloop.identidad.controller;

import com.sazloop.identidad.domain.CuentaEstablecimiento;
import com.sazloop.identidad.dto.CuentaLocalRequest;
import com.sazloop.identidad.service.*;
import com.sazloop.shared.SesionesPorRol;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CuentaLocalController {
    private final CuentaLocalService servicio;
    private final CuentaLocalActual actual;
    private final UsuarioSesionActual usuario;
    public CuentaLocalController(CuentaLocalService servicio, CuentaLocalActual actual,
                                 UsuarioSesionActual usuario) {
        this.servicio = servicio;
        this.actual = actual;
        this.usuario = usuario;
    }

    @PostMapping("/establecimientos/{id}/cuenta")
    @ResponseStatus(HttpStatus.CREATED)
    public CuentaResponse crear(@PathVariable Long id, @Valid @RequestBody CuentaLocalRequest entrada,
                                HttpServletRequest request) {
        return respuesta(servicio.crear(usuario.id(request), id, entrada));
    }

    @PostMapping("/local/sesion")
    public CuentaResponse entrar(@Valid @RequestBody CuentaLocalRequest entrada, HttpServletRequest request) {
        CuentaEstablecimiento cuenta = servicio.iniciar(entrada);
        SesionesPorRol.abrir(request, SesionesPorRol.LOCAL, cuenta.getId());
        return respuesta(cuenta);
    }

    @GetMapping("/local/sesion")
    public CuentaResponse consultar(HttpServletRequest request) { return respuesta(actual.obtener(request)); }

    @DeleteMapping("/local/sesion")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void salir(HttpServletRequest request) {
        actual.obtener(request);
        SesionesPorRol.cerrar(request, SesionesPorRol.LOCAL);
    }

    private static CuentaResponse respuesta(CuentaEstablecimiento cuenta) {
        return new CuentaResponse(cuenta.getId(), cuenta.getNombreUsuario(),
                cuenta.getEstablecimiento().getId());
    }
    public record CuentaResponse(Long id, String nombreUsuario, Long establecimientoId) {}
}
