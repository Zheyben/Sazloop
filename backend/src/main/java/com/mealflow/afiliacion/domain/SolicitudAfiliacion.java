package com.mealflow.afiliacion.domain;

import com.mealflow.identidad.domain.Establecimiento;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Solicitud para autorizar a un establecimiento; cada reintento crea una nueva. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "solicitudes_afiliacion")
public class SolicitudAfiliacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "establecimiento_id", nullable = false)
    private Establecimiento establecimiento;

    @Builder.Default
    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    @Builder.Default @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @Builder.Default
    @OneToMany(mappedBy = "solicitudAfiliacion",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentoEstablecimiento> documentos = new ArrayList<>();

    @OneToOne(mappedBy = "solicitudAfiliacion", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private EvaluacionAfiliacion evaluacion;

    public enum EstadoSolicitud { PENDIENTE, EN_REVISION, APROBADA, RECHAZADA }
}

