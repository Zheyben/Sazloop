package com.sazloop.shared;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.List;

/**
 * Las tres sesiones (persona, cuenta del local y administrador) comparten la cookie del navegador,
 * pero cada una guarda su propio atributo. Abrir o cerrar una no afecta a las otras, de modo que
 * una persona puede tener abiertos a la vez, por ejemplo, el portal de empresa y el de administración.
 */
public final class SesionesPorRol {
    public static final String PERSONA = "usuarioId";
    public static final String LOCAL = "cuentaLocalId";
    public static final String ADMINISTRADOR = "administradorId";
    private static final List<String> TODAS = List.of(PERSONA, LOCAL, ADMINISTRADOR);

    private SesionesPorRol() {}

    /** Guarda la identidad del rol; renueva el identificador de sesión para evitar su fijación. */
    public static void abrir(HttpServletRequest request, String rol, Object identificador) {
        HttpSession sesion = request.getSession(true);
        request.changeSessionId();
        sesion.setAttribute(rol, identificador);
    }

    /** Quita solo la identidad del rol; la sesión se invalida cuando ya no queda ninguna. */
    public static void cerrar(HttpServletRequest request, String rol) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null) return;
        sesion.removeAttribute(rol);
        if (TODAS.stream().noneMatch(otro -> sesion.getAttribute(otro) != null)) sesion.invalidate();
    }
}
