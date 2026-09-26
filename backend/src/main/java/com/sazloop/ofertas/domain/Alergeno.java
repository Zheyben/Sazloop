package com.sazloop.ofertas.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/** Catálogo reutilizable de sustancias que podrían causar alergias. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "alergenos",
        uniqueConstraints = @UniqueConstraint(name = "uk_alergeno_nombre", columnNames = "nombre"))
public class Alergeno {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String nombre;

    @Size(max = 300) @Column(length = 300)
    private String descripcion;

    @Builder.Default @Column(nullable = false)
    private boolean activo = true;

    @Builder.Default
    @ManyToMany(mappedBy = "alergenos")
    private Set<PackSorpresa> packs = new HashSet<>();
}

