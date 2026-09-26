package com.sazloop.reservas.domain;

import com.sazloop.experiencia.domain.Resena;
import com.sazloop.identidad.domain.Consumidor;
import com.sazloop.ofertas.domain.OfertaPack;
import com.sazloop.transacciones.domain.BoletaSimulada;
import com.sazloop.transacciones.domain.CancelacionReserva;
import com.sazloop.transacciones.domain.Pago;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Compra que conecta al consumidor con la oferta, el pago y el recojo. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "reservas")
public class Reserva {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consumidor_id", nullable = false)
    private Consumidor consumidor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oferta_id", nullable = false)
    private OfertaPack oferta;

    @Builder.Default
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleReserva> detalles = new ArrayList<>();

    @NotNull @DecimalMin(value = "0.01")
    @Column(name = "monto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;

    @Builder.Default @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado = EstadoReserva.PENDIENTE_PAGO;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen_compra", nullable = false, length = 20)
    private OrigenCompra origenCompra;

    @Builder.Default
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @OneToOne(mappedBy = "reserva", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Pago pago;

    @OneToOne(mappedBy = "reserva", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private BoletaSimulada boleta;

    @OneToOne(mappedBy = "reserva", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private CodigoRecojo codigoRecojo;

    @OneToOne(mappedBy = "reserva", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Recojo recojo;

    @OneToOne(mappedBy = "reserva", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private CancelacionReserva cancelacion;

    @OneToOne(mappedBy = "reserva", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Resena resena;

    public enum EstadoReserva {
        PENDIENTE_PAGO, CONFIRMADA, RECOGIDA, CANCELADA, EXPIRADA
    }

    public enum OrigenCompra { REMOTA, PRESENCIAL_QR }
}

