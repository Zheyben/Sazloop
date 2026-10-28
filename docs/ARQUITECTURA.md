# Arquitectura de Sazloop

Este documento describe la organización implementada. El estado de las funciones y sus comprobaciones se mantiene en [Proyecto](PROYECTO.md); los acuerdos que debe respetar el desarrollo están en [Decisiones](decisiones/DECISIONES_PROYECTO.md).

## Aplicación y tecnologías

Hay un único backend Spring Boot y una base relacional compartida: un monolito organizado por áreas del negocio. El paquete base es `com.sazloop` y el artefacto Maven, `com.sazloop:sazloop-backend`.

| Elemento | Configuración actual |
|---|---|
| Java / Spring Boot | Java 21 / Spring Boot 4.1.1, declarados en el [POM](../backend/pom.xml) |
| Construcción | Maven; el wrapper configura 3.9.16 |
| Web y validación | Spring Web MVC y Spring Validation |
| Persistencia | Spring Data JPA; Hibernate gestiona las entidades |
| Bases de datos | MySQL para persistencia local; H2 en memoria para desarrollo y pruebas aisladas |
| Frontend | HTML, CSS propio y JavaScript con módulos ES; sin proceso de construcción de Node |
| Recursos externos | SweetAlert2 desde jsDelivr y Animate.css desde cdnjs, en las páginas que los incluyen |
| QR | `frontend/app/vendor/qrcode.mjs`, distribuido con su licencia MIT |

Tailwind se retiró del stack por decisión D6 / DEC-056. No se identificó una integración técnica que deba eliminarse. Las versiones de las dependencias de Java se administran desde Spring Boot; deben consultarse en Maven cuando se necesite el árbol resuelto.

El POM declara también MySQL Connector/J, H2, `spring-boot-h2console`, Lombok, DevTools y Starter Test. Web MVC incorpora Tomcat y Jackson; Data JPA incorpora Hibernate y el manejo de conexiones. No es necesario repetir todas las dependencias transitivas en este documento.

## Módulos y responsabilidades

| Paquete | Responsabilidad | Capas presentes | Responsable documentado |
|---|---|---|---|
| `identidad` | Personas, perfiles, empresas, locales, cuentas operativas y perfil personal | controller, service, repository, domain, dto | Emanuel |
| `afiliacion` | Adjuntos privados, solicitudes, revisión, resolución, vigencia y suspensión | Las cinco capas | Daniel |
| `ofertas` | Packs, variantes, publicación, búsqueda y panel del trabajador | Las cinco capas | Franco |
| `reservas` | Compra remota y presencial, retención, entrega, cancelación y vencimiento | Las cinco capas | Piero |
| `transacciones` | Modelo de pagos, boletas, reembolsos y comisiones; cálculo de comisión | domain y service | Alejandro |
| `experiencia` | Reseñas, notificaciones e impacto conceptual | Las cinco capas | Alejandro |
| `shared` | `SesionesPorRol`, compartido por los accesos | Sin subcapas | Revisión de Daniel y Franco |
| `demo` | Carga ficticia de H2 mediante `DatosDemoH2` | Configuración de demostración | Sin nueva asignación |

Las capas separan tareas: el controller recibe HTTP, el service coordina reglas, el repository accede a datos, domain contiene entidades y estados, y los DTO describen entradas y respuestas. Algunas respuestas están declaradas como `record` dentro de controladores o servicios. No se exige crear capas vacías.

Los pagos se coordinan desde servicios de Reservas; la ubicación de una entidad no determina por sí sola dónde se ejecuta un caso de uso. Los cambios que afecten a varios módulos deben coordinarse con sus responsables.

### Dependencias actuales y reglas de mantenimiento

El objetivo sigue siendo comunicar módulos mediante servicios claros, mantener las decisiones de negocio fuera de los controladores y ejecutar conjuntamente los cambios de compra, pago y stock. Actualmente existen accesos directos a repositorios ajenos: por ejemplo, `CompraRecojoService` utiliza repositorios de Identidad, Ofertas y Experiencia; `OfertaService` consulta Identidad y Reseñas. Son dependencias existentes que deben evaluarse antes de refactorizar, no una autorización para ampliarlas sin criterio.

