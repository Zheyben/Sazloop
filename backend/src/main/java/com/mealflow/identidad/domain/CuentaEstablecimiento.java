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
// import jakarta.persistence.UniqueConstraint;
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
//  * Cuenta operativa compartida por el personal de un establecimiento aprobado.
//  *
//  * Sirve para administrar el pack activo, comprobar pagos presenciales y
//  * confirmar recojos. No se modelan empleados individuales en esta primera
//  * versión.
//  */
// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// @Entity
// @Table(
//         name = "cuentas_establecimiento",
//         uniqueConstraints = @UniqueConstraint(
//                 name = "uk_cuenta_establecimiento_usuario",
//                 columnNames = "nombre_usuario"
//         )
// )
// public class CuentaEstablecimiento {
//
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;
//
//     /**
//      * Cada cuenta operativa pertenece a un solo local y cada local tiene como
//      * máximo una cuenta operativa en esta versión.
//      */
//     @OneToOne(fetch = FetchType.LAZY, optional = false)
//     @JoinColumn(
//             name = "establecimiento_id",
//             nullable = false,
//             unique = true
//     )
//     private Establecimiento establecimiento;
//
//     @NotBlank
//     @Size(max = 80)
//     @Column(name = "nombre_usuario", nullable = false, length = 80)
//     private String nombreUsuario;
//
//     @NotBlank
//     @Size(max = 255)
//     @Column(name = "password_hash", nullable = false, length = 255)
//     private String passwordHash;
//
//     @Builder.Default
//     @Column(nullable = false)
//     private boolean activa = true;
//
//     @Builder.Default
//     @Column(name = "fecha_creacion", nullable = false, updatable = false)
//     private LocalDateTime fechaCreacion = LocalDateTime.now();
//
//     @Column(name = "ultimo_acceso")
//     private LocalDateTime ultimoAcceso;
// }
