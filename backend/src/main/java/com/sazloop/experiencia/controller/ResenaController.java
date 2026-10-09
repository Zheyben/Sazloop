package com.sazloop.experiencia.controller;

import com.sazloop.experiencia.dto.ResenaRequest;
import com.sazloop.experiencia.service.ResenaService;
import com.sazloop.identidad.service.UsuarioSesionActual;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compras")
public class ResenaController {
    private final ResenaService resenas;
    private final UsuarioSesionActual usuario;

    public ResenaController(ResenaService resenas, UsuarioSesionActual usuario) {
        this.resenas = resenas;
        this.usuario = usuario;
    }

    @PostMapping("/{reservaId}/resena/omitir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void omitir(@PathVariable Long reservaId, HttpServletRequest request) {
        resenas.omitir(usuario.id(request), reservaId);
    }

    @PostMapping("/{reservaId}/resena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void registrar(@PathVariable Long reservaId, @Valid @RequestBody ResenaRequest entrada,
                         HttpServletRequest request) {
        resenas.registrar(usuario.id(request), reservaId, entrada);
    }
}
