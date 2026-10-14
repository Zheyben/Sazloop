# Sazloop

**Lo bueno sigue fluyendo.** Proyecto académico de Desarrollo Web Integrado basado en el tema N.º 91, SalvemosLaComida. Los establecimientos ofrecen excedentes de comida aptos para consumo en packs sorpresa con descuento. Esta versión permite comprar y recoger en el local; la entrega por delivery aún no está implementada en el backend.

## Requisitos para una computadora nueva

- Windows con **JDK 21** instalado (`java -version` debe mostrar 21).
- **Git** para descargar el repositorio y conexión a internet en el primer inicio para que el proyecto descargue sus dependencias.
- Para **MySQL**, un servidor MySQL 8 en ejecución en `localhost:3306`. **Para H2 no se instala ningún servidor de base de datos.**
- No es necesario instalar Maven ni Node.js: se usa `mvnw.cmd` y Spring Boot sirve el frontend desde `frontend/` junto con el backend.

En CMD, descarga el proyecto y entra en la carpeta correcta:

```bat
git clone https://github.com/Zheyben/Sazloop.git
cd Sazloop\backend
java -version
```

Si ya tienes el repositorio, actualízalo con `git pull origin main` desde la carpeta `Sazloop` y luego entra a `backend`.

## Opción A: H2 con datos para la exposición

Desde `backend` en **CMD**:

```bat
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev,demo
```

Cuando aparezca `Tomcat started on port 8080`, abre [http://localhost:8080/](http://localhost:8080/). El sitio público y los portales se abren desde esa misma dirección.

| Acceso | Establecimiento o persona | Usuario o correo | Contraseña |
| --- | --- | --- | --- |
| Administrador | Administración de prueba | `demo.admin@sazloop.test` | `SazloopDemo2026!` |
| Empresa | Aurora Cocina (Miraflores y San Borja) | `duena.aurora@sazloop.test` | `SazloopDemo2026!` |
| Empresa | Oriente Pan (Ate) | `dueno.oriente@sazloop.test` | `SazloopDemo2026!` |
| Empresa | Sazón Urbano (Ate) | `duena.sazon@sazloop.test` | `SazloopDemo2026!` |
| Trabajador | Aurora Miraflores | `demo.miraflores` | `SazloopDemo2026!` |
| Trabajador | Oriente Pan Ate | `demo.ayllon1` | `SazloopDemo2026!` |
| Trabajador | Sazón Urbano Ate | `demo.ayllon2` | `SazloopDemo2026!` |
| Consumidor | Valeria Quispe | `demo.consumidor1@sazloop.test` | `SazloopDemo2026!` |
| Consumidor | Diego Flores | `demo.consumidor2@sazloop.test` | `SazloopDemo2026!` |

La demo carga **tres empresas, cuatro establecimientos, dos consumidores y tres ofertas**, una por cada establecimiento activo. Aurora San Borja queda pendiente de revisión administrativa, sin cuenta de trabajador ni oferta hasta que se apruebe.

H2 funciona en memoria: al detener el programa con `Ctrl + C` se pierden los cambios y, al volver a iniciarlo con `dev,demo`, se cargan otra vez los datos iniciales. **El perfil `dev` por sí solo inicia H2 vacía**, sin estas cuentas. Los datos de H2 no se copian a MySQL ni se comparten entre computadoras. La consola para consultar sus tablas está en [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:sazloop`; usuario: `sa`; contraseña: vacía).

## Opción B: MySQL local

Inicia MySQL y ejecuta una sola vez en MySQL Workbench o en la consola de MySQL:

```sql
CREATE DATABASE IF NOT EXISTS sazloop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

El proyecto espera MySQL en `localhost:3306`. Desde `backend` en **CMD**, configura las credenciales **de MySQL de esa computadora** y una cuenta administradora nueva, luego inicia:

```bat
set "SPRING_DATASOURCE_USERNAME=tu_usuario_mysql"
set "SPRING_DATASOURCE_PASSWORD=tu_clave_mysql"
set "SAZLOOP_ADMIN_EMAIL=tu_correo_admin"
set "SAZLOOP_ADMIN_PASSWORD=tu_clave_admin"
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
```

Sustituye los valores `tu_...` por los tuyos en esa ventana de CMD; no publiques contraseñas reales en Git. Abre [http://localhost:8080/](http://localhost:8080/) cuando arranque Tomcat. Las tablas se crean o actualizan automáticamente. **MySQL conserva sus datos** al detener la aplicación, pero una instalación nueva no recibe las cuentas de H2 ni las de otra computadora. La cuenta administradora se crea solamente si el correo indicado todavía no existe en esa base; cambiar la variable después no modifica su contraseña.

## Pruebas automatizadas

Desde `backend` con JDK 21:

```bat
set "SPRING_PROFILES_ACTIVE=dev"
mvnw.cmd test
```

Si necesitas más detalle sobre los datos de exposición, consulta [la demostración H2](backend/DEMO_H2.md) y [sus accesos](backend/ACCESOS_H2.md).
