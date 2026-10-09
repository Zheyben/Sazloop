package com.sazloop.reservas.controller;

import com.sazloop.identidad.service.CuentaLocalActual;
import com.sazloop.identidad.service.UsuarioSesionActual;
import com.sazloop.reservas.dto.*;
import com.sazloop.reservas.service.CompraPresencialService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CompraPresencialController {
    private final CompraPresencialService servicio;
    private final UsuarioSesionActual usuario;
    private final CuentaLocalActual local;

    public CompraPresencialController(CompraPresencialService servicio,
            UsuarioSesionActual usuario, CuentaLocalActual local) {
        this.servicio = servicio; this.usuario = usuario; this.local = local;
    }

    @PostMapping("/compras/presenciales")
    @ResponseStatus(HttpStatus.CREATED)
    public CompraResponse digital(@Valid @RequestBody CompraPresencialRequest entrada, HttpServletRequest request) {
        return servicio.digital(usuario.id(request), entrada);
    }

    @PostMapping("/compras/presenciales/efectivo")
    @ResponseStatus(HttpStatus.CREATED)
    public RetencionResponse efectivo(@Valid @RequestBody CompraPresencialRequest entrada, HttpServletRequest request) {
        return servicio.reservarEfectivo(usuario.id(request), entrada);
    }

    @PostMapping("/local/efectivo/{codigo}/confirmar")
    public CompraResponse confirmar(@PathVariable String codigo, HttpServletRequest request) {
        return servicio.confirmarEfectivo(local.obtener(request), codigo);
    }
}
