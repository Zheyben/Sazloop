package com.sazloop.identidad.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Perfil que permite buscar, comprar, recoger y calificar packs sorpresa. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "consumidores")
public class Consumidor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Builder.Default @Column(nullable = false)
    private boolean activo = true;

    @Builder.Default
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}

