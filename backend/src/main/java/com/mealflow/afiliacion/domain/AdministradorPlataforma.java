/* package com.salvemoslacomida.afiliacion.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "administradores_plataforma")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdministradorPlataforma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String emailInstitucional;

    @Column(nullable = false)
    private Integer nivelAcceso;

    @OneToMany(mappedBy = "administrador")
    private List<RevisionDocumento> revisiones;

    @OneToMany(mappedBy = "administrador")
    private List<EvaluacionAfiliacion> evaluaciones;
    
    @OneToMany(mappedBy = "administrador")
    private List<SuspensionEstablecimiento> suspensiones;
} */