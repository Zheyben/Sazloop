package com.sazloop.afiliacion.service;

import com.sazloop.afiliacion.domain.AdministradorPlataforma;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AdministradorSesionActual {
    private final AdministradorService servicio;
    public AdministradorSesionActual(AdministradorService servicio) { this.servicio = servicio; }

    public AdministradorPlataforma obtener(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null || !(sesion.getAttribute("administradorId") instanceof Long id)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inicia sesión como administrador");
        }
        return servicio.obtener(id);
    }
}
