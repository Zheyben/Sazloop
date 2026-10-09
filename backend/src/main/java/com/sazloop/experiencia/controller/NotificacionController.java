package com.sazloop.experiencia.controller;

import com.sazloop.experiencia.service.NotificacionService;
import com.sazloop.identidad.service.UsuarioSesionActual;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {
    private final NotificacionService avisos;
    private final UsuarioSesionActual usuario;
    public NotificacionController(NotificacionService avisos, UsuarioSesionActual usuario) {
        this.avisos = avisos; this.usuario = usuario;
    }
    @GetMapping
    public List<NotificacionService.Aviso> listar(HttpServletRequest request) {
        return avisos.listar(usuario.id(request));
    }
    @PostMapping("/{id}/leer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leer(@PathVariable Long id, HttpServletRequest request) {
        avisos.leer(usuario.id(request), id);
    }
}
