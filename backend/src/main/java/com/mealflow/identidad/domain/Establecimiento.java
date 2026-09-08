package com.mealflow.identidad.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/** Local físico con ubicación, aprobación y operación independientes. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "establecimientos")
public class Establecimiento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @NotBlank @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nombre;

    @Size(max = 500) @Column(length = 500)
    private String descripcion;

    @Size(max = 20) @Column(length = 20)
    private String telefono;

    /** En V1 la ubicación es textual; no usa GPS ni mapas. */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL,
            orphanRemoval = true, optional = false)
    @JoinColumn(name = "direccion_id", nullable = false, unique = true)
    private Direccion direccion;

    /** Cuenta compartida creada después de la aprobación del local. */
    @OneToOne(mappedBy = "establecimiento", fetch = FetchType.LAZY)
    private CuentaEstablecimiento cuentaOperativa;

    @Builder.Default @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoEstablecimiento estado = EstadoEstablecimiento.PENDIENTE_APROBACION;

    @Builder.Default
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public enum EstadoEstablecimiento {
        PENDIENTE_APROBACION, ACTIVO, SUSPENDIDO, INACTIVO
    }
}

