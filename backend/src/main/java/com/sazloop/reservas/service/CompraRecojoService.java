package com.sazloop.reservas.service;

import com.sazloop.experiencia.repository.ResenaRepository;
import com.sazloop.experiencia.repository.ImpactoCO2EvitadoRepository;
import com.sazloop.experiencia.domain.ImpactoCO2Evitado;
import com.sazloop.identidad.domain.*;
import com.sazloop.identidad.repository.*;
import com.sazloop.ofertas.domain.*;
import com.sazloop.ofertas.repository.OfertaPackRepository;
import com.sazloop.reservas.domain.*;
import com.sazloop.reservas.dto.*;
import com.sazloop.reservas.repository.*;
import com.sazloop.transacciones.domain.*;
import com.sazloop.transacciones.service.ComisionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/** Compra simulada, descuento seguro de stock, boleta, código y entrega. */
@Service
public class CompraRecojoService {
    private final OfertaPackRepository ofertas;
    private final ReservaRepository reservas;
    private final CodigoRecojoRepository codigos;
    private final ConsumidorRepository consumidores;
    private final UsuarioRepository usuarios;
    private final ResenaRepository resenas;
    private final ImpactoCO2EvitadoRepository impactos;
    private final AvisosReservaService avisos;

    public CompraRecojoService(OfertaPackRepository ofertas, ReservaRepository reservas,
            CodigoRecojoRepository codigos, ConsumidorRepository consumidores, UsuarioRepository usuarios,
            ResenaRepository resenas, ImpactoCO2EvitadoRepository impactos, AvisosReservaService avisos) {
        this.ofertas = ofertas; this.reservas = reservas; this.codigos = codigos;
        this.consumidores = consumidores; this.usuarios = usuarios; this.resenas = resenas;
        this.impactos = impactos; this.avisos = avisos;
    }

    /** Una falla en cualquier pedido revierte el pago y todas las reservas del lote. */
    @Transactional
    public List<CompraResponse> comprarVarios(Long usuarioId, CompraMultipleRequest entrada) {
        if (entrada.medioPago() == Pago.MedioPago.EFECTIVO)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El efectivo no es un pago remoto");
        return entrada.pedidos().stream()
                .map(pedido -> comprar(usuarioId,
                        new CompraRequest(pedido.ofertaId(), entrada.medioPago(), pedido.lineas())))
                .toList();
    }

