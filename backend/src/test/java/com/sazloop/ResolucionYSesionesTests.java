package com.sazloop;

import com.sazloop.afiliacion.controller.AdministradorSesionController;
import com.sazloop.afiliacion.domain.AdministradorPlataforma;
import com.sazloop.afiliacion.domain.DocumentoEstablecimiento.TipoDocumento;
import com.sazloop.afiliacion.domain.RevisionDocumento.ResultadoRevision;
import com.sazloop.afiliacion.domain.SolicitudAfiliacion.EstadoSolicitud;
import com.sazloop.afiliacion.dto.ResolucionRequest;
import com.sazloop.afiliacion.repository.AdministradorPlataformaRepository;
import com.sazloop.afiliacion.service.RevisionAfiliacionService;
import com.sazloop.afiliacion.service.SolicitudAfiliacionService;
import com.sazloop.identidad.controller.CuentaLocalController;
import com.sazloop.identidad.controller.SesionUsuarioController;
import com.sazloop.identidad.domain.CuentaEstablecimiento;
import com.sazloop.identidad.domain.Establecimiento.EstadoEstablecimiento;
import com.sazloop.identidad.domain.Usuario;
import com.sazloop.identidad.dto.CuentaLocalRequest;
import com.sazloop.identidad.dto.DireccionRequest;
import com.sazloop.identidad.dto.EmpresaRequest;
import com.sazloop.identidad.dto.EstablecimientoRequest;
import com.sazloop.identidad.dto.InicioSesionRequest;
import com.sazloop.identidad.dto.RegistroUsuarioRequest;
import com.sazloop.identidad.repository.EstablecimientoRepository;
import com.sazloop.identidad.repository.UsuarioRepository;
import com.sazloop.identidad.service.CodificadorContrasena;
import com.sazloop.identidad.service.CuentaLocalService;
import com.sazloop.identidad.service.EmpresaService;
import com.sazloop.identidad.service.IndicadoresEmpresaService;
import com.sazloop.identidad.service.RegistroUsuarioService;
import com.sazloop.ofertas.domain.VariantePack;
import com.sazloop.ofertas.dto.OfertaResponse;
import com.sazloop.ofertas.dto.OfertaRequest;
import com.sazloop.ofertas.dto.PackRequest;
import com.sazloop.ofertas.dto.PublicacionRequest;
import com.sazloop.ofertas.service.OfertaService;
import com.sazloop.ofertas.service.PanelLocalService;
import com.sazloop.reservas.domain.Reserva;
import com.sazloop.reservas.dto.CompraRequest;
import com.sazloop.reservas.dto.CompraResponse;
import com.sazloop.reservas.service.CompraRecojoService;
import com.sazloop.shared.SesionesPorRol;
import com.sazloop.transacciones.domain.Pago;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/** Resolución de solicitudes en un paso, entrega desde el pago, ganancias con comisión y sesiones simultáneas. */
@SpringBootTest
@TestPropertySource(properties = "sazloop.documentos.directorio=target/test-documentos")
class ResolucionYSesionesTests {
    @Autowired UsuarioRepository usuarios;
    @Autowired EmpresaService empresas;
    @Autowired SolicitudAfiliacionService solicitudes;
    @Autowired RevisionAfiliacionService revision;
    @Autowired AdministradorPlataformaRepository administradores;
    @Autowired CodificadorContrasena contrasenas;
    @Autowired CuentaLocalService cuentas;
    @Autowired OfertaService ofertas;
    @Autowired PanelLocalService panel;
    @Autowired CompraRecojoService compras;
    @Autowired EstablecimientoRepository establecimientos;
    @Autowired IndicadoresEmpresaService indicadores;
    @Autowired RegistroUsuarioService registro;
    @Autowired SesionUsuarioController sesionPersona;
    @Autowired AdministradorSesionController sesionAdministrador;
    @Autowired CuentaLocalController sesionLocal;

    private static final byte[] PDF = "%PDF-1.4\nPrueba".getBytes(StandardCharsets.UTF_8);

    @Test
    void unaSolaResolucionAprueba_oRechazaConElMotivoDeLasObservaciones() {
        AdministradorPlataforma admin = admin();
        Long propietario = usuario();
        Long local = localEnRevision(propietario, "Local resolución");
        var solicitud = solicitudes.consultar(propietario, local).orElseThrow();
        Long documento = solicitud.documentos().getFirst().id();

        assertThrows(ResponseStatusException.class, () -> revision.resolver(admin, solicitud.id(),
                new ResolucionRequest(List.of(new ResolucionRequest.Documento(documento, ResultadoRevision.RECHAZADO, " ")), null)));
        var rechazada = revision.resolver(admin, solicitud.id(), new ResolucionRequest(
                List.of(new ResolucionRequest.Documento(documento, ResultadoRevision.RECHAZADO, "Archivo ilegible")), null));
        assertEquals(EstadoSolicitud.RECHAZADA, rechazada.estado());
        assertTrue(rechazada.motivoDecision().contains("Archivo ilegible"));
        assertEquals(EstadoEstablecimiento.PENDIENTE_APROBACION, estado(local));

        // Con documentos nuevos el propietario presenta otra solicitud y la aprobación es de un solo paso.
        var segunda = solicitudes.adjuntar(propietario, local, TipoDocumento.LICENCIA_FUNCIONAMIENTO, "L-1",
                null, LocalDate.now().plusYears(1), archivo());
        solicitudes.enviar(propietario, local);
        var aprobada = revision.resolver(admin, segunda.id(), new ResolucionRequest(List.of(
                new ResolucionRequest.Documento(segunda.documentos().getFirst().id(), ResultadoRevision.APROBADO, null)), null));
        assertEquals(EstadoSolicitud.APROBADA, aprobada.estado());
        assertEquals(EstadoEstablecimiento.ACTIVO, estado(local));
        assertEquals(EstadoEstablecimiento.ACTIVO, aprobada.estadoLocal());
    }

