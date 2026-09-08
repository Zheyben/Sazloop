package com.mealflow.transacciones.domain;

import com.mealflow.reservas.domain.Reserva;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pago asociado a una reserva. Tarjeta y Yape son simulados;
 * el efectivo solo se permite en compras presenciales por QR.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "pagos")
public class Pago {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    private Reserva reserva;

    @NotNull @DecimalMin(value = "0.01")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal importe;

    @Enumerated(EnumType.STRING)
    @Column(name = "medio_pago", nullable = false, length = 20)
    private MedioPago medioPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContextoPago contexto;

    @Builder.Default @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPago estado = EstadoPago.PENDIENTE;

    /** Permanece verdadero porque MealFlow no procesa dinero real en V1. */
    @Builder.Default @Column(nullable = false)
    private boolean simulado = true;

    @Builder.Default
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @OneToOne(mappedBy = "pago", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private ComisionPlataforma comision;

    public enum MedioPago { TARJETA_DEBITO, YAPE, EFECTIVO }
    public enum ContextoPago { REMOTO, PRESENCIAL_QR }
    public enum EstadoPago { PENDIENTE, CONFIRMADO, RECHAZADO }
}

