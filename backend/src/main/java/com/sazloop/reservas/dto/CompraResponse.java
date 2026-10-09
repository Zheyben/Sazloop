package com.sazloop.reservas.dto;

import com.sazloop.reservas.domain.Reserva.EstadoReserva;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CompraResponse(Long reservaId, Long establecimientoId, String establecimiento, EstadoReserva estado, BigDecimal total,
        String medioPago, boolean pagoSimulado, String numeroBoleta,
        String avisoBoleta, String codigoRecojo,
        LocalDateTime inicioRecojo, LocalDateTime finRecojo, boolean resenaPendiente,
        boolean cancelable, BigDecimal importeReembolso) {}
