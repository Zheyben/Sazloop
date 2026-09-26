package com.sazloop.transacciones.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Devolución simulada del 50 % correspondiente a una cancelación válida. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "reembolsos")
public class Reembolso {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cancelacion_id", nullable = false, unique = true)
    private CancelacionReserva cancelacion;

    @NotNull @DecimalMin(value = "0.00")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal importe;

    @Builder.Default @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReembolso estado = EstadoReembolso.PENDIENTE;

    @Builder.Default @Column(nullable = false)
    private boolean simulado = true;

    @Builder.Default
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public enum EstadoReembolso { PENDIENTE, PROCESADO, RECHAZADO }
}

