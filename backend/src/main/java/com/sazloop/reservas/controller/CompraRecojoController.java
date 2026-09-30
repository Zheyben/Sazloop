package com.sazloop.reservas.controller;

import com.sazloop.identidad.service.*;
import com.sazloop.reservas.dto.*;
import com.sazloop.reservas.service.CompraRecojoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CompraRecojoController {
    private final CompraRecojoService servicio;
    private final UsuarioSesionActual usuario;
    private final CuentaLocalActual local;
    public CompraRecojoController(CompraRecojoService servicio, UsuarioSesionActual usuario,
                                  CuentaLocalActual local) {
        this.servicio = servicio; this.usuario = usuario; this.local = local;
    }

    @PostMapping("/compras")
    @ResponseStatus(HttpStatus.CREATED)
    public CompraResponse comprar(@Valid @RequestBody CompraRequest entrada, HttpServletRequest request) {
        return servicio.comprar(usuario.id(request), entrada);
    }

    @GetMapping("/compras/mias")
    public List<CompraResponse> mias(HttpServletRequest request) {
        return servicio.misCompras(usuario.id(request));
    }

    @GetMapping("/local/recojos/{codigo}")
    public CompraResponse buscarCodigo(@PathVariable String codigo, HttpServletRequest request) {
        return servicio.consultarCodigo(local.obtener(request), codigo);
    }

    @PostMapping("/local/recojos/{codigo}/confirmar")
    public CompraResponse confirmar(@PathVariable @NotBlank String codigo, HttpServletRequest request) {
        return servicio.recoger(local.obtener(request), codigo);
    }
}
