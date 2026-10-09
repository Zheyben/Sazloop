package com.sazloop.reservas.service;

import com.sazloop.identidad.domain.Establecimiento;
import com.sazloop.ofertas.domain.OfertaPack;
import com.sazloop.ofertas.repository.OfertaPackRepository;
import com.sazloop.reservas.domain.CodigoRecojo;
import com.sazloop.reservas.domain.Reserva;
import com.sazloop.reservas.repository.ReservaRepository;
import com.sazloop.transacciones.domain.CancelacionReserva;
import com.sazloop.transacciones.domain.Reembolso;
import com.sazloop.transacciones.service.ComisionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class CancelacionService {
    private final ReservaRepository reservas;
    private final OfertaPackRepository ofertas;

    public CancelacionService(ReservaRepository reservas, OfertaPackRepository ofertas) {
        this.reservas = reservas;
        this.ofertas = ofertas;
    }

    @Transactional
    public void cancelar(Long usuarioId, Long reservaId) {
        Reserva reserva = reservas.bloquear(reservaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!reserva.getConsumidor().getUsuario().getId().equals(usuarioId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (!puedeCancelar(reserva, LocalDateTime.now()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta compra ya no puede cancelarse");

        OfertaPack oferta = ofertas.bloquear(reserva.getOferta().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        LocalDateTime ahora = LocalDateTime.now();
        if (!puedeCancelar(reserva, ahora))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Se alcanzó el límite de cancelación");
        boolean devolverStock = oferta.getEstado() == OfertaPack.EstadoOferta.ACTIVA
                && oferta.getPackSorpresa().getEstablecimiento().getEstado()
                    == Establecimiento.EstadoEstablecimiento.ACTIVO
                && ahora.isBefore(oferta.getFinRecojo());
        if (devolverStock) reserva.getDetalles().forEach(detalle ->
                detalle.getVariante().setStockDisponible(
                        detalle.getVariante().getStockDisponible() + detalle.getCantidad()));

        BigDecimal reembolso = reserva.getMontoTotal().multiply(new BigDecimal("0.50"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal retenido = reserva.getMontoTotal().subtract(reembolso);
        ComisionService.recalcular(reserva.getPago(), retenido);
        CancelacionReserva cancelacion = CancelacionReserva.builder().reserva(reserva)
                .importeReembolso(reembolso).importePenalizacion(retenido)
                .stockDevuelto(devolverStock).build();
        cancelacion.setReembolso(Reembolso.builder().cancelacion(cancelacion).importe(reembolso)
                .estado(Reembolso.EstadoReembolso.PROCESADO).build());
        reserva.setCancelacion(cancelacion);
        reserva.setEstado(Reserva.EstadoReserva.CANCELADA);
        reserva.getCodigoRecojo().setEstado(CodigoRecojo.EstadoCodigo.VENCIDO);
        reservas.saveAndFlush(reserva);
    }

    public static boolean puedeCancelar(Reserva reserva, LocalDateTime ahora) {
        return reserva.getOrigenCompra() == Reserva.OrigenCompra.REMOTA
                && reserva.getEstado() == Reserva.EstadoReserva.CONFIRMADA
                && ahora.isBefore(reserva.getOferta().getFinRecojo());
    }
}
