// CÓDIGO TEMPORALMENTE DESACTIVADO PARA EL PRIMER AVANCE.
// Todas las líneas Java están comentadas para evitar dependencias cruzadas
// mientras el equipo termina los demás módulos. Al integrarlo, se debe
// retirar únicamente el primer prefijo "// " de cada línea.
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
//  * Perfil que permite a un Usuario buscar, comprar y recoger packs sorpresa.
//  *
//  * Los datos personales y de acceso permanecen en Usuario. Esta entidad existe
//  * para separar las responsabilidades propias del consumidor.
//  */
// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// @Entity
// @Table(name = "consumidores")
// public class Consumidor {
//
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;
//
//     /**
//      * Cada perfil de consumidor pertenece a una sola cuenta de usuario.
//      * unique = true evita crear dos perfiles consumidores para la misma cuenta.
//      */
//     @OneToOne(fetch = FetchType.LAZY, optional = false)
//     @JoinColumn(
//             name = "usuario_id",
//             nullable = false,
//             unique = true
//     )
//     private Usuario usuario;
//
//     @Builder.Default
//     @Column(nullable = false)
//     private boolean activo = true;
//
//     @Builder.Default
//     @Column(name = "fecha_creacion", nullable = false, updatable = false)
//     private LocalDateTime fechaCreacion = LocalDateTime.now();
// }
