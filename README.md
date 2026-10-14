# Sazloop

**Lo bueno sigue fluyendo.** Proyecto académico de Desarrollo Web Integrado basado en el tema N.º 91, SalvemosLaComida. Los establecimientos ofrecen excedentes de comida aptos para consumo en packs sorpresa con descuento. Esta versión permite comprar y recoger en el local; la entrega por delivery aún no está implementada en el backend.

## Requisitos para una computadora nueva

- Windows con **JDK 21**.
- **Git** para descargar el repositorio y conexión a internet en el primer inicio para que el proyecto descargue sus dependencias.
- Para **MySQL**, un servidor MySQL 8.

En CMD, descarga el proyecto y entra en la carpeta correcta:

```bat
git clone https://github.com/Zheyben/Sazloop.git
cd Sazloop\backend
java -version
```

Si ya tienes el repositorio, actualízalo con `git pull origin main` desde la carpeta `Sazloop` y luego entra a `backend`.

**Tambien puedes descargar el código por .zip y ahorrarte lo anterior.**

## Opción A: H2 con datos
Creado para ver una demostración de las cuentas ya registradas para todos los portales y evitar crear cuentas uno por uno. Esto se realizó para probar la plataforma y las funcionalidades que trae.

Desde la ruta `C:\Users\Emanuel\Documents\Proyectos\Sazloop\backend` abrimos el **CMD** y ejecutamos el siguiente comando:

```bat
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev,demo
```

Cuando aparezca `Tomcat started on port 8080`, abre [http://localhost:8080/](http://localhost:8080/). El sitio público y los portales se abren desde esa misma dirección.
Utiliza los siguientes accesos para ingresar a las cuentas:

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

## Opción B: MySQL local

Inicia MySQL y ejecuta una sola vez en MySQL Workbench o en la consola de MySQL:

```sql
CREATE DATABASE IF NOT EXISTS sazloop;
```

Debes modificar en `Sazloop\backend\src\main\resources\application-mysql.properties` la contraseña que esta escrita por tu contraseña root de tu mysql:

```java
spring.datasource.username=root
spring.datasource.password=CONTRASEÑA <--- Cambia esta contraseña
```

Ejecuta el proyecto desde IntelliJ IDEA, dandole RUN en el archivo de SazloopApplication.

Para ingresar al administrador debes ingresar estas credenciales:
- Email: admin@correo.com
- Contraseña: admin123

Abre [http://localhost:8080/](http://localhost:8080/) cuando arranque Tomcat. Las tablas se crean o actualizan automáticamente. **MySQL conserva sus datos** al detener la aplicación, pero una instalación nueva no recibe las cuentas de H2 ni las de otra computadora. La cuenta administradora se crea solamente si el correo indicado todavía no existe en esa base; cambiar la variable después no modifica su contraseña.