    /** Bloquea la oferta hasta reservar el stock; dos compradores no pueden consumir la misma unidad. */
    @Transactional
    public CompraResponse comprar(Long usuarioId, CompraRequest entrada) {
        if (entrada.medioPago() == Pago.MedioPago.EFECTIVO)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El efectivo no es un pago remoto");
        OfertaPack oferta = ofertas.bloquear(entrada.ofertaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        LocalDateTime ahora = LocalDateTime.now();
        if (oferta.getEstado() != OfertaPack.EstadoOferta.ACTIVA ||
                oferta.getPackSorpresa().getEstablecimiento().getEstado()
                        != Establecimiento.EstadoEstablecimiento.ACTIVO ||
                !ahora.isBefore(oferta.getFinRecojo()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Oferta no disponible");
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (CompraRequest.Linea linea : entrada.lineas()) {
            if (linea.cantidad() < 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cantidad inválida");
            cantidades.merge(linea.varianteId(), linea.cantidad(), (a, b) -> {
                try { return Math.addExact(a, b); }
                catch (ArithmeticException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cantidad inválida"); }
            });
        }
        Map<Long, VariantePack> disponibles = new HashMap<>();
        oferta.getVariantes().forEach(v -> disponibles.put(v.getId(), v));
        BigDecimal total = BigDecimal.ZERO;
        for (var linea : cantidades.entrySet()) {
            VariantePack variante = disponibles.get(linea.getKey());
            if (variante == null || !variante.isActiva() || variante.getStockDisponible() < linea.getValue())
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente o variante ajena a la oferta");
            total = total.add(variante.getPrecioDescuento().multiply(BigDecimal.valueOf(linea.getValue())));
        }
        Usuario usuario = usuarios.findById(usuarioId).filter(Usuario::isActivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        Consumidor consumidor = consumidores.findByUsuario_Id(usuarioId)
                .orElseGet(() -> consumidores.save(Consumidor.builder().usuario(usuario).build()));
        if (!consumidor.isActivo()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        Reserva reserva = Reserva.builder().consumidor(consumidor).oferta(oferta)
                .origenCompra(Reserva.OrigenCompra.REMOTA).montoTotal(total)
                .estado(Reserva.EstadoReserva.CONFIRMADA).fechaConfirmacion(ahora).build();
        for (var linea : cantidades.entrySet()) {
            VariantePack variante = disponibles.get(linea.getKey());
            int cantidad = linea.getValue();
            variante.setStockDisponible(variante.getStockDisponible() - cantidad);
            reserva.getDetalles().add(DetalleReserva.builder().reserva(reserva).variante(variante)
                    .cantidad(cantidad).precioUnitario(variante.getPrecioDescuento())
                    .subtotal(variante.getPrecioDescuento().multiply(BigDecimal.valueOf(cantidad))).build());
        }
        Pago pago = Pago.builder().reserva(reserva).importe(total).medioPago(entrada.medioPago())
                .contexto(Pago.ContextoPago.REMOTO).estado(Pago.EstadoPago.CONFIRMADO)
                .fechaConfirmacion(ahora).build();
        BoletaSimulada boleta = BoletaSimulada.builder().reserva(reserva).total(total)
                .numero("SIM-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase(Locale.ROOT)).build();
        CodigoRecojo codigo = CodigoRecojo.builder().reserva(reserva)
                .codigo(UUID.randomUUID().toString().substring(0, 16).toUpperCase(Locale.ROOT)).build();
        ComisionService.recalcular(pago, total);
        reserva.setPago(pago);
        reserva.setBoleta(boleta);
        reserva.setCodigoRecojo(codigo);
        Reserva guardada = reservas.saveAndFlush(reserva);
        avisos.recordatorioRecojo(guardada);
        return respuesta(guardada);
    }

    @Transactional(readOnly = true)
    public List<CompraResponse> misCompras(Long usuarioId) {
        return reservas.findByConsumidor_Usuario_IdOrderByIdDesc(usuarioId)
                .stream().map(this::respuesta).toList();
    }

    @Transactional(readOnly = true)
    public CompraResponse consultarCodigo(CuentaEstablecimiento cuenta, String codigo) {
        return respuesta(reservaDelLocal(cuenta, codigo));
    }

    @Transactional
    public CompraResponse recoger(CuentaEstablecimiento cuenta, String codigo) {
        Reserva reserva = reservaDelLocal(cuenta, codigo, true);
        LocalDateTime ahora = LocalDateTime.now();
        OfertaPack oferta = reserva.getOferta();
        if (reserva.getEstado() != Reserva.EstadoReserva.CONFIRMADA ||
                reserva.getCodigoRecojo().getEstado() != CodigoRecojo.EstadoCodigo.PENDIENTE)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El código ya fue usado o la compra no está confirmada");
        // El fin del horario es el único límite: se puede entregar desde el momento del pago.
        if (!ahora.isBefore(oferta.getFinRecojo()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El horario de recojo de este pedido ya terminó");
        reserva.getCodigoRecojo().setEstado(CodigoRecojo.EstadoCodigo.UTILIZADO);
        reserva.getCodigoRecojo().setFechaUso(ahora);
        reserva.setEstado(Reserva.EstadoReserva.RECOGIDA);
        reserva.setRecojo(Recojo.builder().reserva(reserva).confirmadoPor(cuenta).build());
        reservas.saveAndFlush(reserva);
        impactos.save(ImpactoCO2Evitado.builder().recojo(reserva.getRecojo()).build());
        return respuesta(reserva);
    }

    private Reserva reservaDelLocal(CuentaEstablecimiento cuenta, String codigo) {
        return reservaDelLocal(cuenta, codigo, false);
    }

    private Reserva reservaDelLocal(CuentaEstablecimiento cuenta, String codigo, boolean bloquear) {
        String valor = codigo.trim().toUpperCase(Locale.ROOT);
        CodigoRecojo encontrado = (bloquear ? codigos.bloquear(valor) : codigos.findByCodigo(valor))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Reserva reserva = encontrado.getReserva();
        if (!reserva.getOferta().getPackSorpresa().getEstablecimiento().getId()
                .equals(cuenta.getEstablecimiento().getId()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return reserva;
    }

    private CompraResponse respuesta(Reserva reserva) {
        return new CompraResponse(reserva.getId(),
                reserva.getOferta().getPackSorpresa().getEstablecimiento().getId(),
                reserva.getOferta().getPackSorpresa().getEstablecimiento().getNombre(),
                reserva.getEstado(), reserva.getMontoTotal(),
                reserva.getPago().getMedioPago().name(), reserva.getPago().isSimulado(),
                reserva.getBoleta() == null ? null : reserva.getBoleta().getNumero(),
                reserva.getBoleta() == null ? null : reserva.getBoleta().getMensajeLegal(),
                reserva.getCodigoRecojo().getCodigo(), reserva.getOferta().getInicioRecojo(),
                reserva.getOferta().getFinRecojo(),
                reserva.getEstado() == Reserva.EstadoReserva.RECOGIDA
                        && !reserva.isResenaOmitida()
                        && !resenas.existsByReserva_Id(reserva.getId())
                        && !resenas.existsByConsumidor_IdAndEstablecimiento_Id(
                                reserva.getConsumidor().getId(),
                                reserva.getOferta().getPackSorpresa().getEstablecimiento().getId()),
                CancelacionService.puedeCancelar(reserva, LocalDateTime.now()),
                reserva.getCancelacion() == null ? null : reserva.getCancelacion().getImporteReembolso(),
                cliente(reserva.getConsumidor().getUsuario()),
                reserva.getDetalles().stream().map(detalle -> new CompraResponse.Linea(
                        detalle.getVariante().getTamanio(), detalle.getCantidad(), detalle.getPrecioUnitario()))
                        .toList());
    }

    /** Nombre y la inicial del apellido: suficiente para que el personal identifique al cliente. */
    private static String cliente(Usuario usuario) {
        String nombre = usuario.getNombres().trim().split("\\s+")[0];
        String apellido = usuario.getApellidos().trim();
        return apellido.isEmpty() ? nombre : nombre + " " + apellido.charAt(0) + ".";
    }
}
