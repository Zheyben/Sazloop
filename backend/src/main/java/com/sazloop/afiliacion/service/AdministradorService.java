package com.sazloop.afiliacion.service;

import com.sazloop.afiliacion.domain.AdministradorPlataforma;
import com.sazloop.afiliacion.repository.AdministradorPlataformaRepository;
import com.sazloop.identidad.service.CodificadorContrasena;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;

/** Acceso reservado al personal interno; jamás expone registro público. */
@Service
public class AdministradorService {
    private final AdministradorPlataformaRepository administradores;
    private final CodificadorContrasena codificador;

    public AdministradorService(AdministradorPlataformaRepository administradores,
                                CodificadorContrasena codificador) {
        this.administradores = administradores;
        this.codificador = codificador;
    }

    @Transactional(readOnly = true)
    public AdministradorPlataforma iniciar(String email, String clave) {
        AdministradorPlataforma admin = administradores.findByEmailInstitucionalIgnoreCase(
                email.trim()).orElseThrow(AdministradorService::denegado);
        if (!admin.isActivo() || !codificador.coincide(clave, admin.getPasswordHash())) throw denegado();
        return admin;
    }

    @Transactional(readOnly = true)
    public AdministradorPlataforma obtener(Long id) {
        return administradores.findById(id).filter(AdministradorPlataforma::isActivo)
                .orElseThrow(AdministradorService::denegado);
    }

    /** Credenciales facilitadas al iniciar el servidor, nunca enviadas a GitHub. */
    @Bean
    @Profile("!demo")
    CommandLineRunner provisionarAdministrador(
            @Value("${sazloop.admin.email:}") String email,
            @Value("${sazloop.admin.password:}") String clave) {
        return args -> {
            if (email.isBlank() || clave.isBlank()) return;
            String normalizado = email.trim().toLowerCase(Locale.ROOT);
            if (administradores.findByEmailInstitucionalIgnoreCase(normalizado).isEmpty()) {
                administradores.save(AdministradorPlataforma.builder().nombre("Administrador")
                        .emailInstitucional(normalizado)
                        .passwordHash(codificador.codificar(clave)).build());
            }
        };
    }

    private static ResponseStatusException denegado() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Acceso administrativo inválido");
    }
}
