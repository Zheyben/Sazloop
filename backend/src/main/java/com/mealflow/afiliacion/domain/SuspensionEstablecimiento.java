package com.mealflow.afiliacion.domain;

import com.mealflow.identidad.domain.Establecimiento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/** Suspensión que conserva el establecimiento y todo su historial. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "suspensiones_establecimiento")
public class SuspensionEstablecimiento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "establecimiento_id", nullable = false)
    private Establecimiento establecimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "administrador_id", nullable = false)
    private AdministradorPlataforma administrador;

    @NotBlank @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String motivo;

    @Builder.Default
    @Column(name = "fecha_inicio", nullable = false, updatable = false)
    private LocalDateTime fechaInicio = LocalDateTime.now();

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Builder.Default @Column(nullable = false)
    private boolean activa = true;
}

