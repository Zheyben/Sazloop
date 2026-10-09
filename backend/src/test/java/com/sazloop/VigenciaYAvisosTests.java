package com.sazloop;

import com.sazloop.afiliacion.domain.AdministradorPlataforma;
import com.sazloop.afiliacion.domain.DocumentoEstablecimiento.TipoDocumento;
import com.sazloop.afiliacion.domain.EvaluacionAfiliacion;
import com.sazloop.afiliacion.domain.RevisionDocumento;
import com.sazloop.afiliacion.dto.DecisionRequest;
import com.sazloop.afiliacion.dto.RevisionRequest;
import com.sazloop.afiliacion.repository.AdministradorPlataformaRepository;
import com.sazloop.afiliacion.service.RevisionAfiliacionService;
import com.sazloop.afiliacion.service.SolicitudAfiliacionService;
import com.sazloop.afiliacion.service.VigenciaDocumentosService;
import com.sazloop.experiencia.domain.Notificacion.TipoNotificacion;
import com.sazloop.experiencia.repository.ImpactoCO2EvitadoRepository;
import com.sazloop.experiencia.service.NotificacionService;
import com.sazloop.identidad.domain.CuentaEstablecimiento;
import com.sazloop.identidad.domain.Establecimiento.EstadoEstablecimiento;
import com.sazloop.identidad.domain.Usuario;
import com.sazloop.identidad.dto.CuentaLocalRequest;
import com.sazloop.identidad.dto.DireccionRequest;
import com.sazloop.identidad.dto.EmpresaRequest;
import com.sazloop.identidad.dto.EstablecimientoRequest;
import com.sazloop.identidad.repository.EstablecimientoRepository;
import com.sazloop.identidad.repository.UsuarioRepository;
import com.sazloop.identidad.service.CodificadorContrasena;
import com.sazloop.identidad.service.CuentaLocalService;
import com.sazloop.identidad.service.EmpresaService;
import com.sazloop.ofertas.domain.OfertaPack;
import com.sazloop.ofertas.domain.VariantePack;
import com.sazloop.ofertas.dto.OfertaRequest;
import com.sazloop.ofertas.dto.OfertaResponse;
import com.sazloop.ofertas.dto.PackRequest;
import com.sazloop.ofertas.dto.PublicacionRequest;
import com.sazloop.ofertas.repository.OfertaPackRepository;
import com.sazloop.ofertas.service.OfertaService;
import com.sazloop.ofertas.service.PanelLocalService;
import com.sazloop.reservas.domain.Reserva;
import com.sazloop.reservas.dto.CompraRequest;
import com.sazloop.reservas.dto.CompraResponse;
import com.sazloop.reservas.service.CancelacionService;
import com.sazloop.reservas.service.CompraRecojoService;
import com.sazloop.transacciones.domain.Pago;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

/** Suspensión por documento vencido, renovación, avisos (RF-09, RF-25) e impacto de cada entrega (RF-26). */
@SpringBootTest
@TestPropertySource(properties = "sazloop.documentos.directorio=target/test-documentos")
class VigenciaYAvisosTests {
    @Autowired UsuarioRepository usuarios;
    @Autowired EmpresaService empresas;
    @Autowired SolicitudAfiliacionService solicitudes;
    @Autowired RevisionAfiliacionService revision;
    @Autowired AdministradorPlataformaRepository administradores;
    @Autowired CodificadorContrasena contrasenas;
    @Autowired CuentaLocalService cuentas;
    @Autowired OfertaService ofertas;
    @Autowired OfertaPackRepository ofertasRepo;
    @Autowired PanelLocalService panel;
    @Autowired CompraRecojoService compras;
    @Autowired CancelacionService cancelaciones;
    @Autowired VigenciaDocumentosService vigencia;
    @Autowired NotificacionService notificaciones;
    @Autowired EstablecimientoRepository establecimientos;
    @Autowired ImpactoCO2EvitadoRepository impactos;

    private static final byte[] PDF = "%PDF-1.4\nPrueba".getBytes(StandardCharsets.UTF_8);

