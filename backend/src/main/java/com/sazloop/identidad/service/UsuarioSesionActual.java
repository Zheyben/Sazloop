package com.sazloop.identidad.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Identifica a la persona autenticada y comprueba que su cuenta siga activa. */
@Component
public class UsuarioSesionActual {
    private final SesionUsuarioService sesiones;

    public UsuarioSesionActual(SesionUsuarioService sesiones) {
        this.sesiones = sesiones;
    }

    public Long id(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null || !(sesion.getAttribute("usuarioId") instanceof Long id)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inicia sesión");
        }
        sesiones.obtener(id);
        return id;
    }
}
