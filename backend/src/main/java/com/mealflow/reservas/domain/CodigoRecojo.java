package com.mealflow.reservas.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/** Código único que el consumidor dicta al personal para recibir su pack. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "codigos_recojo",
        uniqueConstraints = @UniqueConstraint(name = "uk_codigo_recojo", columnNames = "codigo"))
public class CodigoRecojo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    private Reserva reserva;

    @NotBlank @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String codigo;

    @Builder.Default @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoCodigo estado = EstadoCodigo.PENDIENTE;

    @Builder.Default
    @Column(name = "fecha_generacion", nullable = false, updatable = false)
    private LocalDateTime fechaGeneracion = LocalDateTime.now();

    @Column(name = "fecha_uso")
    private LocalDateTime fechaUso;

    public enum EstadoCodigo { PENDIENTE, UTILIZADO, VENCIDO }
}

