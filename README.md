# Sazloop

> **Lo bueno sigue fluyendo.**

Proyecto final del curso Desarrollo Web Integrado.

**Proyecto asignado:** N.º 91 — SalvemosLaComida. **Nombre de la plataforma:** Sazloop.

El repositorio está disponible en `Zheyben/Sazloop`.

Sazloop es una plataforma web mobile-first para reducir el desperdicio alimentario. Los establecimientos afiliados ofrecerán sus excedentes mediante packs sorpresa con descuento y los consumidores los recogerán presencialmente.

## Empieza aquí

Toda la información necesaria para comprender y continuar el proyecto se encuentra en la [portada de documentación](docs/README.md).

Antes de comenzar una tarea:

1. Lee la visión general y el glosario.
2. Revisa el estado actual.
3. Consulta las decisiones, los requisitos y el [estado actual](docs/tareas/ESTADO_ACTUAL.md).
4. Sigue la [guía de colaboración](CONTRIBUTING.md). Los planes de las semanas 4 y 5 son históricos.

## Estado

**Avance 2.** Los 26 requisitos funcionales tienen backend y pantallas conectados: sitio público, portal del consumidor, portal del trabajador, portal de empresa y portal de administración. Las reglas aprobadas están en el anexo del [informe del segundo avance](docs/informe/Avance_del_proyecto_02.docx).

Consulta el estado por requisito y cómo se comprobó en [docs/tareas/ESTADO_ACTUAL.md](docs/tareas/ESTADO_ACTUAL.md).

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
- HTML, CSS y JavaScript (módulos ES) en el frontend, servidos por el mismo backend

## Arquitectura

Monolito modular organizado por capas. Consulta [docs/ARQUITECTURA.md](docs/ARQUITECTURA.md).

## Estructura

- `backend/`: aplicación Spring Boot.
- `frontend/`: sitio público (`index.html`) y aplicación web (`app/`): accesos, portal del consumidor (`consumidor.html`), del trabajador (`trabajador.html`), de empresa (`empresa.html`) y de administración (`administrador.html`). Maven los copia como contenido estático del backend.
- `docs/`: conocimiento, decisiones y entregables del proyecto.

## Equipo

- Emanuel
- Daniel
- Franco
- Piero
- Alejandro

## Ejecutar la plataforma

Requiere JDK 21 y acceso a Maven Central (no hace falta instalar Tomcat: viene incluido). Desde `backend/`, en Windows (CMD):

```bat
mvnw.cmd test
set SAZLOOP_ADMIN_EMAIL=admin@sazloop.test
set SAZLOOP_ADMIN_PASSWORD=una-clave-de-prueba
mvnw.cmd spring-boot:run
```

Luego abre `http://localhost:8080/`. El acceso de cada rol está en «Iniciar sesión»; el administrador usa el correo y la clave de las variables anteriores, que solo crean la cuenta si aún no existe.

- **Perfil `dev` (por defecto):** H2 en memoria; los datos se borran al detener la aplicación. Consola de la base en `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:sazloop`, usuario `sa`, sin contraseña).
- **Perfil `mysql`:** crea la base `sazloop` en MySQL 8 y ejecuta `mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql`, con `SAZLOOP_DB_USER` y `SAZLOOP_DB_PASSWORD` si no usas `root` sin contraseña. Hibernate crea las tablas al iniciar.

Las tareas programadas revisan cada minuto las reservas no recogidas, las retenciones de efectivo y la vigencia de los documentos. Los cambios en `frontend/` se ven después de reiniciar la aplicación.

