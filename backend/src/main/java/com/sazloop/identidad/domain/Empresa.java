package com.sazloop.identidad.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Organización comercial que agrupa varios establecimientos físicos. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "empresas",
        uniqueConstraints = @UniqueConstraint(name = "uk_empresa_ruc", columnNames = "ruc"))
public class Empresa {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 150)
    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @NotBlank @Size(max = 120)
    @Column(name = "nombre_comercial", nullable = false, length = 120)
    private String nombreComercial;

    /** El RUC peruano contiene exactamente once dígitos. */
    @NotBlank @Pattern(regexp = "\\d{11}")
    @Column(nullable = false, length = 11)
    private String ruc;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "propietario_id", nullable = false, unique = true)
    private PropietarioEmpresa propietario;

    @Builder.Default
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Establecimiento> establecimientos = new ArrayList<>();

    @Builder.Default @Column(nullable = false)
    private boolean activa = true;

    @Builder.Default
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}

