package com.mealflow.reservas.domain;

import com.mealflow.identidad.domain.CuentaEstablecimiento;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/** Registro de la entrega física que cierra la compra y habilita la reseña. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "recojos")
public class Recojo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_establecimiento_id", nullable = false)
    private CuentaEstablecimiento confirmadoPor;

    @Builder.Default
    @Column(name = "fecha_recojo", nullable = false, updatable = false)
    private LocalDateTime fechaRecojo = LocalDateTime.now();

    @Size(max = 300) @Column(length = 300)
    private String observaciones;
}

