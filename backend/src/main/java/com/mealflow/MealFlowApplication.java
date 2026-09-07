package com.mealflow;

// =========================================================
// IMPORTACIONES
// Spring Boot necesita estas dos clases para arrancar.
// =========================================================

// SpringApplication: la clase que pone en marcha el contexto
// de Spring (el "contenedor" que gestiona todos los objetos)
import org.springframework.boot.SpringApplication;

// @SpringBootApplication: la anotación que activa tres cosas
// a la vez (ver explicación abajo)
import org.springframework.boot.autoconfigure.SpringBootApplication;

// =========================================================
// @SpringBootApplication
// Esta anotación es un atajo que combina tres anotaciones:
//
//   1. @Configuration
//      Indica que esta clase puede definir "beans" (objetos
//      que Spring gestiona en su contenedor).
//
//   2. @EnableAutoConfiguration
//      Le dice a Spring Boot que configure automáticamente
//      los componentes según las dependencias del pom.xml.
//      Por ejemplo: detecta JPA y la base de datos disponible, y

//      configura Hibernate y el DataSource automáticamente.
//
//   3. @ComponentScan
//      Le dice a Spring que busque clases anotadas con
//      @Controller, @Service, @Repository, @Component, etc.
//      en este paquete y en todos sus subpaquetes.
//      Por eso TODA la estructura del proyecto vive dentro
//      de com.mealflow: Spring las encuentra automáticamente.
// =========================================================
@SpringBootApplication
public class MealFlowApplication {

    // =========================================================
    // MÉTODO MAIN
    // El punto de entrada de cualquier programa Java.
    // Cuando ejecutas el .jar o presionas "Run" en IntelliJ,
    // Java busca este método y empieza aquí.
    //
    // SpringApplication.run() hace todo el trabajo pesado:
    //   - Crea el contexto de Spring (el contenedor de objetos)
    //   - Configura automáticamente JPA, Tomcat, etc.
    //   - Arranca el servidor web Tomcat en el puerto 8080
    //   - Registra todos los controllers, services, repos, etc.
    //
    // args → los argumentos de línea de comandos (raramente
    // los usaremos, pero el método los necesita por convenio)
    // =========================================================
    public static void main(String[] args) {
        SpringApplication.run(MealFlowApplication.class, args);
    }

}
