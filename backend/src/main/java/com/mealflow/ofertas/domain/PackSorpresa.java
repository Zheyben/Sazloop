package com.mealflow.ofertas.domain;

import com.mealflow.identidad.domain.Establecimiento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Definición reutilizable del pack sorpresa de un establecimiento.
 * Describe posibles productos, pero no revela el contenido exacto.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "packs_sorpresa")
public class PackSorpresa {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "establecimiento_id", nullable = false)
    private Establecimiento establecimiento;

    @NotBlank @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nombre;

    @NotBlank @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String descripcion;

    @NotBlank @Size(max = 1000)
    @Column(name = "posibles_productos", nullable = false, length = 1000)
    private String posiblesProductos;

    @Builder.Default
    @ManyToMany
    @JoinTable(name = "pack_alergenos",
            joinColumns = @JoinColumn(name = "pack_id"),
            inverseJoinColumns = @JoinColumn(name = "alergeno_id"))
    private Set<Alergeno> alergenos = new HashSet<>();

    @Builder.Default
    @ManyToMany
    @JoinTable(name = "pack_restricciones",
            joinColumns = @JoinColumn(name = "pack_id"),
            inverseJoinColumns = @JoinColumn(name = "restriccion_id"))
    private Set<RestriccionAlimentaria> restricciones = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "packSorpresa")
    private List<OfertaPack> ofertas = new ArrayList<>();

    @Builder.Default @Column(nullable = false)
    private boolean activo = true;

    @Builder.Default
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}

