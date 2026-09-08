// // CÓDIGO TEMPORALMENTE DESACTIVADO PARA EL PRIMER AVANCE.
// // Todas las líneas Java están comentadas para evitar dependencias cruzadas
// // mientras el equipo termina los demás módulos. Al integrarlo, se debe
// // retirar únicamente el primer prefijo "// " de cada línea.
// package com.mealflow.identidad.domain;
//
// import jakarta.persistence.CascadeType;
// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.EnumType;
// import jakarta.persistence.Enumerated;
// import jakarta.persistence.FetchType;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.ManyToOne;
// import jakarta.persistence.OneToOne;
// import jakarta.persistence.Table;
// import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.Size;
// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.Setter;
//
// import java.time.LocalDateTime;
//
// /**
//  * Representa un local físico de una empresa.
//  *
//  * Cada establecimiento tiene una ubicación, documentación y aprobación
//  * independientes. Por ello, una empresa puede tener un local activo y otro
//  * pendiente o suspendido sin mezclar sus estados.
//  */
// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// @Entity
// @Table(name = "establecimientos")
// public class Establecimiento {
//
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;
//
//     @ManyToOne(fetch = FetchType.LAZY, optional = false)
//     @JoinColumn(name = "empresa_id", nullable = false)
//     private Empresa empresa;
//
//     @NotBlank
//     @Size(max = 120)
//     @Column(nullable = false, length = 120)
//     private String nombre;
//
//     @Size(max = 500)
//     @Column(length = 500)
//     private String descripcion;
//
//     @Size(max = 20)
//     @Column(length = 20)
//     private String telefono;
//
//     /**
//      * Ubicación estructurada del local. En el primer avance no se utilizan
//      * coordenadas, mapas ni geolocalización en tiempo real.
//      */
//     @OneToOne(
//             fetch = FetchType.LAZY,
//             cascade = CascadeType.ALL,
//             orphanRemoval = true,
//             optional = false
//     )
//     @JoinColumn(name = "direccion_id", nullable = false, unique = true)
//     private Direccion direccion;
//
//     /**
//      * Cuenta operativa creada por el propietario después de la aprobación.
//      */
//     @OneToOne(mappedBy = "establecimiento", fetch = FetchType.LAZY)
//     private CuentaEstablecimiento cuentaOperativa;
//
//     @Builder.Default
//     @Enumerated(EnumType.STRING)
//     @Column(nullable = false, length = 30)
//     private EstadoEstablecimiento estado =
//             EstadoEstablecimiento.PENDIENTE_APROBACION;
//
//     @Builder.Default
//     @Column(name = "fecha_registro", nullable = false, updatable = false)
//     private LocalDateTime fechaRegistro = LocalDateTime.now();
//
//     /**
//      * Estado general del local dentro de la plataforma.
//      *
//      * El rechazo de una solicitud pertenece al módulo de afiliación. Si una
//      * solicitud es rechazada, todavía no existe un establecimiento afiliado.
//      */
//     public enum EstadoEstablecimiento {
//         PENDIENTE_APROBACION,
//         ACTIVO,
//         SUSPENDIDO,
//         INACTIVO
//     }
// }
