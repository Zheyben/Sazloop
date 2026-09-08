package com.mealflow.afiliacion.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Cuenta interna que revisa documentos, afiliaciones y suspensiones. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "administradores_plataforma",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_administrador_email", columnNames = "email_institucional"))
public class AdministradorPlataforma {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nombre;

    @NotBlank @Email @Size(max = 150)
    @Column(name = "email_institucional", nullable = false, length = 150)
    private String emailInstitucional;

    @NotBlank @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Builder.Default @Enumerated(EnumType.STRING)
    @Column(name = "nivel_acceso", nullable = false, length = 20)
    private NivelAcceso nivelAcceso = NivelAcceso.ADMINISTRADOR;

    @Builder.Default @Column(nullable = false)
    private boolean activo = true;

    @Builder.Default
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Builder.Default @OneToMany(mappedBy = "administrador")
    private List<RevisionDocumento> revisiones = new ArrayList<>();

    @Builder.Default @OneToMany(mappedBy = "administrador")
    private List<EvaluacionAfiliacion> evaluaciones = new ArrayList<>();

    @Builder.Default @OneToMany(mappedBy = "administrador")
    private List<SuspensionEstablecimiento> suspensiones = new ArrayList<>();

    public enum NivelAcceso { ADMINISTRADOR, SUPER_ADMINISTRADOR }
}

