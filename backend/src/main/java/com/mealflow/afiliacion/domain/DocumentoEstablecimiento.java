/* package com.mealflow.afiliacion.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "documentos_establecimiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoEstablecimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tipoDocumento;

    @Column(nullable = false)
    private String urlArchivo;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCarga;

    @ManyToOne
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudAfiliacion solicitudAfiliacion;

    @OneToMany(mappedBy = "documento", cascade = CascadeType.ALL)
    private List<RevisionDocumento> revisiones;
} */