package com.mealflow.identidad.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/** Ubicación textual estructurada que permite buscar establecimientos por distrito. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "direcciones")
public class Direccion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String departamento;

    @NotBlank @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String provincia;

    @NotBlank @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String distrito;

    @NotBlank @Size(max = 180)
    @Column(name = "direccion_linea", nullable = false, length = 180)
    private String direccionLinea;

    @Size(max = 20) @Column(length = 20)
    private String numero;

    @Size(max = 250) @Column(length = 250)
    private String referencia;

    @Size(max = 10)
    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @OneToOne(mappedBy = "direccion", fetch = FetchType.LAZY)
    private Establecimiento establecimiento;
}

