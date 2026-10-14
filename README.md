# Sazloop

> **Lo bueno sigue fluyendo.**

Proyecto académico del curso **Desarrollo Web Integrado**, basado en el tema asignado **N.º 91 — SalvemosLaComida**.

Sazloop es una plataforma web para que establecimientos de comida ofrezcan excedentes aptos para consumo en packs sorpresa con descuento. Los consumidores pueden explorar ofertas, comprarlas y recoger sus packs en el establecimiento.

## Estado del proyecto

El repositorio contiene el avance 2 en desarrollo. El sitio público, los portales por rol y el backend comparten un mismo proyecto; la interfaz se sirve desde Spring Boot.

El código actual permite:

- **Consumidor:** registrarse e iniciar sesión, explorar y filtrar ofertas, consultar sus detalles, agregar packs al carrito, realizar compras con pagos simulados, consultar compras y dejar reseñas después del recojo.
- **Empresa:** registrar su empresa y establecimientos, enviar documentos de afiliación, consultar el estado de sus locales y solicitar renovaciones.
- **Trabajador del local:** gestionar packs, publicar ofertas con tamaños, precios, existencias y horario, y validar entregas mediante códigos de recojo. También se contempla la compra presencial con pago digital simulado o efectivo.
- **Administración:** revisar documentos y solicitudes, aprobar o rechazar establecimientos y consultar su estado.

La **entrega por delivery no está implementada en el backend**. Aunque el sitio público la menciona, las compras operativas de esta versión terminan con el recojo en el establecimiento. No se debe presentar la integración con empresas de reparto como una función ya comprobada.

## Tecnologías y estructura

- **Backend:** Java 21, Spring Boot 4.1.1, Maven, API REST, Spring Data JPA e Hibernate; Tomcat viene integrado.
- **Base de datos:** MySQL para datos persistentes y H2 en memoria para pruebas locales.
- **Frontend:** HTML, CSS y JavaScript. Maven copia `frontend/` a los recursos estáticos servidos por el backend; no se necesita iniciar un servidor de frontend aparte.

```text
backend/   Aplicación Java, configuración y pruebas
frontend/  Sitio público y portales de consumidor, empresa, trabajador y administración
```

## Equipo

Emanuel, Daniel, Franco, Piero y Alejandro.

## Ejecutar en Windows (CMD)

Se requiere **JDK 21**. La primera ejecución del wrapper de Maven necesita acceso a internet para descargar sus dependencias. Abre CMD en la carpeta del repositorio y entra a `backend`:

```bat
cd backend
java -version
```

### Opción 1: probar sin MySQL

H2 guarda los datos temporalmente: al detener la aplicación se pierden.

```bat
set "SPRING_PROFILES_ACTIVE=dev"
mvnw.cmd test
mvnw.cmd spring-boot:run
```

### Opción 2: usar MySQL

Inicia MySQL y crea la base de datos una sola vez:

```sql
CREATE DATABASE IF NOT EXISTS sazloop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

En CMD, dentro de `backend`, utiliza las credenciales **de tu instalación local**:

```bat
set "SPRING_PROFILES_ACTIVE=mysql"
set "SPRING_DATASOURCE_USERNAME=tu_usuario_mysql"
set "SPRING_DATASOURCE_PASSWORD=tu_clave_mysql"
mvnw.cmd spring-boot:run
```

El perfil configurado por defecto en `application.properties` es `mysql`; las variables anteriores permiten escoger el perfil y las credenciales sin editar archivos para cada ejecución. Al iniciar, Hibernate crea o actualiza las tablas, pero **no traslada datos de otras bases**.

Cuando la consola muestre que Tomcat arrancó en el puerto 8080, abre **http://localhost:8080/**. En «Iniciar sesión» puedes elegir el acceso correspondiente. Para ver cambios hechos en `frontend/`, reinicia la aplicación y recarga el navegador.

### Cuenta de administración local

Antes de arrancar el servidor, puedes definir el correo y la contraseña de la cuenta administrativa mediante variables de entorno de esa ventana de CMD:

```bat
set "SAZLOOP_ADMIN_EMAIL=tu_correo_admin"
set "SAZLOOP_ADMIN_PASSWORD=tu_clave_admin"
```

La cuenta se crea **solo si ese correo todavía no existe** en la base de datos activa. Cambiar estas variables más tarde no modifica la contraseña de una cuenta existente. No subas credenciales reales al repositorio.

## Pruebas

Para ejecutar las pruebas automatizadas con la base temporal H2 desde `backend/`:

```bat
set "SPRING_PROFILES_ACTIVE=dev"
mvnw.cmd test
```

Las pruebas cubren flujos de afiliación, ofertas, compra, recojo y consultas de los portales. El resultado de `mvnw.cmd test` corresponde a tu ejecución local.

Para la exposición con tres empresas, cuatro locales, dos consumidores y tres ofertas precargadas en H2, consulta la [guía de demostración](backend/DEMO_H2.md).
