package com.sazloop;

import com.sazloop.afiliacion.domain.*;
import com.sazloop.afiliacion.dto.*;
import com.sazloop.afiliacion.repository.AdministradorPlataformaRepository;
import com.sazloop.afiliacion.service.*;
import com.sazloop.identidad.domain.*;
import com.sazloop.identidad.dto.*;
import com.sazloop.identidad.repository.*;
import com.sazloop.identidad.service.*;
import com.sazloop.ofertas.domain.*;
import com.sazloop.ofertas.dto.*;
import com.sazloop.ofertas.repository.OfertaPackRepository;
import com.sazloop.ofertas.service.OfertaService;
import com.sazloop.reservas.domain.Reserva;
import com.sazloop.reservas.dto.*;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import static org.junit.jupiter.api.Assertions.*;

/** Recorre la afiliación, oferta, compra y entrega con base de datos real en memoria. */
@SpringBootTest
@TestPropertySource(properties = "sazloop.documentos.directorio=target/test-documentos")
class FlujoAfp2Tests {
    @Autowired UsuarioRepository usuarios;
    @Autowired EmpresaService empresas;
    @Autowired SolicitudAfiliacionService solicitudes;
    @Autowired RevisionAfiliacionService revision;
    @Autowired AdministradorPlataformaRepository administradores;
    @Autowired CodificadorContrasena contrasenas;
    @Autowired CuentaLocalService cuentas;
    @Autowired OfertaService ofertas;
    @Autowired OfertaPackRepository ofertasRepo;
    @Autowired CompraRecojoService compras;

    @Test
    void desdeDocumentosHastaRecojoSinSobreventaNiSegundoUsoDelCodigo() {
        Long propietario = usuario();
        String ruc = Long.toString(ThreadLocalRandom.current().nextLong(10_000_000_000L, 100_000_000_000L));
        empresas.registrarEmpresa(propietario, new EmpresaRequest("Empresa", "Comercio", ruc));
        Long local = empresas.registrarEstablecimiento(propietario,
                new EstablecimientoRequest("Local", null, null,
                        new DireccionRequest("Lima", "Lima", "Miraflores", "Calle Uno", null, null, null))).id();
        byte[] pdf = "%PDF-1.4\nPrueba".getBytes(StandardCharsets.UTF_8);
        var subida = solicitudes.adjuntar(propietario, local,
                DocumentoEstablecimiento.TipoDocumento.LICENCIA_FUNCIONAMIENTO, "ABC", null, null,
                new MockMultipartFile("archivo", "prueba.pdf", "application/pdf", pdf));
        assertEquals(SolicitudAfiliacion.EstadoSolicitud.EN_REVISION,
                solicitudes.enviar(propietario, local).estado());
        AdministradorPlataforma admin = administradores.save(AdministradorPlataforma.builder()
                .nombre("Revisor").emailInstitucional(UUID.randomUUID() + "@example.test")
                .passwordHash(contrasenas.codificar("clave-segura")).build());
        assertEquals(1, revision.consultar(subida.id()).documentos().size());
        revision.revisar(admin, subida.documentos().getFirst().id(),
                new RevisionRequest(RevisionDocumento.ResultadoRevision.APROBADO, "Documento legible",
                        DocumentoEstablecimiento.TipoDocumento.LICENCIA_FUNCIONAMIENTO,
                        "ABC verificado", null, null));
        assertEquals("ABC verificado", revision.consultar(subida.id()).documentos().getFirst().numero());
        revision.decidir(admin, subida.id(), new DecisionRequest(
                EvaluacionAfiliacion.DecisionAfiliacion.APROBADA, "Documentación revisada"));
        assertEquals("Documentación revisada", solicitudes.consultar(propietario, local)
                .orElseThrow().motivoDecision());

        CuentaEstablecimiento cuenta = cuentas.crear(propietario, local,
                new CuentaLocalRequest("local-" + UUID.randomUUID(), "clave-local"));
        assertEquals(cuenta.getId(), cuentas.iniciar(new CuentaLocalRequest(
                cuenta.getNombreUsuario(), "clave-local")).getId());
        Long pack = ofertas.crearPack(cuenta, new PackRequest("Sorpresa", "Excedentes del día",
                "Pan y fruta", List.of("Trigo"), List.of("Contiene gluten")));
        LocalDateTime inicio = LocalDateTime.now().plusMinutes(1);
        OfertaResponse publicada = ofertas.publicar(cuenta, new OfertaRequest(pack,
                inicio, inicio.plusHours(1), inicio.plusHours(2), List.of(
                new OfertaRequest.VarianteRequest(VariantePack.TamanioVariante.PEQUENO,
                        new BigDecimal("20.00"), new BigDecimal("10.00"), 2))));
        assertEquals(1, ofertas.buscar("Miraflores", "Calle", new BigDecimal("10.00"), null).size());
        assertEquals(List.of("Trigo"), ofertas.consultar(publicada.id()).alergenos());
        Long consumidor = usuario();
        CompraRequest pedido = new CompraRequest(publicada.id(), Pago.MedioPago.YAPE,
                List.of(new CompraRequest.Linea(publicada.variantes().getFirst().id(), 1)));
        CompraResponse compra = compras.comprar(consumidor, pedido);
        assertEquals(new BigDecimal("10.00"), compra.total());
        assertEquals(Reserva.EstadoReserva.CONFIRMADA, compra.estado());
        assertTrue(compra.avisoBoleta().contains("sin validez tributaria"));
        assertEquals(1, ofertas.consultar(publicada.id()).variantes().getFirst().stockDisponible());
        assertThrows(ResponseStatusException.class, () -> compras.comprar(consumidor,
                new CompraRequest(publicada.id(), Pago.MedioPago.TARJETA_DEBITO,
                        List.of(new CompraRequest.Linea(publicada.variantes().getFirst().id(), 2)))));
        assertEquals(compra.codigoRecojo(), compras.misCompras(consumidor).getFirst().codigoRecojo());
        assertEquals(compra.reservaId(), compras.consultarCodigo(cuenta, compra.codigoRecojo()).reservaId());
        // Simula el momento del recojo sin demorar el test ni alterar el stock.
        OfertaPack oferta = ofertasRepo.findById(publicada.id()).orElseThrow();
        oferta.setInicioRecojo(LocalDateTime.now().minusMinutes(1));
        ofertasRepo.save(oferta);
        assertEquals(Reserva.EstadoReserva.RECOGIDA,
                compras.recoger(cuenta, compra.codigoRecojo()).estado());
        assertThrows(ResponseStatusException.class, () -> compras.recoger(cuenta, compra.codigoRecojo()));
    }

