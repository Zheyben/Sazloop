package com.sazloop.identidad.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Perfil empresarial que administra una empresa desde la cuenta personal. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "propietarios_empresa")
public class PropietarioEmpresa {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @OneToOne(mappedBy = "propietario", fetch = FetchType.LAZY)
    private Empresa empresa;

    @Builder.Default @Column(nullable = false)
    private boolean activo = true;

    @Builder.Default
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}

