package com.sazloop.experiencia.domain;

import com.sazloop.identidad.domain.Consumidor;
import com.sazloop.identidad.domain.Establecimiento;
import com.sazloop.reservas.domain.Reserva;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/** Calificación verificada que solo se habilita después del recojo. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "resenas")
public class Resena {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consumidor_id", nullable = false)
    private Consumidor consumidor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "establecimiento_id", nullable = false)
    private Establecimiento establecimiento;

    @Min(1) @Max(5)
    @Column(nullable = false)
    private int calificacion;

    @Size(max = 1000) @Column(length = 1000)
    private String comentario;

    @Builder.Default
    @Column(name = "fecha_publicacion", nullable = false, updatable = false)
    private LocalDateTime fechaPublicacion = LocalDateTime.now();

    @Builder.Default @Column(nullable = false)
    private boolean visible = true;
}

