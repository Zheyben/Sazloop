package com.sazloop.reservas.service;

import com.sazloop.identidad.domain.*;
import com.sazloop.experiencia.domain.ImpactoCO2Evitado;
import com.sazloop.experiencia.repository.ImpactoCO2EvitadoRepository;
import com.sazloop.identidad.repository.*;
import com.sazloop.ofertas.domain.*;
import com.sazloop.ofertas.repository.OfertaPackRepository;
import com.sazloop.reservas.domain.*;
import com.sazloop.reservas.dto.*;
import com.sazloop.reservas.repository.*;
import com.sazloop.transacciones.domain.*;
import com.sazloop.transacciones.service.ComisionService;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CompraPresencialService {
    private final CompraRecojoService compras;
    private final OfertaPackRepository ofertas;
    private final ReservaRepository reservas;
    private final RetencionStockRepository retenciones;
    private final CodigoRecojoRepository codigos;
    private final UsuarioRepository usuarios;
    private final ConsumidorRepository consumidores;
    private final ImpactoCO2EvitadoRepository impactos;
    private final AvisosReservaService avisos;

    public CompraPresencialService(CompraRecojoService compras, OfertaPackRepository ofertas,
            ReservaRepository reservas, RetencionStockRepository retenciones,
            CodigoRecojoRepository codigos, UsuarioRepository usuarios,
            ConsumidorRepository consumidores, ImpactoCO2EvitadoRepository impactos,
            AvisosReservaService avisos) {
        this.compras = compras; this.ofertas = ofertas; this.reservas = reservas;
        this.retenciones = retenciones; this.codigos = codigos;
        this.usuarios = usuarios; this.consumidores = consumidores;
        this.impactos = impactos; this.avisos = avisos;
    }

    @Transactional
    public CompraResponse digital(Long usuarioId, CompraPresencialRequest solicitud) {
        CompraRequest entrada = solicitud.compra();
        validarToken(entrada.ofertaId(), solicitud.token());
        if (entrada.medioPago() == Pago.MedioPago.EFECTIVO)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usa la reserva en efectivo");
        CompraResponse creada = compras.comprar(usuarioId, entrada);
        Reserva reserva = reservas.findById(creada.reservaId()).orElseThrow();
        reserva.setOrigenCompra(Reserva.OrigenCompra.PRESENCIAL_QR);
        reserva.getPago().setContexto(Pago.ContextoPago.PRESENCIAL_QR);
        return compras.misCompras(usuarioId).stream()
                .filter(compra -> compra.reservaId().equals(creada.reservaId())).findFirst().orElseThrow();
    }

    @Transactional
    public RetencionResponse reservarEfectivo(Long usuarioId, CompraPresencialRequest solicitud) {
        CompraRequest entrada = solicitud.compra();
        if (entrada.medioPago() != Pago.MedioPago.EFECTIVO)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Se requiere efectivo");
        OfertaPack oferta = ofertas.bloquear(entrada.ofertaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        LocalDateTime ahora = LocalDateTime.now();
        if (oferta.getTokenQr() == null || !oferta.getTokenQr().equals(solicitud.token()) ||
                ahora.isBefore(oferta.getInicioRecojo()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "QR no válido para la compra presencial");
        if (oferta.getEstado() != OfertaPack.EstadoOferta.ACTIVA ||
                oferta.getPackSorpresa().getEstablecimiento().getEstado()
                    != Establecimiento.EstadoEstablecimiento.ACTIVO || !ahora.isBefore(oferta.getFinRecojo()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Oferta no disponible");
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (CompraRequest.Linea linea : entrada.lineas()) {
            if (linea.cantidad() < 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            cantidades.merge(linea.varianteId(), linea.cantidad(), (a, b) -> {
                try { return Math.addExact(a, b); }
                catch (ArithmeticException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST); }
            });
        }
        Map<Long, VariantePack> disponibles = new HashMap<>();
        oferta.getVariantes().forEach(v -> disponibles.put(v.getId(), v));
        BigDecimal total = BigDecimal.ZERO;
        for (var linea : cantidades.entrySet()) {
            VariantePack v = disponibles.get(linea.getKey());
            if (v == null || !v.isActiva() || v.getStockDisponible() < linea.getValue())
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente");
            total = total.add(v.getPrecioDescuento().multiply(BigDecimal.valueOf(linea.getValue())));
        }
        Usuario usuario = usuarios.findById(usuarioId).filter(Usuario::isActivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        Consumidor consumidor = consumidores.findByUsuario_Id(usuarioId)
                .orElseGet(() -> consumidores.save(Consumidor.builder().usuario(usuario).build()));
        if (!consumidor.isActivo()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        LocalDateTime vence = ahora.plusMinutes(10);
        if (vence.isAfter(oferta.getFinRecojo())) vence = oferta.getFinRecojo();
        Reserva reserva = Reserva.builder().consumidor(consumidor).oferta(oferta)
                .origenCompra(Reserva.OrigenCompra.PRESENCIAL_QR).montoTotal(total)
                .estado(Reserva.EstadoReserva.PENDIENTE_PAGO).build();
        for (var linea : cantidades.entrySet()) {
            VariantePack v = disponibles.get(linea.getKey());
            v.setStockDisponible(v.getStockDisponible() - linea.getValue());
            reserva.getDetalles().add(DetalleReserva.builder().reserva(reserva).variante(v)
                    .cantidad(linea.getValue()).precioUnitario(v.getPrecioDescuento())
                    .subtotal(v.getPrecioDescuento().multiply(BigDecimal.valueOf(linea.getValue()))).build());
        }
        reserva.setPago(Pago.builder().reserva(reserva).importe(total)
                .medioPago(Pago.MedioPago.EFECTIVO).contexto(Pago.ContextoPago.PRESENCIAL_QR)
                .estado(Pago.EstadoPago.PENDIENTE).build());
        reserva.setCodigoRecojo(CodigoRecojo.builder().reserva(reserva)
                .codigo(UUID.randomUUID().toString().substring(0, 16).toUpperCase(Locale.ROOT)).build());
        Reserva guardada = reservas.saveAndFlush(reserva);
        for (var linea : cantidades.entrySet()) {
            retenciones.save(RetencionStock.builder().reserva(guardada)
                    .variante(disponibles.get(linea.getKey())).consumidor(consumidor)
                    .cantidad(linea.getValue()).fechaCreacion(ahora).fechaExpiracion(vence)
                    .motivo(RetencionStock.MotivoRetencion.EFECTIVO_PRESENCIAL).build());
        }
        return new RetencionResponse(guardada.getId(), guardada.getCodigoRecojo().getCodigo(), total, vence);
    }

    @Transactional
    public CompraResponse confirmarEfectivo(CuentaEstablecimiento cuenta, String codigo) {
        Reserva reserva = reservas.findById(codigoReserva(codigo))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!reserva.getOferta().getPackSorpresa().getEstablecimiento().getId()
                .equals(cuenta.getEstablecimiento().getId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        reserva = reservas.bloquear(reserva.getId()).orElseThrow();
        if (reserva.getEstado() != Reserva.EstadoReserva.PENDIENTE_PAGO ||
                reserva.getPago().getMedioPago() != Pago.MedioPago.EFECTIVO)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No hay efectivo pendiente");
        LocalDateTime ahora = LocalDateTime.now();
        List<RetencionStock> holds = retenciones.findByReserva_Id(reserva.getId());
        if (holds.isEmpty() || !ahora.isBefore(holds.getFirst().getFechaExpiracion()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La retención venció");
        reserva.setEstado(Reserva.EstadoReserva.RECOGIDA);
        reserva.setFechaConfirmacion(ahora);
        reserva.getPago().setEstado(Pago.EstadoPago.CONFIRMADO);
        reserva.getPago().setFechaConfirmacion(ahora);
        reserva.getCodigoRecojo().setEstado(CodigoRecojo.EstadoCodigo.UTILIZADO);
        reserva.getCodigoRecojo().setFechaUso(ahora);
        reserva.setBoleta(BoletaSimulada.builder().reserva(reserva).total(reserva.getMontoTotal())
                .numero("SIM-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase(Locale.ROOT)).build());
        reserva.setRecojo(Recojo.builder().reserva(reserva).confirmadoPor(cuenta).build());
        ComisionService.recalcular(reserva.getPago(), reserva.getMontoTotal());
        holds.forEach(hold -> hold.setEstado(RetencionStock.EstadoRetencion.CONFIRMADA));
        reservas.saveAndFlush(reserva);
        impactos.save(ImpactoCO2Evitado.builder().recojo(reserva.getRecojo()).build());
        return compras.consultarCodigo(cuenta, codigo);
    }

    private Long codigoReserva(String codigo) {
        return codigos.findByCodigo(codigo.trim().toUpperCase(Locale.ROOT))
                .map(registro -> registro.getReserva().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void validarToken(Long ofertaId, String token) {
        OfertaPack oferta = ofertas.findById(ofertaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (oferta.getTokenQr() == null || !oferta.getTokenQr().equals(token))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "QR no válido");
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void vencerRetenciones() {
        LocalDateTime ahora = LocalDateTime.now();
        retenciones.findByEstadoAndFechaExpiracionLessThanEqual(
                RetencionStock.EstadoRetencion.ACTIVA, ahora).stream()
                .map(hold -> hold.getReserva().getId()).distinct()
                .forEach(id -> {
                    Reserva reserva = reservas.bloquear(id).orElseThrow();
                    if (reserva.getEstado() == Reserva.EstadoReserva.PENDIENTE_PAGO)
                        liberar(reserva, retenciones.findByReserva_Id(id));
                });
    }

    private void liberar(Reserva reserva, List<RetencionStock> holds) {
        if (reserva.getEstado() != Reserva.EstadoReserva.PENDIENTE_PAGO) return;
        OfertaPack oferta = ofertas.bloquear(reserva.getOferta().getId()).orElseThrow();
        for (RetencionStock hold : holds) {
            if (hold.getEstado() != RetencionStock.EstadoRetencion.ACTIVA) continue;
            VariantePack v = oferta.getVariantes().stream()
                    .filter(variante -> variante.getId().equals(hold.getVariante().getId()))
                    .findFirst().orElseThrow();
            v.setStockDisponible(v.getStockDisponible() + hold.getCantidad());
            hold.setEstado(RetencionStock.EstadoRetencion.VENCIDA);
        }
        reserva.setEstado(Reserva.EstadoReserva.EXPIRADA);
        reserva.getPago().setEstado(Pago.EstadoPago.RECHAZADO);
        reserva.getCodigoRecojo().setEstado(CodigoRecojo.EstadoCodigo.VENCIDO);
        avisos.efectivoVencido(reserva);
    }
}
