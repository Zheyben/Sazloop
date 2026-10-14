package com.sazloop;

import com.sazloop.afiliacion.repository.AdministradorPlataformaRepository;
import com.sazloop.afiliacion.repository.DocumentoEstablecimientoRepository;
import com.sazloop.afiliacion.repository.SolicitudAfiliacionRepository;
import com.sazloop.afiliacion.service.AdministradorService;
import com.sazloop.afiliacion.service.AlmacenDocumentoService;
import com.sazloop.identidad.domain.Establecimiento;
import com.sazloop.identidad.dto.CuentaLocalRequest;
import com.sazloop.identidad.repository.CuentaEstablecimientoRepository;
import com.sazloop.identidad.repository.ConsumidorRepository;
import com.sazloop.identidad.repository.EmpresaRepository;
import com.sazloop.identidad.repository.EstablecimientoRepository;
import com.sazloop.identidad.repository.UsuarioRepository;
import com.sazloop.identidad.service.CodificadorContrasena;
import com.sazloop.identidad.service.CuentaLocalService;
import com.sazloop.ofertas.service.OfertaService;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/** Comprueba que la demostración se pueda usar sin modificar ni abrir MySQL. */
@SpringBootTest
@ActiveProfiles({"dev", "demo"})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:sazloop_demo_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "sazloop.documentos.directorio=target/test-demo-documentos"
})
class DatosDemoH2Tests {
    @Autowired EmpresaRepository empresas;
    @Autowired EstablecimientoRepository locales;
    @Autowired UsuarioRepository usuarios;
    @Autowired CuentaEstablecimientoRepository cuentasRepo;
    @Autowired ConsumidorRepository consumidores;
    @Autowired DocumentoEstablecimientoRepository documentos;
    @Autowired SolicitudAfiliacionRepository solicitudes;
    @Autowired AdministradorPlataformaRepository administradores;
    @Autowired AdministradorService admins;
    @Autowired CuentaLocalService cuentas;
    @Autowired OfertaService ofertas;
    @Autowired AlmacenDocumentoService archivos;
    @Autowired CodificadorContrasena codificador;

    @Test
    void laDemostracionCargaLocalesDocumentoAccesosYOfertas() {
        assertEquals(3, empresas.count());
        assertEquals(4, locales.count());
        assertEquals(4, documentos.count());
        assertEquals(3, cuentasRepo.count());
        assertEquals(2, consumidores.count());
        assertEquals(1, solicitudes.findByEstadoOrderByIdAsc(
                com.sazloop.afiliacion.domain.SolicitudAfiliacion.EstadoSolicitud.EN_REVISION).size());

        var sanBorja = locales.findAll().stream().filter(l -> l.getNombre().equals("Aurora San Borja"))
                .findFirst().orElseThrow();
        assertEquals(Establecimiento.EstadoEstablecimiento.PENDIENTE_APROBACION, sanBorja.getEstado());
        assertTrue(cuentasRepo.findByEstablecimiento_Id(sanBorja.getId()).isEmpty());
        assertEquals(3, locales.findAll().stream().filter(l ->
                l.getEstado() == Establecimiento.EstadoEstablecimiento.ACTIVO).count());
        assertEquals(Set.of("Panes del Día", "Merienda Sorpresa", "Almuerzo del Día"),
                ofertas.buscar(null, null, null, null).stream().map(o -> o.pack()).collect(Collectors.toSet()));

        assertEquals("demo.admin@sazloop.test",
                admins.iniciar("demo.admin@sazloop.test", "SazloopDemo2026!").getEmailInstitucional());
        assertNotNull(cuentas.iniciar(new CuentaLocalRequest("demo.miraflores", "SazloopDemo2026!")));
        assertNotNull(cuentas.iniciar(new CuentaLocalRequest("demo.ayllon1", "SazloopDemo2026!")));
        assertNotNull(cuentas.iniciar(new CuentaLocalRequest("demo.ayllon2", "SazloopDemo2026!")));
        for (String email : Set.of("demo.consumidor1@sazloop.test", "demo.consumidor2@sazloop.test")) {
            var usuario = usuarios.findByEmailIgnoreCase(email).orElseThrow();
            assertTrue(codificador.coincide("SazloopDemo2026!", usuario.getPasswordHash()));
        }
        assertEquals(1, administradores.count());
        documentos.findAll().forEach(d -> assertDoesNotThrow(() ->
                assertArrayEquals(new byte[] {'%', 'P', 'D', 'F'},
                        java.util.Arrays.copyOf(archivos.leer(d.getUrlArchivo()), 4))));
    }
}
