package com.sazloop.reservas.dto;

import com.sazloop.transacciones.domain.Pago.MedioPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Un pago conjunto que origina reservas independientes. */
public record CompraMultipleRequest(@NotNull MedioPago medioPago,
        @NotEmpty @Valid List<Pedido> pedidos) {
    public record Pedido(@NotNull Long ofertaId,
            @NotEmpty @Valid List<CompraRequest.Linea> lineas) {}
}
