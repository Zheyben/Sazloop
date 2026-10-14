# Datos para la demostración con H2

Desde la carpeta `backend` en CMD, inicia el proyecto así:

```bat
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev,demo
```

Abre `http://localhost:8080/`. El perfil `dev` usa H2 temporal y `demo` carga los datos ficticios en cada arranque. El comando habitual con solo `dev` continúa sin datos precargados. **MySQL no participa en esta demostración.**

## Accesos de prueba

La contraseña de **todas** estas cuentas ficticias es `SazloopDemo2026!`. Solo funcionan en el H2 local de demostración.

| Portal | Usuario o correo |
| --- | --- |
| Administrador | `demo.admin@sazloop.test` |
| Empresa Aurora Cocina | `duena.aurora@sazloop.test` |
| Empresa Oriente Pan | `dueno.oriente@sazloop.test` |
| Empresa Sazón Urbano | `duena.sazon@sazloop.test` |
| Trabajador de Miraflores | `demo.miraflores` |
| Trabajador de Av. Nicolás Ayllón 5000 | `demo.ayllon1` |
| Trabajador de Av. Nicolás Ayllón 6000 | `demo.ayllon2` |
| Consumidor 1 | `demo.consumidor1@sazloop.test` |
| Consumidor 2 | `demo.consumidor2@sazloop.test` |

## Lo que verá el profesor

| Empresa ficticia | Establecimiento | Estado inicial | Oferta |
| --- | --- | --- | --- |
| Aurora Cocina | Aurora San Borja | Pendiente de revisión | Ninguna |
| Aurora Cocina | Aurora Miraflores | Aprobado | Panes del Día |
| Oriente Pan | Oriente Pan Ate, Av. Nicolás Ayllón 5000 | Aprobado | Merienda Sorpresa |
| Sazón Urbano | Sazón Urbano Ate, Av. Nicolás Ayllón 6000 | Aprobado | Almuerzo del Día |

Cada local tiene adjunto `PRUEBA.pdf` como **otro documento de prueba**. No representa una licencia real. Las aprobaciones de los tres locales activos también son simuladas. El administrador puede revisar la solicitud de San Borja y aprobarla durante la demostración. Después, la propietaria de Aurora Cocina puede crear desde su portal la cuenta de trabajador de ese local; antes de la aprobación el sistema no lo permite.

Las tres ofertas se publican con un horario que empieza al iniciar la aplicación y termina ocho horas después. Si el horario se vence o se detiene el programa, reinícialo: H2 vuelve a empezar vacío y la carga crea nuevamente los datos. Las direcciones y empresas son **ilustrativas**; no corresponden necesariamente a negocios existentes.