    @Test
    void documentoVencidoSuspendeYLaRenovacionReactivaSinPerderReservasPagadas() {
        LocalDate hoy = VigenciaDocumentosService.hoy();
        Long propietario = usuario();
        String ruc = Long.toString(ThreadLocalRandom.current().nextLong(10_000_000_000L, 100_000_000_000L));
        empresas.registrarEmpresa(propietario, new EmpresaRequest("Empresa", "Comercio", ruc));
        Long local = empresas.registrarEstablecimiento(propietario, new EstablecimientoRequest("Local vigencia",
                null, null, new DireccionRequest("Lima", "Lima", "Barranco", "Calle Tres", null, null, null))).id();
        var solicitud = solicitudes.adjuntar(propietario, local, TipoDocumento.CERTIFICADO_SANITARIO, "CS-1",
                hoy.minusYears(1), hoy.plusDays(3), archivo());
        solicitudes.enviar(propietario, local);
        AdministradorPlataforma admin = admin();
        revision.revisar(admin, solicitud.documentos().getFirst().id(),
                new RevisionRequest(RevisionDocumento.ResultadoRevision.APROBADO, "Legible"));
        revision.decidir(admin, solicitud.id(), new DecisionRequest(
                EvaluacionAfiliacion.DecisionAfiliacion.APROBADA, "Documentación completa"));
        assertEquals(1, avisos(propietario, TipoNotificacion.RESULTADO_AFILIACION));

        CuentaEstablecimiento cuenta = cuentas.crear(propietario, local,
                new CuentaLocalRequest("vigencia-" + UUID.randomUUID(), "clave-local"));
        OfertaResponse oferta = ofertas.crearYPublicar(cuenta, new PublicacionRequest(
                new PackRequest("Pack vigencia", "Excedentes", "Pan", List.of(), List.of()),
                LocalDateTime.now().plusMinutes(1), LocalDateTime.now().plusHours(2), List.of(
                new OfertaRequest.VarianteRequest(VariantePack.TamanioVariante.MEDIANO,
                        new BigDecimal("20.00"), new BigDecimal("8.00"), 3))));
        Long consumidor = usuario();
        CompraRequest pedido = new CompraRequest(oferta.id(), Pago.MedioPago.YAPE,
                List.of(new CompraRequest.Linea(oferta.variantes().getFirst().id(), 1)));
        CompraResponse pagada = compras.comprar(consumidor, pedido);
        assertEquals(1, avisos(consumidor, TipoNotificacion.RECORDATORIO_RECOJO));
        assertEquals(VariantePack.TamanioVariante.MEDIANO, pagada.lineas().getFirst().tamanio());
        assertEquals("Prueba A.", pagada.cliente());

        // Siete días antes del vencimiento se avisa una sola vez.
        vigencia.revisar(hoy);
        vigencia.revisar(hoy);
        assertEquals(1, avisos(propietario, TipoNotificacion.DOCUMENTO_POR_VENCER));
        assertEquals(EstadoEstablecimiento.ACTIVO, estado(local));

        // Al día siguiente del vencimiento el local queda suspendido y deja de vender.
        vigencia.revisar(hoy.plusDays(4));
        assertEquals(EstadoEstablecimiento.SUSPENDIDO, estado(local));
        assertEquals(1, avisos(propietario, TipoNotificacion.SUSPENSION));
        assertTrue(vigencia.suspensionActiva(local).orElseThrow().getMotivo().contains("CS-1"));
        assertTrue(ofertas.buscar("Barranco", null, null, null).isEmpty());
        assertThrows(ResponseStatusException.class, () -> compras.comprar(consumidor, pedido));
        Long packId = ofertas.listarPacks(cuenta).getFirst().id();
        assertThrows(ResponseStatusException.class, () -> ofertas.publicar(cuenta, new OfertaRequest(
                packId, LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(4),
                List.of(new OfertaRequest.VarianteRequest(VariantePack.TamanioVariante.GRANDE,
                        new BigDecimal("30.00"), new BigDecimal("12.00"), 1)))));

        // El trabajador conserva su acceso y entrega la reserva ya pagada.
        assertEquals(cuenta.getId(), cuentas.iniciar(new CuentaLocalRequest(
                cuenta.getNombreUsuario(), "clave-local")).getId());
        CuentaEstablecimiento suspendida = cuentas.obtener(cuenta.getId());
        assertEquals(EstadoEstablecimiento.SUSPENDIDO, panel.resumen(suspendida).estadoLocal());
        OfertaPack entidad = ofertasRepo.findById(oferta.id()).orElseThrow();
        entidad.setInicioRecojo(LocalDateTime.now().minusMinutes(1));
        ofertasRepo.saveAndFlush(entidad);
        long impactosAntes = impactos.count();
        assertEquals(Reserva.EstadoReserva.RECOGIDA, compras.recoger(suspendida, pagada.codigoRecojo()).estado());
        assertEquals(impactosAntes + 1, impactos.count());

        // La renovación solo acepta documentos vencidos o por vencer y reactiva el local al aprobarse.
        assertThrows(ResponseStatusException.class, () -> solicitudes.adjuntar(propietario, local,
                TipoDocumento.INSPECCION_SEGURIDAD, null, null, null, archivo()));
        var renovacion = solicitudes.adjuntar(propietario, local, TipoDocumento.CERTIFICADO_SANITARIO, "CS-2",
                hoy, hoy.plusYears(1), archivo());
        assertTrue(renovacion.renovacion());
        assertNotEquals(solicitud.id(), renovacion.id());
        solicitudes.enviar(propietario, local);
        revision.revisar(admin, renovacion.documentos().getFirst().id(),
                new RevisionRequest(RevisionDocumento.ResultadoRevision.APROBADO, "Vigente"));
        assertTrue(revision.consultar(renovacion.id()).renovacion());
        revision.decidir(admin, renovacion.id(), new DecisionRequest(
                EvaluacionAfiliacion.DecisionAfiliacion.APROBADA, "Renovación conforme"));
        assertEquals(EstadoEstablecimiento.ACTIVO, estado(local));
        assertTrue(vigencia.suspensionActiva(local).isEmpty());
        assertEquals(2, avisos(propietario, TipoNotificacion.RESULTADO_AFILIACION));

        // El documento renovado reemplaza al vencido: la revisión ya no vuelve a suspender.
        vigencia.revisar(hoy.plusDays(4));
        assertEquals(EstadoEstablecimiento.ACTIVO, estado(local));
        assertEquals("CS-2", solicitudes.documentacion(propietario, local).documentos().getFirst().numero());
    }