`shared` no contiene hoy un sistema general de autorización ni un manejador global propio de excepciones, aunque sus comentarios históricos mencionen esos usos posibles. Las comprobaciones de acceso se realizan en los componentes de sesión y servicios correspondientes.

## Frontend e integración

Maven copia los recursos seleccionados de `frontend/` a `static` al construir el backend. La portada y los portales se sirven desde el mismo origen que `/api`; no hay un servidor frontend independiente configurado.

| Área | Archivos principales |
|---|---|
| Portada y selección de acceso | `frontend/index.html`, `js/acceso.js`, `app/index.html` |
| Registro e ingreso | `app/acceso-*.html`, `accesos.js` |
| Empresa, trabajador y administrador | Sus `.html` y `.js`; comparten `panel.js` y `panel.css` |
| Consumidor | `portal-main.js` monta Inicio, Catálogo, Compras, Perfil y Notificaciones; `portal.js` comparte navegación y carrito |
| Ofertas y compra | `ofertas-portal.js`, `compras.js`, `qr.js` y `resenas-portal.js` |
| Solicitudes HTTP | `comun.js` antepone `/api`, envía cookies y trata respuestas JSON; `accesos.js` hace sus propias solicitudes |

El carrito vive en `sessionStorage`, separado por usuario; no es una reserva ni retiene stock. `portal-main.js` puede cambiar el contenido principal sin recargar toda la página. La compra por QR usa `qr.html?oferta=...&token=...`; tanto el identificador como el token pertenecen a una oferta.

Los errores de negocio útiles se muestran desde `comun.js`; los errores de servidor reciben un mensaje general y los accesos usan mensajes propios. El backend configura la inclusión del motivo y la exclusión de la traza. No debe suponerse que todas las pantallas muestran exactamente el mismo detalle.

## Mapa de la API

Son rutas actuales, no una lista de funciones futuras. Todas llevan el prefijo `/api`. Los contratos exactos están en los [controladores y DTO](../backend/src/main/java/com/sazloop/).

| Área | Operaciones principales | Acceso |
|---|---|---|
| Persona | `POST /usuarios`; `POST/GET/DELETE /sesion` | Registro público; después sesión de persona |
| Perfil | `PUT /perfil`, `PUT /perfil/contrasena`, `POST/GET /perfil/foto` | Persona |
| Empresa | `POST /empresas`; `GET /empresas/actual`; `GET /empresas/actual/indicadores`; `POST/GET /empresas/actual/establecimientos` | Persona propietaria |
| Afiliación | Bajo `/establecimientos/{localId}`: `GET /solicitud`, `GET /documentacion`, `POST /documentos`, `POST /solicitud/enviar`, `GET /documentos/{documentoId}/archivo` | Propietario del local |
| Cuenta de local | `POST /establecimientos/{id}/cuenta`; `POST/GET/DELETE /local/sesion` | Propietario para crear; cuenta operativa para ingresar |
| Administración | `POST/GET/DELETE /administradores/sesion`; consultas de solicitudes, documentos y establecimientos | Administrador |
| Resolución vigente en pantalla | `POST /administradores/solicitudes/{id}/resolucion` | Administrador |
| Operaciones anteriores conservadas | `POST /administradores/documentos/{id}/revision`; `POST /administradores/solicitudes/{id}/decision` | Administrador; detalle debajo |
| Publicación y panel | `GET/POST /local/packs`, `POST /local/publicaciones`, `POST /local/ofertas`, `GET /local/resumen` | Cuenta operativa |
| Catálogo | `GET /ofertas`, `GET /ofertas/{id}` | Consulta pública de API; el portal exige sesión |
| Compra remota | `POST /compras`, `POST /compras/lote`, `GET /compras/mias` | Persona |
| Compra presencial | `POST /compras/presenciales`, `POST /compras/presenciales/efectivo` | Persona y token de la oferta |
| Entrega y efectivo | `GET /local/recojos/{codigo}`, `POST /local/recojos/{codigo}/confirmar`, `POST /local/efectivo/{codigo}/confirmar` | Cuenta del local correspondiente |
| Cancelación y reseña | `POST /compras/{reservaId}/cancelar`, `POST /compras/{reservaId}/resena`, `POST /compras/{reservaId}/resena/omitir` | Consumidor de la compra |
| Notificaciones | `GET /notificaciones`; `POST /notificaciones/leer-todas`; `POST /notificaciones/{id}/leer` | Persona destinataria |