    @Test
    void sePuedeEntregarDesdeElPago_yLaGananciaDescuentaLaComision() {
        AdministradorPlataforma admin = admin();
        Long propietario = usuario();
        Long local = localEnRevision(propietario, "Local entrega");
        var solicitud = solicitudes.consultar(propietario, local).orElseThrow();
        revision.resolver(admin, solicitud.id(), new ResolucionRequest(List.of(
                new ResolucionRequest.Documento(solicitud.documentos().getFirst().id(), ResultadoRevision.APROBADO, null)), null));
        CuentaEstablecimiento cuenta = cuentas.crear(propietario, local,
                new CuentaLocalRequest("entrega-" + UUID.randomUUID(), "clave-local"));

        // Un inicio cercano al momento actual es válido; uno de hace diez minutos, no.
        PackRequest pack = new PackRequest("Pack entrega", "Excedentes", "Pan", List.of(), List.of());
        var variante = new OfertaRequest.VarianteRequest(VariantePack.TamanioVariante.PEQUENO,
                new BigDecimal("20.00"), new BigDecimal("10.00"), 4);
        assertThrows(ResponseStatusException.class, () -> ofertas.crearYPublicar(cuenta, new PublicacionRequest(
                pack, LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusHours(2), List.of(variante))));
        OfertaResponse oferta = ofertas.crearYPublicar(cuenta, new PublicacionRequest(
                pack, LocalDateTime.now(), LocalDateTime.now().plusHours(2), List.of(variante)));

        Long consumidor = usuario();
        CompraResponse compra = compras.comprar(consumidor, new CompraRequest(oferta.id(), Pago.MedioPago.YAPE,
                List.of(new CompraRequest.Linea(oferta.variantes().getFirst().id(), 2))));
        assertEquals(Reserva.EstadoReserva.RECOGIDA, compras.recoger(cuenta, compra.codigoRecojo()).estado());

        var hoy = panel.resumen(cuenta).dia();
        assertEquals(new BigDecimal("20.00"), hoy.ventaBruta());
        assertEquals(new BigDecimal("2.00"), hoy.comision());
        assertEquals(new BigDecimal("18.00"), hoy.ganancia());
        assertEquals(2, hoy.packsEntregados());
        var total = indicadores.resumen(propietario).total();
        assertEquals(2, total.packsEntregados());
    }

    @Test
    void personaLocalYAdministradorPuedenEstarConectadosAlMismoTiempo() {
        String correo = UUID.randomUUID() + "@example.test";
        registro.registrar(new RegistroUsuarioRequest("Ana", "Soto", correo, "ClaveSegura1", null));
        String correoAdmin = UUID.randomUUID() + "@example.test";
        administradores.save(AdministradorPlataforma.builder().nombre("Revisor").emailInstitucional(correoAdmin)
                .passwordHash(contrasenas.codificar("ClaveAdmin1")).build());

        MockHttpSession sesion = new MockHttpSession();
        sesionPersona.iniciar(new InicioSesionRequest(correo, "ClaveSegura1"), peticion(sesion));
        sesionAdministrador.entrar(new AdministradorSesionController.Credenciales(correoAdmin, "ClaveAdmin1"), peticion(sesion));
        assertNotNull(sesion.getAttribute(SesionesPorRol.PERSONA));
        assertNotNull(sesion.getAttribute(SesionesPorRol.ADMINISTRADOR));

        sesionAdministrador.salir(peticion(sesion));
        assertNotNull(sesion.getAttribute(SesionesPorRol.PERSONA));
        assertNull(sesion.getAttribute(SesionesPorRol.ADMINISTRADOR));
        assertFalse(sesion.isInvalid());

        sesionPersona.cerrar(peticion(sesion));
        assertTrue(sesion.isInvalid());
    }

    private static MockHttpServletRequest peticion(MockHttpSession sesion) {
        MockHttpServletRequest peticion = new MockHttpServletRequest();
        peticion.setSession(sesion);
        return peticion;
    }

    private Long localEnRevision(Long propietario, String nombre) {
        String ruc = Long.toString(ThreadLocalRandom.current().nextLong(10_000_000_000L, 100_000_000_000L));
        empresas.registrarEmpresa(propietario, new EmpresaRequest("Empresa", "Comercio", ruc));
        Long local = empresas.registrarEstablecimiento(propietario, new EstablecimientoRequest(nombre, null, null,
                new DireccionRequest("Lima", "Lima", "Surquillo", "Calle Cinco", null, null, null))).id();
        solicitudes.adjuntar(propietario, local, TipoDocumento.CERTIFICADO_SANITARIO, "CS-9",
                null, LocalDate.now().plusYears(1), archivo());
        solicitudes.enviar(propietario, local);
        return local;
    }

    private EstadoEstablecimiento estado(Long localId) {
        return establecimientos.findById(localId).orElseThrow().getEstado();
    }

    private AdministradorPlataforma admin() {
        return administradores.save(AdministradorPlataforma.builder().nombre("Revisor")
                .emailInstitucional(UUID.randomUUID() + "@example.test")
                .passwordHash(contrasenas.codificar("clave-segura")).build());
    }

    private static MockMultipartFile archivo() {
        return new MockMultipartFile("archivo", "documento.pdf", "application/pdf", PDF);
    }

    private Long usuario() {
        return usuarios.save(Usuario.builder().nombres("Prueba").apellidos("Resolución")
                .email(UUID.randomUUID() + "@example.test").passwordHash("hash-temporal").build()).getId();
    }
}
