/* package com.mealflow.afiliacion.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "evaluaciones_afiliacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionAfiliacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String veredictoFinal;

    private String motivoRechazo;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaResolucion;

    @OneToOne
    @JoinColumn(name = "solicitud_id", nullable = false, unique = true)
    private SolicitudAfiliacion solicitudAfiliacion;

    @ManyToOne
    @JoinColumn(name = "administrador_id", nullable = false)
    private AdministradorPlataforma administrador;
} */