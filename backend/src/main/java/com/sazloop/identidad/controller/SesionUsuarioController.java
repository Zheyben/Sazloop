package com.sazloop.identidad.controller;

import com.sazloop.identidad.dto.InicioSesionRequest;
import com.sazloop.identidad.dto.UsuarioResponse;
import com.sazloop.identidad.service.SesionUsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Inicio, consulta y cierre de sesión de cuentas personales (RF-02). */
@RestController
@RequestMapping("/api/sesion")
public class SesionUsuarioController {
    private final SesionUsuarioService sesiones;

    public SesionUsuarioController(SesionUsuarioService sesiones) {
        this.sesiones = sesiones;
    }

    @PostMapping
    public UsuarioResponse iniciar(@Valid @RequestBody InicioSesionRequest entrada, HttpServletRequest request) {
        UsuarioResponse usuario = sesiones.iniciar(entrada);
        HttpSession sesion = request.getSession(true);
        request.changeSessionId();
        sesion.setAttribute("usuarioId", usuario.id());
        return usuario;
    }

    @GetMapping
    public UsuarioResponse actual(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null || !(sesion.getAttribute("usuarioId") instanceof Long id)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inicia sesión");
        }
        return sesiones.obtener(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cerrar(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
    }
}
