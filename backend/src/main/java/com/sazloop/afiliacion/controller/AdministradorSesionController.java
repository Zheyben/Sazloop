package com.sazloop.afiliacion.controller;

import com.sazloop.afiliacion.domain.AdministradorPlataforma;
import com.sazloop.afiliacion.service.AdministradorService;
import com.sazloop.afiliacion.service.AdministradorSesionActual;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/administradores/sesion")
public class AdministradorSesionController {
    private final AdministradorService servicio;
    private final AdministradorSesionActual sesionActual;
    public AdministradorSesionController(AdministradorService servicio, AdministradorSesionActual sesionActual) {
        this.servicio = servicio;
        this.sesionActual = sesionActual;
    }

    @PostMapping
    public AdministradorResponse entrar(@Valid @RequestBody Credenciales entrada, HttpServletRequest request) {
        AdministradorPlataforma admin = servicio.iniciar(entrada.email(), entrada.contrasena());
        HttpSession anterior = request.getSession(false);
        if (anterior != null) anterior.invalidate();
        HttpSession sesion = request.getSession(true);
        sesion.setAttribute("administradorId", admin.getId());
        return respuesta(admin);
    }

    @GetMapping
    public AdministradorResponse actual(HttpServletRequest request) {
        return respuesta(sesionActual.obtener(request));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void salir(HttpServletRequest request) {
        sesionActual.obtener(request);
        request.getSession(false).invalidate();
    }

    private static AdministradorResponse respuesta(AdministradorPlataforma admin) {
        return new AdministradorResponse(admin.getId(), admin.getNombre(), admin.getEmailInstitucional());
    }
    public record Credenciales(@NotBlank @Email String email, @NotBlank String contrasena) {}
    public record AdministradorResponse(Long id, String nombre, String email) {}
}
