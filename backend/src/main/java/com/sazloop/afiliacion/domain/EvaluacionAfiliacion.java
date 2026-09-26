package com.sazloop.afiliacion.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/** Decisión final de aprobación o rechazo de una solicitud. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "evaluaciones_afiliacion")
public class EvaluacionAfiliacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false, unique = true)
    private SolicitudAfiliacion solicitudAfiliacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "administrador_id", nullable = false)
    private AdministradorPlataforma administrador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DecisionAfiliacion decision;

    @Size(max = 500) @Column(length = 500)
    private String motivo;

    @Builder.Default
    @Column(name = "fecha_resolucion", nullable = false, updatable = false)
    private LocalDateTime fechaResolucion = LocalDateTime.now();

    public enum DecisionAfiliacion { APROBADA, RECHAZADA }
}

