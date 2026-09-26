package com.sazloop.afiliacion.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/** Revisión manual de un documento realizada por un administrador. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "revisiones_documento")
public class RevisionDocumento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "documento_id", nullable = false)
    private DocumentoEstablecimiento documento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "administrador_id", nullable = false)
    private AdministradorPlataforma administrador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResultadoRevision resultado;

    @Size(max = 500) @Column(length = 500)
    private String observaciones;

    @Builder.Default
    @Column(name = "fecha_revision", nullable = false, updatable = false)
    private LocalDateTime fechaRevision = LocalDateTime.now();

    public enum ResultadoRevision { APROBADO, RECHAZADO }
}

