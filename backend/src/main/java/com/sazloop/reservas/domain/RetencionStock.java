package com.sazloop.reservas.domain;

import com.sazloop.identidad.domain.Consumidor;
import com.sazloop.ofertas.domain.VariantePack;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Separa temporalmente stock mientras una compra está en proceso,
 * especialmente al esperar la confirmación de efectivo presencial.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "retenciones_stock")
public class RetencionStock {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variante_id", nullable = false)
    private VariantePack variante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consumidor_id", nullable = false)
    private Consumidor consumidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    @Min(1)
    @Column(nullable = false)
    private int cantidad;

    @NotNull
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @NotNull
    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Builder.Default @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoRetencion estado = EstadoRetencion.ACTIVA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private MotivoRetencion motivo;

    public enum EstadoRetencion { ACTIVA, CONFIRMADA, LIBERADA, VENCIDA }
    public enum MotivoRetencion { COMPRA_DIGITAL, EFECTIVO_PRESENCIAL }
}

