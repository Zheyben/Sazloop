package com.mealflow.experiencia.domain;

import com.mealflow.identidad.domain.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/** Aviso persistente dirigido a un usuario por un acontecimiento del sistema. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "notificaciones")
public class Notificacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario destinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 35)
    private TipoNotificacion tipo;

    @NotBlank @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String titulo;

    @NotBlank @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    private String mensaje;

    /** Identifica de forma simple el registro que originó el aviso, si existe. */
    @Size(max = 60)
    @Column(name = "tipo_referencia", length = 60)
    private String tipoReferencia;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Builder.Default
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Builder.Default @Column(nullable = false)
    private boolean leida = false;

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    public enum TipoNotificacion {
        RESULTADO_AFILIACION,
        DOCUMENTO_POR_VENCER,
        SUSPENSION,
        RECORDATORIO_RECOJO,
        CAMBIO_RESERVA,
        GENERAL
    }
}

