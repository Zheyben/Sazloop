package com.sazloop.reservas.dto;

import com.sazloop.ofertas.domain.VariantePack.TamanioVariante;
import com.sazloop.reservas.domain.Reserva.EstadoReserva;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Compra con su boleta y código; las líneas indican al personal qué entregar. */
public record CompraResponse(Long reservaId, Long establecimientoId, String establecimiento, EstadoReserva estado, BigDecimal total,
        String medioPago, boolean pagoSimulado, String numeroBoleta,
        String avisoBoleta, String codigoRecojo,
        LocalDateTime inicioRecojo, LocalDateTime finRecojo, boolean resenaPendiente,
        boolean cancelable, BigDecimal importeReembolso, String cliente, List<Linea> lineas) {
    public record Linea(TamanioVariante tamanio, int cantidad, BigDecimal precioUnitario) {}
}
