package com.sazloop.transacciones.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Comisión del 10 % calculada sobre el importe que permanece cobrado. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "comisiones_plataforma")
public class ComisionPlataforma {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pago_id", nullable = false, unique = true)
    private Pago pago;

    @NotNull @DecimalMin(value = "0.00")
    @Column(name = "importe_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal importeBase;

    @Builder.Default @NotNull
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal porcentaje = new BigDecimal("0.1000");

    @NotNull @DecimalMin(value = "0.00")
    @Column(name = "importe_comision", nullable = false, precision = 10, scale = 2)
    private BigDecimal importeComision;

    @NotNull @DecimalMin(value = "0.00")
    @Column(name = "ingreso_establecimiento", nullable = false, precision = 10, scale = 2)
    private BigDecimal ingresoEstablecimiento;

    /** En efectivo representa el saldo simulado que el local debe a Sazloop. */
    @Builder.Default
    @Column(name = "pendiente_por_efectivo", nullable = false)
    private boolean pendientePorEfectivo = false;

    @Builder.Default
    @Column(name = "fecha_calculo", nullable = false, updatable = false)
    private LocalDateTime fechaCalculo = LocalDateTime.now();
}

