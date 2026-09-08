// CÓDIGO TEMPORALMENTE DESACTIVADO PARA EL PRIMER AVANCE.
// Todas las líneas Java están comentadas para evitar dependencias cruzadas
// mientras el equipo termina los demás módulos. Al integrarlo, se debe
// retirar únicamente el primer prefijo "// " de cada línea.
// package com.mealflow.identidad.domain;
//
// import jakarta.persistence.CascadeType;
// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.FetchType;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.OneToMany;
// import jakarta.persistence.OneToOne;
// import jakarta.persistence.Table;
// import jakarta.persistence.UniqueConstraint;
// import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.Pattern;
// import jakarta.validation.constraints.Size;
// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.Setter;
//
// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;
//
// /**
//  * Representa la organización comercial afiliada a MealFlow.
//  *
//  * Una empresa puede agrupar varios establecimientos físicos. Así, el
//  * propietario consulta la información general de su negocio sin perder el
//  * control independiente de cada local.
//  */
// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// @Entity
// @Table(
//         name = "empresas",
//         uniqueConstraints = @UniqueConstraint(
//                 name = "uk_empresa_ruc",
//                 columnNames = "ruc"
//         )
// )
// public class Empresa {
//
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;
//
//     @NotBlank
//     @Size(max = 150)
//     @Column(name = "razon_social", nullable = false, length = 150)
//     private String razonSocial;
//
//     @NotBlank
//     @Size(max = 120)
//     @Column(name = "nombre_comercial", nullable = false, length = 120)
//     private String nombreComercial;
//
//     /**
//      * El RUC peruano contiene exactamente once dígitos.
//      */
//     @NotBlank
//     @Pattern(regexp = "\\d{11}")
//     @Column(nullable = false, length = 11)
//     private String ruc;
//
//     @OneToOne(fetch = FetchType.LAZY, optional = false)
//     @JoinColumn(
//             name = "propietario_id",
//             nullable = false,
//             unique = true
//     )
//     private PropietarioEmpresa propietario;
//
//     /**
//      * Locales físicos que pertenecen a la empresa.
//      * Cada establecimiento mantiene su propia aprobación y operación.
//      */
//     @Builder.Default
//     @OneToMany(
//             mappedBy = "empresa",
//             cascade = CascadeType.ALL,
//             orphanRemoval = true
//     )
//     private List<Establecimiento> establecimientos = new ArrayList<>();
//
//     @Builder.Default
//     @Column(nullable = false)
//     private boolean activa = true;
//
//     @Builder.Default
//     @Column(name = "fecha_registro", nullable = false, updatable = false)
//     private LocalDateTime fechaRegistro = LocalDateTime.now();
// }
