package com.sazloop.identidad.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/** Cuenta operativa compartida; en V1 no se registran empleados individuales. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "cuentas_establecimiento",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cuenta_establecimiento_usuario", columnNames = "nombre_usuario"))
public class CuentaEstablecimiento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "establecimiento_id", nullable = false, unique = true)
    private Establecimiento establecimiento;

    @NotBlank @Size(max = 80)
    @Column(name = "nombre_usuario", nullable = false, length = 80)
    private String nombreUsuario;

    @NotBlank @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Builder.Default @Column(nullable = false)
    private boolean activa = true;

    @Builder.Default
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;
}

