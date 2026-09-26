package com.sazloop.experiencia.domain;

import com.sazloop.reservas.domain.Recojo;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Impacto ambiental asociado a un recojo completado.
 * Los valores pueden quedar pendientes hasta definir la fórmula oficial.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "impactos_co2_evitado")
public class ImpactoCO2Evitado {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recojo_id", nullable = false, unique = true)
    private Recojo recojo;

    @DecimalMin(value = "0.00")
    @Column(name = "alimento_recuperado_kg", precision = 10, scale = 3)
    private BigDecimal alimentoRecuperadoKg;

    @DecimalMin(value = "0.00")
    @Column(name = "co2_evitado_kg", precision = 10, scale = 3)
    private BigDecimal co2EvitadoKg;

    @Builder.Default
    @Column(name = "estimacion_disponible", nullable = false)
    private boolean estimacionDisponible = false;

    @Column(name = "metodologia", length = 300)
    private String metodologia;

    @Builder.Default
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}

