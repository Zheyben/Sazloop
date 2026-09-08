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
// import jakarta.persistence.OneToOne;
// import jakarta.persistence.Table;
// import jakarta.persistence.UniqueConstraint;
// import jakarta.validation.constraints.Email;
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
//  * Representa la cuenta principal de una persona en MealFlow.
//  *
//  * Una misma cuenta puede usar el perfil de consumidor, el perfil empresarial
//  * o ambos. La contraseña se almacena como hash; nunca debe guardarse en texto
//  * plano.
//  */
// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// @Entity
// @Table(
//         name = "usuarios",
//         uniqueConstraints = @UniqueConstraint(
//                 name = "uk_usuario_email",
//                 columnNames = "email"
//         )
// )
// public class Usuario {
//
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;
//
//     @NotBlank
//     @Size(max = 80)
//     @Column(nullable = false, length = 80)
//     private String nombres;
//
//     @NotBlank
//     @Size(max = 80)
//     @Column(nullable = false, length = 80)
//     private String apellidos;
//
//     @NotBlank
//     @Email
//     @Size(max = 150)
//     @Column(nullable = false, length = 150)
//     private String email;
//
//     @NotBlank
//     @Size(max = 255)
//     @Column(name = "password_hash", nullable = false, length = 255)
//     private String passwordHash;
//
//     @Size(max = 20)
//     @Column(length = 20)
//     private String telefono;
//
//     @Builder.Default
//     @Column(nullable = false)
//     private boolean activo = true;
//
//     @Builder.Default
//     @Column(name = "fecha_registro", nullable = false, updatable = false)
//     private LocalDateTime fechaRegistro = LocalDateTime.now();
//
//     /**
//      * Perfil habilitado cuando la persona compra packs sorpresa.
//      * Es opcional porque no toda cuenta necesariamente compra.
//      */
//     @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY)
//     private Consumidor consumidor;
//
//     /**
//      * Perfil habilitado cuando la persona registra y administra una empresa.
//      * No hereda de Usuario: ambos perfiles comparten esta misma cuenta.
//      */
//     @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY)
//     private PropietarioEmpresa propietarioEmpresa;
// }
