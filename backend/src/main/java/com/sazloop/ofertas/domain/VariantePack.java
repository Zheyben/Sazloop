package com.sazloop.ofertas.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/** Tamaño disponible dentro de una oferta, con precio y stock propios. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "variantes_pack")
public class VariantePack {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oferta_id", nullable = false)
    private OfertaPack oferta;

    @NotNull @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TamanioVariante tamanio;

    @NotNull @DecimalMin(value = "0.01")
    @Column(name = "precio_referencial", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioReferencial;

    @NotNull @DecimalMin(value = "0.01")
    @Column(name = "precio_descuento", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioDescuento;

    @Min(0)
    @Column(name = "stock_inicial", nullable = false)
    private int stockInicial;

    @Min(0)
    @Column(name = "stock_disponible", nullable = false)
    private int stockDisponible;

    @Builder.Default @Column(nullable = false)
    private boolean activa = true;

    public enum TamanioVariante { PEQUENO, MEDIANO, GRANDE }
}

