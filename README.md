# Sazloop

> **Lo bueno sigue fluyendo.**

Proyecto final del curso Desarrollo Web Integrado.

**Proyecto asignado:** N.º 91 — SalvemosLaComida. **Nombre de la plataforma:** Sazloop.

El repositorio conserva su URL histórica `Zheyben/MealFlow`.

Sazloop es una plataforma web mobile-first para reducir el desperdicio alimentario. Los establecimientos afiliados ofrecerán sus excedentes mediante packs sorpresa con descuento y los consumidores los recogerán presencialmente.

## Empieza aquí

Toda la información necesaria para comprender y continuar el proyecto se encuentra en la [portada de documentación](docs/README.md).

Antes de comenzar una tarea:

1. Lee la visión general y el glosario.
2. Revisa el estado actual.
3. Consulta los pendientes vigentes; los planes de las semanas 4 y 5 son históricos.
4. Consulta las decisiones, los requisitos y la [revisión del informe corregido](docs/informe/REVISION_APF1.md).
5. Sigue la [guía de colaboración](CONTRIBUTING.md).

## Estado

Primer avance presentado; informe corregido disponible en `docs/informe/Informe_Primer_Avance.docx`. La revisión del 26 de septiembre de 2026 identifica observaciones pendientes hasta Referencias bibliográficas.

El backend contiene 31 entidades JPA y configuración inicial. El frontend aún no contiene una implementación; las pantallas del informe son prototipos. No se considera implementado un RF por aparecer en un diagrama.

Consulta el progreso y los pendientes en [docs/tareas/ESTADO_ACTUAL.md](docs/tareas/ESTADO_ACTUAL.md).

## Función principal

- Los establecimientos publican una oferta activa con variantes de tamaño, precio y stock.
- Los consumidores buscan, compran mediante pagos simulados y recogen el pack en el local.
- La plataforma no realiza delivery.
- Cada establecimiento es revisado y aprobado de forma independiente.

El alcance completo se encuentra en [docs/PROYECTO.md](docs/PROYECTO.md).

## Tecnologías

- Java 21
- Spring Boot 4.1.1 (declarado en el POM)
- Maven
- Spring Data JPA
- MySQL; H2 para desarrollo local
- HTML, CSS, JavaScript y Tailwind CSS previstos para el frontend

## Arquitectura

Monolito modular organizado por capas. Consulta [docs/ARQUITECTURA.md](docs/ARQUITECTURA.md).

## Estructura

- `backend/`: aplicación Spring Boot.
- `frontend/`: ubicación prevista de la interfaz web; actualmente solo contiene `.gitkeep`.
- `docs/`: conocimiento, decisiones y entregables del proyecto.

## Equipo

- Emanuel
- Daniel
- Franco
- Piero
- Alejandro

## Ejecutar el backend

Requiere JDK 21 y acceso a Maven Central. Desde `backend/`:

```bash
sh mvnw test
sh mvnw spring-boot:run
```

En Windows: `mvnw.cmd test` y `mvnw.cmd spring-boot:run`.

La clase de entrada es `com.sazloop.SazloopApplication`. El perfil por defecto es `dev`, con H2 en memoria (`jdbc:h2:mem:sazloop`). Para MySQL, configura el perfil `mysql`; el esquema local conserva el nombre técnico `mealflow` para no requerir una migración por el cambio de marca.

La verificación de esta actualización incluyó rutas, imports y mapeos estáticos. La ejecución de Maven quedó bloqueada por resolución DNS de Maven Central; no se afirma que las pruebas hayan pasado.
