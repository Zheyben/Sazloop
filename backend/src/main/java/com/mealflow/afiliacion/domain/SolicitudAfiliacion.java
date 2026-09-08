/* package com.mealflow.afiliacion.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "solicitudes_afiliacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudAfiliacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaSolicitud;

    @Column(nullable = false)
    private String estado; // PENDIENTE, EN_REVISION, APROBADO, RECHAZADO

    @OneToMany(mappedBy = "solicitudAfiliacion", cascade = CascadeType.ALL)
    private List<DocumentoEstablecimiento> documentos;

    @OneToOne(mappedBy = "solicitudAfiliacion", cascade = CascadeType.ALL)
    private EvaluacionAfiliacion evaluacion;
} */