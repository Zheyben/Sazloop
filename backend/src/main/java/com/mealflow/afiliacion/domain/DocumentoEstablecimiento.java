package com.mealflow.afiliacion.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Documento privado presentado para una afiliación o renovación. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "documentos_establecimiento")
public class DocumentoEstablecimiento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 40)
    private TipoDocumento tipoDocumento = TipoDocumento.OTRO;

    @Size(max = 80)
    @Column(name = "numero_documento", length = 80)
    private String numeroDocumento;

    @NotBlank @Size(max = 500)
    @Column(name = "url_archivo", nullable = false, length = 500)
    private String urlArchivo;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Builder.Default @Column(nullable = false)
    private boolean obligatorio = true;

    @Builder.Default
    @Column(name = "fecha_carga", nullable = false, updatable = false)
    private LocalDateTime fechaCarga = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudAfiliacion solicitudAfiliacion;

    @Builder.Default
    @OneToMany(mappedBy = "documento",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RevisionDocumento> revisiones = new ArrayList<>();

    public enum TipoDocumento {
        LICENCIA_FUNCIONAMIENTO, CERTIFICADO_SANITARIO, INSPECCION_SEGURIDAD, OTRO
    }
}

