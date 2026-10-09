package com.sazloop.ofertas.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Publicación concreta de un pack para una fecha y ventana de recojo.
 * En V1 cada establecimiento puede tener como máximo una oferta activa.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "ofertas_pack")
public class OfertaPack {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pack_id", nullable = false)
    private PackSorpresa packSorpresa;

    @NotNull
    @Column(name = "fecha_oferta", nullable = false)
    private LocalDate fechaOferta;

    @NotNull
    @Column(name = "inicio_recojo", nullable = false)
    private LocalDateTime inicioRecojo;

    @NotNull
    @Column(name = "fin_recojo", nullable = false)
    private LocalDateTime finRecojo;

    /** Conserva compatibilidad con los datos anteriores; en nuevas ofertas coincide con finRecojo. */
    @NotNull
    @Column(name = "limite_definitivo", nullable = false)
    private LocalDateTime limiteDefinitivo;

    @Builder.Default @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOferta estado = EstadoOferta.BORRADOR;

    @Builder.Default
    @OneToMany(mappedBy = "oferta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VariantePack> variantes = new ArrayList<>();

    @Builder.Default
    @Column(name = "token_qr", unique = true, length = 36)
    private String tokenQr = UUID.randomUUID().toString();

    @Builder.Default
    @Column(name = "fecha_publicacion", nullable = false, updatable = false)
    private LocalDateTime fechaPublicacion = LocalDateTime.now();

    public enum EstadoOferta {
        BORRADOR, ACTIVA, AGOTADA, FINALIZADA, CANCELADA
    }
}

