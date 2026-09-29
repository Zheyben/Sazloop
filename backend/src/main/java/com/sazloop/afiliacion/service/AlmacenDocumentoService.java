package com.sazloop.afiliacion.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/** Guarda documentos fuera de la carpeta web; solo los controladores autorizados los sirven. */
@Service
public class AlmacenDocumentoService {
    private static final int MAX_BYTES = 10 * 1024 * 1024;
    private final Path directorio;

    public AlmacenDocumentoService(@Value("${sazloop.documentos.directorio:data/documentos}") String ruta) {
        this.directorio = Path.of(ruta).toAbsolutePath().normalize();
    }

    public String guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty() || archivo.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona un documento de hasta 10 MB");
        }
        try {
            byte[] contenido = archivo.getBytes();
            String extension = extensionPermitida(contenido);
            String nombre = UUID.randomUUID() + extension;
            Files.createDirectories(directorio);
            Files.write(directorio.resolve(nombre), contenido);
            return nombre;
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo guardar el documento", ex);
        }
    }

    public byte[] leer(String nombre) {
        Path archivo = directorio.resolve(nombre).normalize();
        if (!archivo.startsWith(directorio) || !nombre.matches("[a-f0-9-]{36}\\.(pdf|png|jpg)")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        try {
            return Files.readAllBytes(archivo);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento no disponible", ex);
        }
    }

    public void borrar(String nombre) {
        try {
            Files.deleteIfExists(directorio.resolve(nombre));
        } catch (IOException ignored) {
            // El error de persistencia original se conserva para el cliente.
        }
    }

    private static String extensionPermitida(byte[] datos) {
        if (datos.length >= 4 && datos[0] == '%' && datos[1] == 'P' && datos[2] == 'D' && datos[3] == 'F') {
            return ".pdf";
        }
        if (datos.length >= 8 && (datos[0] & 0xff) == 0x89 && datos[1] == 'P'
                && datos[2] == 'N' && datos[3] == 'G') {
            return ".png";
        }
        if (datos.length >= 3 && (datos[0] & 0xff) == 0xff && (datos[1] & 0xff) == 0xd8
                && (datos[2] & 0xff) == 0xff) {
            return ".jpg";
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adjunta un archivo PDF, PNG o JPG");
    }
}
