package com.sazloop;

import com.sazloop.afiliacion.domain.*;
import com.sazloop.afiliacion.dto.*;
import com.sazloop.afiliacion.repository.AdministradorPlataformaRepository;
import com.sazloop.afiliacion.service.*;
import com.sazloop.experiencia.dto.ResenaRequest;
import com.sazloop.experiencia.service.ResenaService;
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
import com.sazloop.reservas.service.CancelacionService;
import com.sazloop.reservas.repository.ReservaRepository;
import com.sazloop.transacciones.domain.Pago;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
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
    @Autowired ResenaService resenas;
    @Autowired CancelacionService cancelaciones;
    @Autowired ReservaRepository reservasRepo;
    @Autowired TransactionTemplate transacciones;
    @Autowired PerfilService perfiles;
    @Autowired com.sazloop.ofertas.service.PanelLocalService panelLocal;
    @Autowired com.sazloop.reservas.service.CompraPresencialService presencial;
    @Autowired com.sazloop.reservas.repository.RetencionStockRepository retencionesRepo;
    @Autowired com.sazloop.reservas.service.ReservaVencimientoService vencimiento;
    @Autowired com.sazloop.identidad.service.IndicadoresEmpresaService indicadores;

    @Test
    void perfilActualizaDatosConFotoYProtegeContrasena() {
        Long id = usuario();
        Long otro = usuario();
        String correoOcupado = usuarios.findById(otro).orElseThrow().getEmail();
        assertThrows(ResponseStatusException.class, () -> perfiles.actualizar(id,
                new PerfilRequest("Nuevo", "Nombre", correoOcupado, "999999999")));

        UsuarioResponse editado = perfiles.actualizar(id,
                new PerfilRequest("Nuevo", "Nombre", "perfil-" + UUID.randomUUID() + "@example.test", "999999999"));
        assertEquals("Nuevo", editado.nombres());
        assertEquals("999999999", editado.telefono());
        assertFalse(editado.tieneFoto());

        Usuario registrado = usuarios.findById(id).orElseThrow();
        registrado.setPasswordHash(contrasenas.codificar("clave-vieja"));
        usuarios.saveAndFlush(registrado);
        assertThrows(ResponseStatusException.class, () -> perfiles.cambiarContrasena(id,
                new CambioContrasenaRequest("equivocada", "nueva-clave")));
        perfiles.cambiarContrasena(id, new CambioContrasenaRequest("clave-vieja", "nueva-clave"));
        assertTrue(contrasenas.coincide("nueva-clave", usuarios.findById(id).orElseThrow().getPasswordHash()));
        assertThrows(ResponseStatusException.class, () -> perfiles.guardarFoto(id,
                new MockMultipartFile("archivo", "invalido.png", "image/png", "texto".getBytes(StandardCharsets.UTF_8))));
        byte[] png = new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0, 0};
        perfiles.guardarFoto(id, new MockMultipartFile("archivo", "foto.png", "image/png", png));
        assertArrayEquals(png, perfiles.foto(id).contenido());
        assertEquals("image/png", perfiles.foto(id).tipo());
    }

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
        PackRequest packNuevo = new PackRequest("Sorpresa", "Excedentes del día",
                "Pan y fruta", List.of("Trigo"), List.of("Contiene gluten"));
        LocalDateTime inicio = LocalDateTime.now().plusMinutes(1);
        PublicacionRequest publicacion = new PublicacionRequest(packNuevo,
                inicio, inicio.plusHours(1), List.of(
                new OfertaRequest.VarianteRequest(VariantePack.TamanioVariante.PEQUENO,
                        new BigDecimal("20.00"), new BigDecimal("10.00"), 2)));
        OfertaResponse publicada = ofertas.crearYPublicar(cuenta, publicacion);
        assertEquals("Sorpresa", ofertas.listarPacks(cuenta).getFirst().nombre());
        int cantidadPacks = ofertas.listarPacks(cuenta).size();
        assertThrows(ResponseStatusException.class, () -> ofertas.crearYPublicar(cuenta, publicacion));
        assertEquals(cantidadPacks, ofertas.listarPacks(cuenta).size());
        assertEquals(1, ofertas.buscar("Miraflores", "Calle", new BigDecimal("10.00"), null).size());
        assertEquals(List.of("Trigo"), ofertas.consultar(publicada.id()).alergenos());
        Long consumidor = usuario();
        CompraRequest pedido = new CompraRequest(publicada.id(), Pago.MedioPago.YAPE,
                List.of(new CompraRequest.Linea(publicada.variantes().getFirst().id(), 1)));
        CompraMultipleRequest lote = new CompraMultipleRequest(Pago.MedioPago.YAPE, List.of(
                new CompraMultipleRequest.Pedido(publicada.id(), pedido.lineas()),
                new CompraMultipleRequest.Pedido(publicada.id(), pedido.lineas())));
        List<CompraResponse> loteComprado = compras.comprarVarios(consumidor, lote);
        assertEquals(2, loteComprado.size());
        assertNotEquals(loteComprado.get(0).reservaId(), loteComprado.get(1).reservaId());
        assertNotEquals(loteComprado.get(0).codigoRecojo(), loteComprado.get(1).codigoRecojo());
        assertEquals(0, ofertas.consultar(publicada.id()).variantes().getFirst().stockDisponible());
        assertNotNull(panelLocal.resumen(cuenta).ofertaVigente());
        assertEquals(0, panelLocal.resumen(cuenta).stockDisponible());
        assertEquals(new BigDecimal("0"), panelLocal.resumen(cuenta).dia().ganancia());
        loteComprado.forEach(c -> cancelaciones.cancelar(consumidor, c.reservaId()));
        assertEquals(2, ofertas.consultar(publicada.id()).variantes().getFirst().stockDisponible());
        CompraResponse compra = compras.comprar(consumidor, pedido);
        assertEquals(new BigDecimal("10.00"), compra.total());
        assertEquals(new BigDecimal("1.00"), transacciones.execute(status -> reservasRepo
                .findById(compra.reservaId()).orElseThrow().getPago().getComision().getImporteComision()));
        assertEquals(Reserva.EstadoReserva.CONFIRMADA, compra.estado());
        assertTrue(compra.avisoBoleta().contains("sin validez tributaria"));
        assertEquals(1, ofertas.consultar(publicada.id()).variantes().getFirst().stockDisponible());
        int reservasAntes = compras.misCompras(consumidor).size();
        assertThrows(ResponseStatusException.class, () -> compras.comprarVarios(consumidor, lote));
        assertEquals(reservasAntes, compras.misCompras(consumidor).size());
        assertEquals(1, ofertas.consultar(publicada.id()).variantes().getFirst().stockDisponible());
        assertThrows(ResponseStatusException.class, () -> compras.comprar(consumidor,
                new CompraRequest(publicada.id(), Pago.MedioPago.TARJETA_DEBITO,
                        List.of(new CompraRequest.Linea(publicada.variantes().getFirst().id(), 2)))));
        assertEquals(compra.codigoRecojo(), compras.misCompras(consumidor).getFirst().codigoRecojo());
        assertEquals(compra.reservaId(), compras.consultarCodigo(cuenta, compra.codigoRecojo()).reservaId());
        assertFalse(compras.misCompras(consumidor).getFirst().resenaPendiente());
        assertThrows(ResponseStatusException.class, () -> resenas.registrar(consumidor,
                compra.reservaId(), new ResenaRequest(5, "Muy bueno")));
        // Simula el momento del recojo sin demorar el test ni alterar el stock.
        OfertaPack oferta = ofertasRepo.findById(publicada.id()).orElseThrow();
        oferta.setInicioRecojo(LocalDateTime.now().minusMinutes(1));
        ofertasRepo.save(oferta);
        assertEquals(Reserva.EstadoReserva.RECOGIDA,
                compras.recoger(cuenta, compra.codigoRecojo()).estado());
        assertThrows(ResponseStatusException.class, () -> compras.recoger(cuenta, compra.codigoRecojo()));
        assertTrue(compras.misCompras(consumidor).getFirst().resenaPendiente());
        assertEquals(new BigDecimal("9.00"), panelLocal.resumen(cuenta).dia().ganancia());
        assertEquals(new BigDecimal("9.00"), panelLocal.resumen(cuenta).semana().ganancia());
        assertEquals(new BigDecimal("9.00"), panelLocal.resumen(cuenta).mes().ganancia());
        assertThrows(ResponseStatusException.class, () -> resenas.registrar(propietario,
                compra.reservaId(), new ResenaRequest(5, "Ajena")));
        resenas.registrar(consumidor, compra.reservaId(), new ResenaRequest(5, null));
        assertFalse(compras.misCompras(consumidor).getFirst().resenaPendiente());
        assertEquals(5.0, ofertas.consultar(publicada.id()).calificacion());
        assertThrows(ResponseStatusException.class, () -> resenas.registrar(consumidor,
                compra.reservaId(), new ResenaRequest(1, "Otra")));
        assertThrows(ResponseStatusException.class, () -> cancelaciones.cancelar(consumidor, compra.reservaId()));
        CompraResponse segunda = compras.comprar(consumidor, pedido);
        assertEquals(0, ofertas.consultar(publicada.id()).variantes().getFirst().stockDisponible());
        assertTrue(compras.misCompras(consumidor).getFirst().cancelable());
        assertThrows(ResponseStatusException.class, () -> cancelaciones.cancelar(propietario, segunda.reservaId()));
        cancelaciones.cancelar(consumidor, segunda.reservaId());
        assertEquals(Reserva.EstadoReserva.CANCELADA, compras.misCompras(consumidor).getFirst().estado());
        assertEquals(new BigDecimal("5.00"), compras.misCompras(consumidor).getFirst().importeReembolso());
        assertEquals(new BigDecimal("0.50"), transacciones.execute(status -> reservasRepo
                .findById(segunda.reservaId()).orElseThrow().getPago().getComision().getImporteComision()));
        assertEquals(1, ofertas.consultar(publicada.id()).variantes().getFirst().stockDisponible());
        assertThrows(ResponseStatusException.class, () -> cancelaciones.cancelar(consumidor, segunda.reservaId()));
        assertThrows(ResponseStatusException.class, () -> resenas.omitir(consumidor, compra.reservaId()));

        Long otroConsumidor = usuario();
        CompraResponse tercera = compras.comprar(otroConsumidor, pedido);
        assertEquals(Reserva.EstadoReserva.RECOGIDA,
                compras.recoger(cuenta, tercera.codigoRecojo()).estado());
        assertTrue(compras.misCompras(otroConsumidor).getFirst().resenaPendiente());
        resenas.omitir(otroConsumidor, tercera.reservaId());
        assertFalse(compras.misCompras(otroConsumidor).getFirst().resenaPendiente());
        assertThrows(ResponseStatusException.class, () -> resenas.registrar(otroConsumidor,
                tercera.reservaId(), new ResenaRequest(4, null)));

        oferta = ofertasRepo.findById(publicada.id()).orElseThrow();
        oferta.setFinRecojo(LocalDateTime.now().minusSeconds(1));
        ofertasRepo.saveAndFlush(oferta);
        Long packPresencial = ofertas.crearPack(cuenta, packNuevo);
        OfertaResponse qrOferta = ofertas.publicar(cuenta, new OfertaRequest(packPresencial,
                LocalDateTime.now().plusMinutes(1), LocalDateTime.now().plusHours(1), List.of(
                new OfertaRequest.VarianteRequest(VariantePack.TamanioVariante.PEQUENO,
                        new BigDecimal("20.00"), new BigDecimal("10.00"), 3))));
        OfertaPack qrEntidad = ofertasRepo.findById(qrOferta.id()).orElseThrow();
        String token = qrEntidad.getTokenQr();
        qrEntidad.setInicioRecojo(LocalDateTime.now().minusMinutes(1));
        ofertasRepo.saveAndFlush(qrEntidad);
        CompraRequest pedidoQr = new CompraRequest(qrOferta.id(), Pago.MedioPago.YAPE,
                List.of(new CompraRequest.Linea(qrOferta.variantes().getFirst().id(), 1)));
        assertThrows(ResponseStatusException.class, () -> presencial.digital(consumidor,
                new CompraPresencialRequest("incorrecto", pedidoQr)));
        CompraResponse digital = presencial.digital(consumidor,
                new CompraPresencialRequest(token, pedidoQr));
        assertFalse(digital.cancelable());
        var cifrasAntes = indicadores.resumen(propietario).total();
        assertEquals(3, cifrasAntes.ventas());
        assertEquals(3, cifrasAntes.cancelaciones());
        assertEquals(2, cifrasAntes.packsEntregados());
        assertEquals(new BigDecimal("30.00"), cifrasAntes.importeVentas());
        assertEquals(Reserva.OrigenCompra.PRESENCIAL_QR,
                reservasRepo.findById(digital.reservaId()).orElseThrow().getOrigenCompra());

        CompraRequest pedidoEfectivo = new CompraRequest(qrOferta.id(), Pago.MedioPago.EFECTIVO,
                pedidoQr.lineas());
        RetencionResponse efectivo = presencial.reservarEfectivo(otroConsumidor,
                new CompraPresencialRequest(token, pedidoEfectivo));
        assertEquals(1, ofertas.consultar(qrOferta.id()).variantes().getFirst().stockDisponible());
        assertEquals(Reserva.EstadoReserva.PENDIENTE_PAGO,
                compras.consultarCodigo(cuenta, efectivo.codigoRecojo()).estado());
        assertEquals(Reserva.EstadoReserva.RECOGIDA,
                presencial.confirmarEfectivo(cuenta, efectivo.codigoRecojo()).estado());
        assertEquals(new BigDecimal("1.00"), transacciones.execute(status -> reservasRepo
                .findById(efectivo.reservaId()).orElseThrow().getPago().getComision().getImporteComision()));
        assertThrows(ResponseStatusException.class, () -> presencial.confirmarEfectivo(cuenta,
                efectivo.codigoRecojo()));

        RetencionResponse vencible = presencial.reservarEfectivo(usuario(),
                new CompraPresencialRequest(token, pedidoEfectivo));
        assertEquals(0, ofertas.consultar(qrOferta.id()).variantes().getFirst().stockDisponible());
        var hold = retencionesRepo.findByReserva_Id(vencible.reservaId()).getFirst();
        hold.setFechaExpiracion(LocalDateTime.now().minusSeconds(1));
        retencionesRepo.saveAndFlush(hold);
        presencial.vencerRetenciones();
        assertEquals(1, ofertas.consultar(qrOferta.id()).variantes().getFirst().stockDisponible());
        assertEquals(Reserva.EstadoReserva.EXPIRADA,
                compras.consultarCodigo(cuenta, vencible.codigoRecojo()).estado());
        qrEntidad = ofertasRepo.findById(qrOferta.id()).orElseThrow();
        qrEntidad.setFinRecojo(LocalDateTime.now().minusSeconds(1));
        ofertasRepo.saveAndFlush(qrEntidad);
        vencimiento.vencer();
        assertEquals(Reserva.EstadoReserva.EXPIRADA,
                compras.misCompras(consumidor).stream()
                        .filter(c -> c.reservaId().equals(digital.reservaId())).findFirst().orElseThrow().estado());
        assertEquals(Pago.EstadoPago.CONFIRMADO, transacciones.execute(status ->
                reservasRepo.findById(digital.reservaId()).orElseThrow().getPago().getEstado()));
        var cifrasDespues = indicadores.resumen(propietario).total();
        assertEquals(4, cifrasDespues.ventas());
        assertEquals(1, cifrasDespues.noRecogidas());
        assertEquals(3, cifrasDespues.packsEntregados());
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
