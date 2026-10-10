package com.sazloop;

import com.sazloop.afiliacion.domain.AdministradorPlataforma;
import com.sazloop.afiliacion.domain.DocumentoEstablecimiento.TipoDocumento;
import com.sazloop.afiliacion.domain.RevisionDocumento.ResultadoRevision;
import com.sazloop.afiliacion.dto.RevisionRequest;
import com.sazloop.afiliacion.repository.AdministradorPlataformaRepository;
import com.sazloop.afiliacion.service.RevisionAfiliacionService;
import com.sazloop.afiliacion.service.SolicitudAfiliacionService;
import com.sazloop.experiencia.service.NotificacionService;
import com.sazloop.identidad.domain.Usuario;
import com.sazloop.identidad.dto.DireccionRequest;
import com.sazloop.identidad.dto.EmpresaRequest;
import com.sazloop.identidad.dto.EstablecimientoRequest;
import com.sazloop.identidad.repository.UsuarioRepository;
import com.sazloop.identidad.service.CodificadorContrasena;
import com.sazloop.identidad.service.EmpresaService;
import com.sazloop.identidad.service.IndicadoresEmpresaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Las consultas del portal de empresa deben responder en cualquier etapa de la afiliación
 * (sin transacción abierta, como las recibe el controlador), sin lanzar errores internos.
 */
@SpringBootTest
@TestPropertySource(properties = "sazloop.documentos.directorio=target/test-documentos")
class PortalEmpresaEtapasTests {
    @Autowired UsuarioRepository usuarios;
    @Autowired EmpresaService empresas;
    @Autowired SolicitudAfiliacionService solicitudes;
    @Autowired RevisionAfiliacionService revision;
    @Autowired AdministradorPlataformaRepository administradores;
    @Autowired CodificadorContrasena contrasenas;
    @Autowired IndicadoresEmpresaService indicadores;
    @Autowired NotificacionService notificaciones;

    @Test
    void lasConsultasDelPortalRespondenEnCadaEtapa() {
        Long propietario = usuarios.save(Usuario.builder().nombres("Elena").apellidos("Quispe")
                .email(UUID.randomUUID() + "@example.test").passwordHash("hash").build()).getId();
        assertTrue(empresas.obtenerEmpresa(propietario).isEmpty());
        String ruc = Long.toString(ThreadLocalRandom.current().nextLong(10_000_000_000L, 100_000_000_000L));
        empresas.registrarEmpresa(propietario, new EmpresaRequest("Empresa", "Comercio", ruc));
        consultar(propietario, null, "empresa sin locales");

        Long local = empresas.registrarEstablecimiento(propietario, new EstablecimientoRequest("Local etapas", "", "",
                new DireccionRequest("Lima", "Lima", "Barranco", "Calle Seis", "", "", ""))).id();
        consultar(propietario, local, "local nuevo sin documentos");

        var preparada = solicitudes.adjuntar(propietario, local, TipoDocumento.CERTIFICADO_SANITARIO, null, null, null, archivo());
        solicitudes.adjuntar(propietario, local, TipoDocumento.LICENCIA_FUNCIONAMIENTO, null, null,
                LocalDate.now().plusYears(1), archivo());
        consultar(propietario, local, "documentos adjuntos");

        solicitudes.enviar(propietario, local);
        consultar(propietario, local, "solicitud enviada");

        AdministradorPlataforma admin = administradores.save(AdministradorPlataforma.builder().nombre("Revisor")
                .emailInstitucional(UUID.randomUUID() + "@example.test")
                .passwordHash(contrasenas.codificar("clave-segura")).build());
        var enRevision = revision.consultar(preparada.id());
        revision.revisar(admin, enRevision.documentos().getFirst().id(),
                new RevisionRequest(ResultadoRevision.APROBADO, "Legible"));
        consultar(propietario, local, "un documento revisado y sin decisión");
        assertEquals(1, revision.pendientes().stream().filter(s -> s.id().equals(preparada.id())).count());
        assertFalse(revision.establecimientos().isEmpty());
    }

    private void consultar(Long propietario, Long local, String etapa) {
        assertDoesNotThrow(() -> {
            empresas.obtenerEmpresa(propietario);
            empresas.listarEstablecimientos(propietario);
            var resumen = indicadores.resumen(propietario);
            assertNotNull(resumen.total(), etapa);
            notificaciones.listar(propietario);
            if (local != null) {
                var documentacion = solicitudes.documentacion(propietario, local);
                assertNotNull(documentacion.estado(), etapa);
                solicitudes.consultar(propietario, local);
            }
        }, "falló la consulta del portal en la etapa: " + etapa);
    }

    private static MockMultipartFile archivo() {
        return new MockMultipartFile("archivo", "documento.pdf", "application/pdf",
                "%PDF-1.4\nPrueba".getBytes(StandardCharsets.UTF_8));
    }
}
