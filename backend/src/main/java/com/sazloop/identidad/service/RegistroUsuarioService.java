package com.sazloop.identidad.service;

import com.sazloop.identidad.domain.Usuario;
import com.sazloop.identidad.dto.RegistroUsuarioRequest;
import com.sazloop.identidad.dto.UsuarioResponse;
import com.sazloop.identidad.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

/** Registra una identidad única sin asignarle todavía perfiles de negocio. */
@Service
public class RegistroUsuarioService {
    private final UsuarioRepository usuarios;
    private final CodificadorContrasena codificador;

    public RegistroUsuarioService(UsuarioRepository usuarios, CodificadorContrasena codificador) {
        this.usuarios = usuarios;
        this.codificador = codificador;
    }

    @Transactional
    public UsuarioResponse registrar(RegistroUsuarioRequest entrada) {
        String email = entrada.email().trim().toLowerCase(Locale.ROOT);
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }

        Usuario usuario = Usuario.builder()
                .nombres(entrada.nombres().trim())
                .apellidos(entrada.apellidos().trim())
                .email(email)
                .telefono(entrada.telefono() == null ? null : entrada.telefono().trim())
                .passwordHash(codificador.codificar(entrada.contrasena()))
                .build();

        try {
            Usuario guardado = usuarios.saveAndFlush(usuario);
            return new UsuarioResponse(guardado.getId(), guardado.getNombres(),
                    guardado.getApellidos(), guardado.getEmail());
        } catch (DataIntegrityViolationException ex) {
            // Protege también el caso de dos registros concurrentes con el mismo correo.
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se pudo registrar el correo", ex);
        }
    }
}
