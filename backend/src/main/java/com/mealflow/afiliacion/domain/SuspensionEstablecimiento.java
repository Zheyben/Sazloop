/* package com.salvemoslacomida.afiliacion.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "suspensiones_establecimientos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SuspensionEstablecimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String motivo;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaInicio;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaFin;

    @Column(nullable = false)
    private Boolean estadoActivo;

    @ManyToOne
    @JoinColumn(name = "administrador_id", nullable = false)
    private AdministradorPlataforma administrador;

    // Referencia temporal usando Long según las reglas de tu arquitectura para clases externas
    @Column(name = "establecimiento_id", nullable = false)
    private Long establecimientoId; 
} */