package com.sazloop.identidad.controller;

import com.sazloop.identidad.dto.CambioContrasenaRequest;
import com.sazloop.identidad.dto.PerfilRequest;
import com.sazloop.identidad.dto.UsuarioResponse;
import com.sazloop.identidad.service.PerfilService;
import com.sazloop.identidad.service.UsuarioSesionActual;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {
    private final PerfilService perfiles;
    private final UsuarioSesionActual sesion;

    public PerfilController(PerfilService perfiles, UsuarioSesionActual sesion) {
        this.perfiles = perfiles;
        this.sesion = sesion;
    }

    @PutMapping
    public UsuarioResponse actualizar(@Valid @RequestBody PerfilRequest datos, HttpServletRequest request) {
        return perfiles.actualizar(sesion.id(request), datos);
    }

    @PutMapping("/contrasena")
    public ResponseEntity<Void> cambiarContrasena(@Valid @RequestBody CambioContrasenaRequest datos,
            HttpServletRequest request) {
        perfiles.cambiarContrasena(sesion.id(request), datos);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> foto(@RequestParam MultipartFile archivo, HttpServletRequest request) {
        perfiles.guardarFoto(sesion.id(request), archivo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/foto")
    public ResponseEntity<byte[]> foto(HttpServletRequest request) {
        var foto = perfiles.foto(sesion.id(request));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .contentType(MediaType.parseMediaType(foto.tipo())).body(foto.contenido());
    }
}