    @Test
    void rechazoConMotivoPermiteNuevaSolicitudPeroNoCuentaOperativa() {
        Long propietario = usuario();
        String ruc = Long.toString(ThreadLocalRandom.current().nextLong(10_000_000_000L, 100_000_000_000L));
        empresas.registrarEmpresa(propietario, new EmpresaRequest("Empresa", "Comercio", ruc));
        Long local = empresas.registrarEstablecimiento(propietario,
                new EstablecimientoRequest("Local", null, null,
                        new DireccionRequest("Lima", "Lima", "Surco", "Calle Dos", null, null, null))).id();
        MockMultipartFile archivo = new MockMultipartFile("archivo", "prueba.pdf", "application/pdf",
                "%PDF-1.4\nPrueba".getBytes(StandardCharsets.UTF_8));
        var primera = solicitudes.adjuntar(propietario, local,
                DocumentoEstablecimiento.TipoDocumento.OTRO, null, null, null, archivo);
        solicitudes.enviar(propietario, local);
        AdministradorPlataforma admin = administradores.save(AdministradorPlataforma.builder()
                .nombre("Revisor").emailInstitucional(UUID.randomUUID() + "@example.test")
                .passwordHash(contrasenas.codificar("clave-segura")).build());
        revision.revisar(admin, primera.documentos().getFirst().id(),
                new RevisionRequest(RevisionDocumento.ResultadoRevision.RECHAZADO, "Archivo incompleto"));
        revision.decidir(admin, primera.id(), new DecisionRequest(
                EvaluacionAfiliacion.DecisionAfiliacion.RECHAZADA, "Debe adjuntar una versión completa"));
        assertEquals("Debe adjuntar una versión completa", solicitudes.consultar(propietario, local)
                .orElseThrow().motivoDecision());
        assertThrows(ResponseStatusException.class, () -> cuentas.crear(propietario, local,
                new CuentaLocalRequest("sin-aprobacion", "clave-local")));
        var segunda = solicitudes.adjuntar(propietario, local,
                DocumentoEstablecimiento.TipoDocumento.OTRO, null, null, null, archivo);
        assertNotEquals(primera.id(), segunda.id());
    }

    private Long usuario() {
        return usuarios.save(Usuario.builder().nombres("Prueba").apellidos("APF2")
                .email(UUID.randomUUID() + "@example.test").passwordHash("hash-temporal").build()).getId();
    }
}
