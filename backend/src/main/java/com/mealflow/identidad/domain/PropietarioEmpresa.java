// // CÓDIGO TEMPORALMENTE DESACTIVADO PARA EL PRIMER AVANCE.
// // Todas las líneas Java están comentadas para evitar dependencias cruzadas
// // mientras el equipo termina los demás módulos. Al integrarlo, se debe
// // retirar únicamente el primer prefijo "// " de cada línea.
// package com.mealflow.identidad.domain;
//
// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.FetchType;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.OneToOne;
// import jakarta.persistence.Table;
// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.Setter;
//
// import java.time.LocalDateTime;
//
// /**
//  * Perfil empresarial de un Usuario.
//  *
//  * Permite registrar una empresa, solicitar la afiliación de sus locales y
//  * consultar la información general de todos ellos. La persona puede cambiar
//  * entre este perfil y el perfil consumidor usando una sola cuenta.
//  */
// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// @Entity
// @Table(name = "propietarios_empresa")
// public class PropietarioEmpresa {
//
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;
//
//     @OneToOne(fetch = FetchType.LAZY, optional = false)
//     @JoinColumn(
//             name = "usuario_id",
//             nullable = false,
//             unique = true
//     )
//     private Usuario usuario;
//
//     /**
//      * Empresa registrada por este propietario.
//      * Empresa conserva la clave foránea de la relación.
//      */
//     @OneToOne(mappedBy = "propietario", fetch = FetchType.LAZY)
//     private Empresa empresa;
//
//     @Builder.Default
//     @Column(nullable = false)
//     private boolean activo = true;
//
//     @Builder.Default
//     @Column(name = "fecha_creacion", nullable = false, updatable = false)
//     private LocalDateTime fechaCreacion = LocalDateTime.now();
// }