En administración, `administrador.js` llama a `/resolucion`: presenta datos de solo lectura y envía resultados y observaciones juntos. Las rutas anteriores siguen permitiendo revisar por separado y, mediante `RevisionRequest`, cambiar tipo, número y fechas. Las pruebas existentes y `DatosDemoH2` invocan directamente servicios de ese recorrido; no se acreditó un inventario de clientes HTTP externos. D5 / DEC-055 exige conservarlas temporalmente; su eliminación necesita evaluación de compatibilidad.

## Persistencia y entornos

Las [31 entidades](decisiones/MODELO_DOMINIO.md) y dos asociaciones muchos a muchos describen un esquema de 33 tablas de aplicación. Esto no acredita su coincidencia con diagramas externos.

| Perfil | Datos y esquema | Uso |
|---|---|---|
| `mysql` | Base `sazloop`, `ddl-auto=update`; conserva datos | Perfil activo por defecto en `application.properties` |
| `dev` | H2 en memoria, `ddl-auto=create-drop`; consola H2 habilitada | Desarrollo temporal |
| `dev,demo` | H2 y carga ficticia de `DatosDemoH2`; archivos demo bajo `target` | Demostración; el cargador requiere ambos perfiles |

`spring.jpa.open-in-view=false`: las lecturas de relaciones necesarias para una respuesta deben completarse dentro del servicio. `@Transactional` agrupa operaciones; los repositorios bloquean ofertas, reservas, códigos o locales en los puntos previstos. Estos controles no sustituyen pruebas de concurrencia real ni acreditan MySQL por haber probado H2.

Los documentos privados se guardan por defecto en `data/documentos`, fuera de los recursos web; `sazloop.documentos.directorio` permite elegir otra ruta. Se admiten archivos no vacíos de hasta 10 MB y se reconocen cabeceras de PDF, PNG y JPG. Los propietarios solo descargan documentos de su local y los administradores usan sus rutas autorizadas. Esto no equivale a validar el contenido legal del archivo. La foto de perfil se guarda en la entidad Usuario, admite PNG/JPG y tiene un límite propio de 2 MB.

## Acceso, tareas automáticas y límites

- Las contraseñas se derivan con PBKDF2-HMAC-SHA256, sal individual y 210 000 iteraciones en `CodificadorContrasena`.
- Una sesión HTTP guarda atributos independientes para persona, local y administrador. `SesionesPorRol` cambia el identificador al ingresar y solo invalida toda la sesión cuando no quedan roles conectados. Las cookies se configuran como `HttpOnly` y `SameSite=Lax`.
- La cuenta del local suspendido sigue permitiendo entregar compras pagadas; la búsqueda y nuevas compras/publicaciones se restringen por el estado del local.
- La provisión administrativa normal usa propiedades y solo crea una cuenta si su correo no existe. En demo se utiliza el cargador ficticio. No hay registro público de administradores.
- Vencimiento de reservas y liberación de retenciones: revisión cada 60 segundos. Vigencia documental: primera revisión a los 15 segundos y después cada 60 segundos, configurables. El cambio de estado puede producirse en la siguiente revisión; no implica una ejecución exacta al segundo del vencimiento.
- Las fechas de vigencia y los períodos del panel usan `America/Lima`; varias marcas de tiempo usan la zona de la JVM y el navegador interpreta fechas sin zona. Para pruebas se debe fijar la zona de Perú; la uniformidad entre zonas sigue por verificar.

Debe mantenerse la regla de no versionar secretos. La configuración actual contiene credenciales literales de MySQL y administración; su retirada o rotación está pendiente y no se reproduce aquí ningún valor. Tampoco debe interpretarse este diseño local como una validación completa de seguridad. Los hallazgos y la evidencia de ejecución están en [Auditorías](../auditoria/).
