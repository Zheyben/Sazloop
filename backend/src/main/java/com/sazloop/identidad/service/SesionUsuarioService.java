package com.sazloop.identidad.service;

import com.sazloop.identidad.domain.Usuario;
import com.sazloop.identidad.dto.InicioSesionRequest;
import com.sazloop.identidad.dto.UsuarioResponse;
import com.sazloop.identidad.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Verifica la identidad del usuario registrado sin exponer la contraseña. */
@Service
public class SesionUsuarioService {
    private final UsuarioRepository usuarios;
    private final CodificadorContrasena codificador;

    public SesionUsuarioService(UsuarioRepository usuarios, CodificadorContrasena codificador) {
        this.usuarios = usuarios;
        this.codificador = codificador;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse iniciar(InicioSesionRequest entrada) {
        Usuario usuario = usuarios.findByEmailIgnoreCase(entrada.email().trim())
                .orElseThrow(SesionUsuarioService::accesoDenegado);
        if (!usuario.isActivo() || !codificador.coincide(entrada.contrasena(), usuario.getPasswordHash())) {
            throw accesoDenegado();
        }
        return respuesta(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        Usuario usuario = usuarios.findById(id).orElseThrow(SesionUsuarioService::accesoDenegado);
        if (!usuario.isActivo()) {
            throw accesoDenegado();
        }
        return respuesta(usuario);
    }

    private static UsuarioResponse respuesta(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombres(),
                usuario.getApellidos(), usuario.getEmail());
    }

    private static ResponseStatusException accesoDenegado() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
    }
}
