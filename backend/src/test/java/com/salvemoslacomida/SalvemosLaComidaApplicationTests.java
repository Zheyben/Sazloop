package com.salvemoslacomida;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba de arranque de la aplicación.
 *
 * @SpringBootTest → levanta el contexto completo de Spring Boot
 * para verificar que toda la configuración es correcta.
 *
 * contextLoads() no hace nada explícito: si Spring Boot logra
 * arrancar sin errores, la prueba pasa. Si hay algún problema
 * de configuración, falla aquí antes de llegar a producción.
 *
 * Esta es la prueba más básica y obligatoria de cualquier
 * proyecto Spring Boot.
 */
@SpringBootTest
class SalvemosLaComidaApplicationTests {

    @Test
    void contextLoads() {
        // Si Spring Boot arranca sin errores, esta prueba pasa.
        // No necesita código adicional.
    }
}
