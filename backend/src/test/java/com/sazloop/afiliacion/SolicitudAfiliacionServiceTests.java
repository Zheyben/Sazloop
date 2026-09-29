package com.sazloop.afiliacion;

import com.sazloop.afiliacion.domain.DocumentoEstablecimiento.TipoDocumento;
import com.sazloop.afiliacion.domain.SolicitudAfiliacion.EstadoSolicitud;
import com.sazloop.afiliacion.dto.SolicitudResponse;
import com.sazloop.afiliacion.service.SolicitudAfiliacionService;
import com.sazloop.identidad.dto.DireccionRequest;
import com.sazloop.identidad.dto.EmpresaRequest;
import com.sazloop.identidad.dto.EstablecimientoRequest;
import com.sazloop.identidad.domain.Usuario;
import com.sazloop.identidad.repository.UsuarioRepository;
import com.sazloop.identidad.service.EmpresaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Comprueba envío real y que los documentos privados no sean accesibles a otro dueño. */
@SpringBootTest
@TestPropertySource(properties = "sazloop.documentos.directorio=target/test-documentos")
class SolicitudAfiliacionServiceTests {
    @Autowired private UsuarioRepository usuarios;
    @Autowired private EmpresaService empresas;
    @Autowired private SolicitudAfiliacionService solicitudes;

    @Test
    void propietarioEnviaDocumentoYNoLoPuedeAbrirOtraCuenta() {
        Long propietario = nuevoPropietario();
        Long otro = nuevoPropietario();
        Long local = nuevoLocal(propietario);
        byte[] contenido = "%PDF-1.4\nDocumento de prueba".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile archivo = new MockMultipartFile("archivo", "licencia.pdf", "application/pdf", contenido);

        SolicitudResponse preparada = solicitudes.adjuntar(propietario, local,
                TipoDocumento.LICENCIA_FUNCIONAMIENTO, "123", null, null, archivo);
        assertEquals(EstadoSolicitud.PENDIENTE, preparada.estado());
        assertEquals(1, preparada.documentos().size());
        Long documento = preparada.documentos().getFirst().id();
        assertArrayEquals(contenido, solicitudes.descargar(propietario, local, documento).contenido());

        ResponseStatusException denegado = assertThrows(ResponseStatusException.class,
                () -> solicitudes.descargar(otro, local, documento));
        assertEquals(HttpStatus.NOT_FOUND, denegado.getStatusCode());
        assertEquals(EstadoSolicitud.EN_REVISION, solicitudes.enviar(propietario, local).estado());
        assertThrows(ResponseStatusException.class, () -> solicitudes.adjuntar(propietario, local,
                TipoDocumento.OTRO, null, null, null, archivo));
    }

    private Long nuevoPropietario() {
        String identificador = Long.toString(ThreadLocalRandom.current()
                .nextLong(10_000_000_000L, 100_000_000_000L));
        Usuario usuario = usuarios.save(Usuario.builder().nombres("Prueba")
                .apellidos("Afiliación").email(identificador + "@example.test")
                .passwordHash("hash-de-prueba").build());
        empresas.registrarEmpresa(usuario.getId(), new EmpresaRequest("Empresa de prueba",
                "Local de prueba", identificador));
        return usuario.getId();
    }

    private Long nuevoLocal(Long propietario) {
        return empresas.registrarEstablecimiento(propietario,
                new EstablecimientoRequest("Local", null, null,
                        new DireccionRequest("Lima", "Lima", "Miraflores",
                                "Calle de prueba", null, null, null))).id();
    }
}
