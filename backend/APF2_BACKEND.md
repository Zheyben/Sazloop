# Backend del APF2

## Requisitos implementados en esta rama

| RF | Operación comprobable |
| --- | --- |
| 01, 02 | Registro e inicio, consulta y cierre de sesión personal. |
| 04, 05 | Registro de empresa y establecimientos independientes. |
| 06 | Documentos privados por local y envío de la solicitud. |
| 07, 08 | Revisión manual de cada documento y aprobación o rechazo con motivo. |
| 10 | Cuenta compartida del local, creada solo después de la aprobación. |
| 11, 12, 13 | Descripción del pack y una oferta activa con variantes, stock y horario. |
| 14, 15 | Búsqueda filtrada y ficha pública de oferta con precios y advertencias. |
| 16, 20 | Compra remota con tarjeta/Yape simulados, descuento de stock, boleta simulada y código único. |
| 21 | Búsqueda del código por el personal y confirmación del recojo una sola vez. |

Se mantienen las 31 clases de dominio y las relaciones diseñadas en el APF1. Las operaciones se separan en controladores, servicios, repositorios y objetos de respuesta; las contraseñas se guardan codificadas y las respuestas nunca contienen el archivo privado ni su ruta.

## Ejecución y pruebas

Se requiere Java 21. Desde `backend`, ejecutar `mvnw.cmd test` y luego `mvnw.cmd spring-boot:run` en Windows. El perfil `dev` usa H2 temporal. `FlujoAfp2Tests` recorre afiliación, aprobación, cuenta operativa, publicación, compra, control de stock y recojo; `SolicitudAfiliacionServiceTests` comprueba acceso privado a documentos. Estas pruebas deben ejecutarse en un equipo con acceso a las dependencias Maven.

Para iniciar una cuenta administrativa interna en CMD, antes de arrancar la aplicación:

```bat
set SAZLOOP_ADMIN_EMAIL=admin@sazloop.test
set SAZLOOP_ADMIN_PASSWORD=una-clave-de-prueba-segura
mvnw.cmd spring-boot:run
```

El administrador ingresa por `POST /api/administradores/sesion`. No existe registro público para ese rol. Los endpoints siguientes usan la cookie de esa sesión:

- `GET /api/administradores/solicitudes`: solicitudes enviadas.
- `GET /api/administradores/solicitudes/{id}`: datos y documentos de una solicitud.
- `GET /api/administradores/documentos/{id}/archivo`: descarga privada.
- `POST /api/administradores/documentos/{id}/revision`: `{"resultado":"APROBADO","observaciones":"Legible"}` o `RECHAZADO` con observación; puede incluir `tipo`, `numero`, `emision` y `vencimiento` al transcribir el documento.
- `POST /api/administradores/solicitudes/{id}/decision`: `{"decision":"APROBADA","motivo":"Documentación revisada"}` o `RECHAZADA` con motivo.

Después de aprobar, el propietario usa `POST /api/establecimientos/{id}/cuenta` con `nombreUsuario` y `contrasena`. La cuenta entra por `POST /api/local/sesion` con los mismos campos. Puede registrar `POST /api/local/packs` y publicar `POST /api/local/ofertas`. Los consumidores consultan `GET /api/ofertas` (filtros: `distrito`, `direccion`, `precioMaximo`, `calificacionMinima`) y `GET /api/ofertas/{id}`; compran en `POST /api/compras` con `ofertaId`, `medioPago` (`YAPE` o `TARJETA_DEBITO`) y `lineas` (`varianteId`, `cantidad`). Consultan sus compras en `GET /api/compras/mias`. El personal consulta `GET /api/local/recojos/{codigo}` y confirma `POST /api/local/recojos/{codigo}/confirmar`.

El perfil MySQL se activa con `SPRING_PROFILES_ACTIVE=mysql`; la base `sazloop` debe existir y las credenciales pueden configurarse mediante `SAZLOOP_DB_USER` y `SAZLOOP_DB_PASSWORD`. Este perfil requiere una prueba de ejecución separada en un equipo con MySQL. Si ya existen datos en la base anterior `mealflow`, deben copiarse a `sazloop` antes de cambiar de base; modificar la URL no traslada los datos.

## Avance 2: vigencia, renovación, avisos e impacto

| RF | Operación comprobable |
| --- | --- |
| 09 | `VigenciaDocumentosService` revisa al iniciar y cada minuto: avisa 7 días antes, suspende al vencer y reactiva al aprobar la renovación. |
| 25 | `NotificacionService` recibe los eventos de afiliación, documentos, suspensión y pedidos; el recordatorio de recojo se crea al confirmar la compra. |
| 26 | Cada recojo (código o efectivo) guarda un `ImpactoCO2Evitado` sin cifras; el panel muestra packs entregados. |

Endpoints nuevos o ampliados:

- `GET /api/establecimientos/{id}/documentacion`: documentos en vigor con su vigencia, suspensión activa, tipos vencidos y renovables, y última solicitud.
- `POST /api/establecimientos/{id}/documentos`: en un local aprobado o suspendido, crea una renovación y solo acepta tipos vencidos o por vencer. `POST .../solicitud/enviar` exige, si el local está suspendido, todos los tipos vencidos.
- `GET /api/administradores/establecimientos`: estado de todos los locales y motivo de suspensión.
- `GET /api/administradores/solicitudes`: incluye `renovacion`, empresa, RUC, dirección y estado del local.
- `POST /api/notificaciones/leer-todas`: marca todos los avisos como leídos.
- `GET /api/local/recojos/{codigo}`: devuelve `cliente` y `lineas` (tamaño y cantidad a entregar).
- `GET /api/local/resumen`: incluye `estadoLocal` y `motivoSuspension`. La cuenta del local puede ingresar con el local suspendido para entregar reservas pagadas, pero no publicar.

**Dato pendiente del docente:** RF-06 habla de documentos obligatorios, pero el informe anterior no precisa una lista de tipos. Se permite adjuntar varios documentos y se exige al menos uno para enviar; el administrador decide manualmente su suficiencia. No se presupone una lista legal universal.
