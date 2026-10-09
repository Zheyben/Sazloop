package com.sazloop.reservas.controller;

import com.sazloop.identidad.service.UsuarioSesionActual;
import com.sazloop.reservas.service.CancelacionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compras")
public class CancelacionController {
    private final CancelacionService cancelaciones;
    private final UsuarioSesionActual usuario;

    public CancelacionController(CancelacionService cancelaciones, UsuarioSesionActual usuario) {
        this.cancelaciones = cancelaciones;
        this.usuario = usuario;
    }

    @PostMapping("/{reservaId}/cancelar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable Long reservaId, HttpServletRequest request) {
        cancelaciones.cancelar(usuario.id(request), reservaId);
    }
}
