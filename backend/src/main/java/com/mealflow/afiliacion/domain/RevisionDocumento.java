/* package com.mealflow.afiliacion.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "revisiones_documentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RevisionDocumento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean estadoValidez;

    private String observaciones;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRevision;

    @ManyToOne
    @JoinColumn(name = "documento_id", nullable = false)
    private DocumentoEstablecimiento documento;

    @ManyToOne
    @JoinColumn(name = "administrador_id", nullable = false)
    private AdministradorPlataforma administrador;
} */