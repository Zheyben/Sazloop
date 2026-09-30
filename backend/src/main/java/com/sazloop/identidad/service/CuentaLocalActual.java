package com.sazloop.identidad.service;

import com.sazloop.identidad.domain.CuentaEstablecimiento;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CuentaLocalActual {
    private final CuentaLocalService servicio;
    public CuentaLocalActual(CuentaLocalService servicio) { this.servicio = servicio; }
    public CuentaEstablecimiento obtener(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null || !(sesion.getAttribute("cuentaLocalId") instanceof Long id))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inicia sesión como establecimiento");
        return servicio.obtener(id);
    }
}