    @Test
    void cancelarAvisaAlConsumidorConElReembolso() {
        LocalDate hoy = VigenciaDocumentosService.hoy();
        Long propietario = usuario();
        String ruc = Long.toString(ThreadLocalRandom.current().nextLong(10_000_000_000L, 100_000_000_000L));
        empresas.registrarEmpresa(propietario, new EmpresaRequest("Empresa", "Comercio", ruc));
        Long local = empresas.registrarEstablecimiento(propietario, new EstablecimientoRequest("Local avisos",
                null, null, new DireccionRequest("Lima", "Lima", "Lince", "Calle Cuatro", null, null, null))).id();
        var solicitud = solicitudes.adjuntar(propietario, local, TipoDocumento.LICENCIA_FUNCIONAMIENTO, null,
                null, hoy.plusYears(2), archivo());
        solicitudes.enviar(propietario, local);
        AdministradorPlataforma admin = admin();
        revision.revisar(admin, solicitud.documentos().getFirst().id(),
                new RevisionRequest(RevisionDocumento.ResultadoRevision.APROBADO, "Legible"));
        revision.decidir(admin, solicitud.id(), new DecisionRequest(
                EvaluacionAfiliacion.DecisionAfiliacion.APROBADA, "Conforme"));
        CuentaEstablecimiento cuenta = cuentas.crear(propietario, local,
                new CuentaLocalRequest("avisos-" + UUID.randomUUID(), "clave-local"));
        OfertaResponse oferta = ofertas.crearYPublicar(cuenta, new PublicacionRequest(
                new PackRequest("Pack avisos", "Excedentes", "Fruta", List.of(), List.of()),
                LocalDateTime.now().plusMinutes(1), LocalDateTime.now().plusHours(2), List.of(
                new OfertaRequest.VarianteRequest(VariantePack.TamanioVariante.PEQUENO,
                        new BigDecimal("10.00"), new BigDecimal("6.00"), 2))));
        Long consumidor = usuario();
        CompraResponse compra = compras.comprar(consumidor, new CompraRequest(oferta.id(), Pago.MedioPago.TARJETA_DEBITO,
                List.of(new CompraRequest.Linea(oferta.variantes().getFirst().id(), 1))));
        cancelaciones.cancelar(consumidor, compra.reservaId());
        var aviso = notificaciones.listar(consumidor).stream()
                .filter(a -> a.tipo() == TipoNotificacion.CAMBIO_RESERVA).findFirst().orElseThrow();
        assertTrue(aviso.mensaje().contains("3.00"));
        notificaciones.leerTodas(consumidor);
        assertTrue(notificaciones.listar(consumidor).stream().allMatch(NotificacionService.Aviso::leida));
    }

    private long avisos(Long usuarioId, TipoNotificacion tipo) {
        return notificaciones.listar(usuarioId).stream().filter(a -> a.tipo() == tipo).count();
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
        return usuarios.save(Usuario.builder().nombres("Prueba").apellidos("Avisos")
                .email(UUID.randomUUID() + "@example.test").passwordHash("hash-temporal").build()).getId();
    }
}
