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
// /**
//  * Agrupa los datos de ubicación textual de un establecimiento.
//  *
//  * Se modela como entidad separada para mantener ordenados los datos de
//  * departamento, provincia, distrito, vía y referencia. En el primer avance no
//  * incluye coordenadas ni integración con servicios de mapas.
//  */
// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// @Entity
// @Table(name = "direcciones")
// public class Direccion {
//
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;
//
//     @NotBlank
//     @Size(max = 80)
//     @Column(nullable = false, length = 80)
//     private String departamento;
//
//     @NotBlank
//     @Size(max = 80)
//     @Column(nullable = false, length = 80)
//     private String provincia;
//
//     @NotBlank
//     @Size(max = 80)
//     @Column(nullable = false, length = 80)
//     private String distrito;
//
//     @NotBlank
//     @Size(max = 180)
//     @Column(name = "direccion_linea", nullable = false, length = 180)
//     private String direccionLinea;
//
//     @Size(max = 20)
//     @Column(length = 20)
//     private String numero;
//
//     @Size(max = 250)
//     @Column(length = 250)
//     private String referencia;
//
//     @Size(max = 10)
//     @Column(name = "codigo_postal", length = 10)
//     private String codigoPostal;
//
//     /**
//      * Relación inversa: Establecimiento conserva la clave de la dirección.
//      */
//     @OneToOne(mappedBy = "direccion", fetch = FetchType.LAZY)
//     private Establecimiento establecimiento;
// }
