package com.sazloop.transacciones.domain;

import com.sazloop.reservas.domain.Reserva;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cancelación de una reserva remota pendiente.
 * Registra el 50 % devuelto, el 50 % penalizado y la devolución del stock.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "cancelaciones_reserva")
public class CancelacionReserva {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    private Reserva reserva;

    @Size(max = 500) @Column(length = 500)
    private String motivo;

    @NotNull @DecimalMin(value = "0.00")
    @Column(name = "importe_reembolso", nullable = false, precision = 10, scale = 2)
    private BigDecimal importeReembolso;

    @NotNull @DecimalMin(value = "0.00")
    @Column(name = "importe_penalizacion", nullable = false, precision = 10, scale = 2)
    private BigDecimal importePenalizacion;

    @Builder.Default
    @Column(name = "stock_devuelto", nullable = false)
    private boolean stockDevuelto = false;

    @Builder.Default
    @Column(name = "fecha_cancelacion", nullable = false, updatable = false)
    private LocalDateTime fechaCancelacion = LocalDateTime.now();

    @OneToOne(mappedBy = "cancelacion", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Reembolso reembolso;
}

