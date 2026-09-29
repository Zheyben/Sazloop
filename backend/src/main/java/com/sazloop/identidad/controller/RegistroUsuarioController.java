package com.sazloop.identidad.controller;

import com.sazloop.identidad.dto.RegistroUsuarioRequest;
import com.sazloop.identidad.dto.UsuarioResponse;
import com.sazloop.identidad.service.RegistroUsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Punto de entrada HTTP para el registro de usuarios (RF-01). */
@RestController
@CrossOrigin(origins = "http://localhost:5500", allowCredentials = "true")
@RequestMapping("/api/usuarios")
public class RegistroUsuarioController {
    private final RegistroUsuarioService registro;

    public RegistroUsuarioController(RegistroUsuarioService registro) {
        this.registro = registro;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroUsuarioRequest entrada) {
        return registro.registrar(entrada);
    }
}
