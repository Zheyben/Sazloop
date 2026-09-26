package com.sazloop.ofertas.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/** Catálogo reutilizable de advertencias o condiciones alimentarias. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "restricciones_alimentarias",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_restriccion_nombre", columnNames = "nombre"))
public class RestriccionAlimentaria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(max = 300) @Column(length = 300)
    private String descripcion;

    @Builder.Default @Column(nullable = false)
    private boolean activa = true;

    @Builder.Default
    @ManyToMany(mappedBy = "restricciones")
    private Set<PackSorpresa> packs = new HashSet<>();
}

