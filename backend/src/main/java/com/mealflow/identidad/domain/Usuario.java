package com.mealflow.identidad.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/** Cuenta principal de una persona; puede combinar perfiles consumidor y empresarial. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "usuarios",
        uniqueConstraints = @UniqueConstraint(name = "uk_usuario_email", columnNames = "email"))
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String nombres;

    @NotBlank @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String apellidos;

    @NotBlank @Email @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String email;

    /** Se guarda el hash de la contraseña, nunca la contraseña en texto plano. */
    @NotBlank @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Size(max = 20)
    @Column(length = 20)
    private String telefono;

    @Builder.Default @Column(nullable = false)
    private boolean activo = true;

    @Builder.Default
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY)
    private Consumidor consumidor;

    @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY)
    private PropietarioEmpresa propietarioEmpresa;
}

