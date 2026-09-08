package com.mealflow.transacciones.domain;

import com.mealflow.reservas.domain.Reserva;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Comprobante interno sin validez tributaria generado después del pago. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "boletas_simuladas",
        uniqueConstraints = @UniqueConstraint(name = "uk_boleta_numero", columnNames = "numero"))
public class BoletaSimulada {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    private Reserva reserva;

    @NotBlank @Size(max = 30)
    @Column(nullable = false, length = 30)
    private String numero;

    @NotNull @DecimalMin(value = "0.01")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Builder.Default
    @Column(name = "fecha_emision", nullable = false, updatable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now();

    @Builder.Default
    @Column(name = "mensaje_legal", nullable = false, length = 150)
    private String mensajeLegal = "Comprobante simulado sin validez tributaria";
}

