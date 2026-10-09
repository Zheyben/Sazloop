package com.sazloop.identidad.service;

import com.sazloop.identidad.domain.Usuario;
import com.sazloop.identidad.dto.CambioContrasenaRequest;
import com.sazloop.identidad.dto.PerfilRequest;
import com.sazloop.identidad.dto.UsuarioResponse;
import com.sazloop.identidad.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.util.Locale;

@Service
public class PerfilService {
    private final UsuarioRepository usuarios;
    private final CodificadorContrasena codificador;

    public PerfilService(UsuarioRepository usuarios, CodificadorContrasena codificador) {
        this.usuarios = usuarios;
        this.codificador = codificador;
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, PerfilRequest datos) {
        Usuario usuario = usuario(id);
        String email = datos.email().trim().toLowerCase(Locale.ROOT);
        if (usuarios.findByEmailIgnoreCase(email).filter(otro -> !otro.getId().equals(id)).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        usuario.setNombres(datos.nombres().trim());
        usuario.setApellidos(datos.apellidos().trim());
        usuario.setEmail(email);
        usuario.setTelefono(datos.telefono() == null || datos.telefono().isBlank() ? null : datos.telefono().trim());
        usuarios.flush();
        return new UsuarioResponse(id, usuario.getNombres(), usuario.getApellidos(), email,
                usuario.getTelefono(), usuario.getFotoPerfil() != null);
    }

    @Transactional
    public void cambiarContrasena(Long id, CambioContrasenaRequest datos) {
        Usuario usuario = usuario(id);
        if (!codificador.coincide(datos.actual(), usuario.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual no coincide");
        }
        usuario.setPasswordHash(codificador.codificar(datos.nueva()));
    }

    @Transactional
    public void guardarFoto(Long id, MultipartFile archivo) {
        Usuario usuario = usuario(id);
        if (archivo == null || archivo.isEmpty() || archivo.getSize() > 2_000_000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La foto debe pesar hasta 2 MB");
        }
        try {
            byte[] bytes = archivo.getBytes();
            String tipo;
            if (bytes.length >= 8 && bytes[0] == (byte) 0x89 && bytes[1] == 0x50 &&
                    bytes[2] == 0x4e && bytes[3] == 0x47 && bytes[4] == 0x0d &&
                    bytes[5] == 0x0a && bytes[6] == 0x1a && bytes[7] == 0x0a) tipo = "image/png";
            else if (bytes.length >= 3 && bytes[0] == (byte) 0xff &&
                    bytes[1] == (byte) 0xd8 && bytes[2] == (byte) 0xff) tipo = "image/jpeg";
            else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usa una foto PNG o JPG");
            usuario.setFotoPerfil(bytes);
            usuario.setFotoPerfilTipo(tipo);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer la foto", ex);
        }
    }

    @Transactional(readOnly = true)
    public Foto foto(Long id) {
        Usuario usuario = usuario(id);
        if (usuario.getFotoPerfil() == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return new Foto(usuario.getFotoPerfilTipo(), usuario.getFotoPerfil());
    }

    public record Foto(String tipo, byte[] contenido) {}

    private Usuario usuario(Long id) {
        return usuarios.findById(id).filter(Usuario::isActivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
