# Decisiones del proyecto

Este documento registra las decisiones aprobadas durante el desarrollo del proyecto y la justificación necesaria para comprenderlas y sustentarlas.

## Aclaraciones académicas confirmadas

- El segundo avance se entregará en la semana 10.
- La indicación de semana 14 presente en el PDF corresponde a un error confirmado.
- El PDF contiene los requisitos académicos generales.
- La plantilla DOCX entregada posteriormente será la base estructural y visual del informe.

## DEC-001 — Pagos simulados en V1
El sistema modelará pagos y estados, pero no moverá dinero real.

## DEC-002 — Ubicación textual en V1
Filtrado por datos como distrito/dirección. No GPS ni mapas inicialmente.

## DEC-003 — Web responsive mobile-first
Aplicación web optimizada para celular en lugar de app nativa Android/iOS.

## DEC-004 — Reserva y pago como operación lógica única
Para reserva remota, al confirmarse el pago se crea la reserva y se actualiza el stock de forma consistente.

## DEC-005 — Múltiples unidades
Un consumidor puede adquirir más de una unidad de una oferta si existe stock suficiente.

## DEC-006 — Cancelación/reembolso solo para reservas remotas
Una reserva remota no entregada puede cancelarse. Penalización: 50% de lo abonado. Reembolso simulado: 50%. Las compras presenciales por QR no tienen cancelación/reembolso mediante la plataforma.

## DEC-007 — Oferta con reservas confirmadas no cancelable
El comercio no puede cancelar una oferta después de que tenga reservas confirmadas.

## DEC-008 — Horarios definidos por el establecimiento
Cada oferta establece el inicio y el fin de su horario de recojo, configurados por el comercio. **El fin del horario es el único límite**: después de ese momento no se permite entregar ni cancelar una reserva. No existe un límite definitivo adicional (actualizado en el avance 2; la columna `limite_definitivo` se conserva por compatibilidad y siempre coincide con el fin del horario).

## DEC-009 — Gestión de no recojo
Una reserva pagada que no se entrega antes del fin del horario de recojo pasa automáticamente a expirada (no recogida) y ya no puede entregarse. El importe cobrado y la comisión se conservan, no cuenta como pack entregado para el impacto y el consumidor recibe un aviso. No existe un margen adicional después del fin del horario.

## DEC-010 — Reseñas solo tras recojo completado
No se puede reseñar una experiencia que no haya terminado en entrega.

## DEC-011 — Separar PackSorpresa de OfertaPack
`PackSorpresa` es una definición reutilizable; `OfertaPack` es la publicación concreta para una fecha, precio, stock y horario determinados.

## DEC-012 — Valores cerrados mediante enums
Roles, estados y métodos de pago cerrados se modelarán como enums cuando corresponda, en vez de crear entidades artificiales solo para alcanzar el mínimo de clases.

## DEC-013 — Acceso presencial mediante QR
Un establecimiento puede mostrar QR que lleva directamente a una oferta activa. Sirve para quien ya está físicamente allí y también para dar visibilidad a la plataforma.

## DEC-014 — Medios de pago según contexto
- Remoto: solo pago digital simulado.
- QR presencial: pago digital simulado o efectivo.
  El efectivo existe únicamente cuando el consumidor ya está en el establecimiento.

## DEC-015 — Afiliación de comercios sujeta a aprobación
Un usuario no puede publicar packs inmediatamente; debe solicitar afiliación y ser aprobado por un administrador de plataforma.

## DEC-016 — Verificación documental, no certificación propia
La plataforma revisa documentación aportada, pero no se presenta como autoridad sanitaria ni emite certificaciones sanitarias propias.

## DEC-017 — Documentos obligatorios dentro del registro/solicitud
La documentación requerida se adjunta como parte del mismo proceso de solicitud de afiliación. No se completa “después de registrarse” para poder enviar la solicitud.

## DEC-018 — Transparencia sin exposición de documentos privados
El consumidor puede ver estados de verificación/vigencia del establecimiento, pero no los archivos documentales originales.

## DEC-019 — Vigencia individual por documento
No se asume una frecuencia universal de renovación. Cada documento puede tener fecha de emisión/vencimiento y su propia vigencia.

## DEC-020 — Revisión documental manual en V1
Un administrador humano abre el documento y registra manualmente datos relevantes como tipo, número, fecha de emisión, vencimiento y observaciones. No OCR/análisis automático en V1.

## DEC-021 — Registro público para consumidores y aspirantes a comercio
Las cuentas internas administrativas no tienen registro público.

## DEC-022 — Administradores provisionados internamente
Las cuentas de personal de plataforma se crean previamente por personal autorizado y solo utilizan login.

## DEC-023 — Google Sign-In fuera del alcance funcional de V1
Puede mostrarse como función futura/“Próximamente”, pero no debe aparentar funcionar si no está implementada.

## DEC-024 — Funciones futuras visibles como “Próximamente”
Toda función futura presente en el prototipo debe identificarse claramente como no implementada.

## DEC-025 — Arquitectura de monolito modular por capas

**Estado:** Aprobada.

### Decisión

La primera versión de la plataforma se desarrollará mediante una arquitectura de monolito modular por capas.

El backend será una única aplicación Java con Spring Boot y utilizará una única base de datos relacional. Internamente, el código se organizará en módulos correspondientes a las principales áreas del negocio. Dentro de cada módulo se separarán las responsabilidades mediante las capas `controller`, `service`, `repository`, `domain` y `dto`.

El frontend se comunicará con el backend mediante HTTP y servicios REST. Los controllers recibirán las solicitudes y delegarán las operaciones; los services implementarán las reglas del negocio y los límites transaccionales; los repositories gestionarán la persistencia mediante JPA; y las clases de dominio representarán los conceptos, relaciones y estados del negocio.

Las funciones transversales de seguridad, validación, configuración y manejo de excepciones se mantendrán separadas de la lógica funcional.

### Justificación

Esta arquitectura satisface la exigencia académica de aplicar una arquitectura de software, separación por capas, persistencia con JPA, base de datos, formularios y servicios REST.

Es adecuada para el alcance de la primera versión porque facilita las pruebas, mantiene alineados los diagramas y el código, permite repartir el trabajo entre cinco integrantes y puede ser comprendida y sustentada por todo el equipo.

La aplicación no se dividirá en microservicios durante la V1. Una separación física futura solo se evaluará si aparece una necesidad concreta que la justifique.

### Consecuencias

- Existirá un único backend desplegable.
- La base de datos será compartida por todos los módulos.
- La lógica del negocio se concentrará en los services y el dominio.
- El frontend no accederá directamente a la base de datos.
- Los controllers no contendrán reglas importantes del negocio.
- Los repositories no tomarán decisiones del negocio.
- Las operaciones que requieran consistencia, como la creación de una reserva y la actualización del stock, se ejecutarán mediante transacciones.
- La organización modular deberá corresponder con los requerimientos, casos de uso y clases definitivas.
- Las versiones y tecnologías concretas se confirmarán según lo utilizado en clase.


## DEC-026 — Una identidad con perfiles combinables

Una persona utilizará una sola cuenta y podrá tener perfil de consumidor, perfil empresarial o ambos. El cambio de perfil no requiere crear credenciales duplicadas.

## DEC-027 — Empresa con varios establecimientos

Una empresa puede registrar varios establecimientos físicos. Cada establecimiento tiene una ubicación, documentos, aprobación y operación independientes.

## DEC-028 — Cuenta operativa por establecimiento

Después de la aprobación, el propietario crea una cuenta compartida para el personal de cada establecimiento. Esta cuenta solo puede gestionar el local asignado. No se registrarán empleados individualmente en la V1.

## DEC-029 — Nueva solicitud después de un rechazo

Si una solicitud presenta problemas, se rechaza y se informa el motivo. La cuenta, empresa y establecimiento se conservan; el propietario deberá presentar una nueva solicitud para ese local.

## DEC-030 — Suspensión por documento vencido

Un establecimiento aprobado se suspende automáticamente si vence un documento obligatorio. Mientras esté suspendido no aparece en la búsqueda, no vende y no puede publicar nuevas ofertas. Su cuenta, su historial y sus reservas pagadas se conservan: la cuenta del trabajador sigue activa para validar códigos y entregar esas reservas. El local se reactiva cuando el administrador aprueba la renovación (DEC-044).

## DEC-031 — Una oferta activa con variantes

Cada establecimiento tendrá como máximo una oferta activa simultáneamente. La oferta puede contener varios tamaños con precios y stock propios.

## DEC-032 — Información previa del pack

Antes de comprar se mostrarán la descripción, posibles productos, tamaño, precio, horario, alérgenos y restricciones alimentarias. El contenido exacto seguirá siendo sorpresa.

## DEC-033 — Boleta simulada y código de recojo

Al confirmarse el pago se generará una boleta sin validez tributaria y un código único. El personal buscará ese código y lo invalidará al marcar la entrega.

## DEC-034 — Retención para efectivo presencial

Cuando se elija efectivo desde el QR del local, el stock se retendrá temporalmente. El personal confirmará manualmente el dinero recibido y el recojo quedará completado en ese momento.

## DEC-035 — Límites de cancelación remota

Una reserva remota puede cancelarse mientras esté pendiente de recojo y antes del fin del horario de recojo (DEC-008). Se registra un reembolso simulado del 50 % y el stock se recupera si todavía puede venderse.

## DEC-036 — Comisión del 10 % sobre lo cobrado

La comisión de la plataforma será el 10 % del importe que efectivamente permanezca cobrado. Si una reserva se cancela, la comisión se calcula sobre la mitad retenida, no sobre el precio original.

**Estado de esta cifra:** confirmada en RF-24 y en las definiciones funcionales aprobadas del informe del avance 2. El cálculo está implementado: si el consumidor cancela, la comisión se aplica sobre el 50 % retenido; en efectivo queda como saldo simulado del local con la plataforma.

## DEC-037 — Panel basado en información calculada

Ventas, comisiones, ingresos, cancelaciones, no recojos, calificaciones e impacto se calcularán desde los registros existentes. No se crearán entidades artificiales para representar el dashboard.

## DEC-038 — Cálculo ambiental postergado

El impacto ambiental se registrará conceptualmente después de un recojo completado. La fórmula exacta de alimento recuperado y CO₂ evitado se definirá en una etapa posterior.

## DEC-039 — Stack tecnológico principal

- Java 21.
- Spring Boot.
- Maven.
- Spring Data JPA.
- MySQL.
- HTML, CSS y JavaScript.
- Tailwind CSS.

El POM ya declara Spring Boot 4.1.1 y Java 21. H2 se utiliza en el perfil `dev`; MySQL tiene un perfil local separado. El stack del frontend permanece previsto.

## DEC-040 — Identidad del proyecto

El nombre de la plataforma es **Sazloop**. El equipo ha confirmado que corresponde al proyecto **N.º 91 — SalvemosLaComida**. El repositorio se encuentra en `Zheyben/Sazloop`. El paquete Java pasa a `com.sazloop` y el artefacto Maven a `sazloop-backend`. El esquema MySQL mantiene su identificador anterior por compatibilidad con las instalaciones existentes.

## DEC-041 — Delimitación territorial de la primera versión

La primera versión se dirige a establecimientos y consumidores de Lima Metropolitana, con recojo presencial y pagos simulados. Las menciones de GPS y delivery externo son investigación futura; no introducen funcionalidades activas en esta actualización.

## DEC-042 — Separación entre diseño e implementación

Los RF corresponden a los 26 requisitos agrupados del informe del primer avance. Las 31 entidades existen en el backend y originan 33 tablas al incluir las dos asociaciones muchos a muchos. Las operaciones dibujadas, la API REST y los prototipos describen comportamiento previsto; no se marcan como ejecutados sin evidencia. El estado del código se mantiene en [ESTADO_ACTUAL.md](../tareas/ESTADO_ACTUAL.md).

## DEC-043 — Vigencia de documentos y aviso previo

Todo documento aprobado con fecha de vencimiento cuenta para la vigencia del local; uno sin fecha no vence. El propietario puede indicar la fecha al adjuntarlo y el administrador la corrige al revisarlo. Un documento vale hasta el final del día de su vencimiento (hora de Perú). Siete días antes, el propietario recibe un único aviso; si vence sin una renovación aprobada, una tarea programada (al iniciar y cada minuto) suspende el local, registra el motivo y lo notifica. La suspensión automática queda firmada por una cuenta interna del sistema sin acceso, para conservar la relación obligatoria con `AdministradorPlataforma` del modelo.

## DEC-044 — Renovación de documentos

Para reactivar un local suspendido, el propietario presenta solo los documentos vencidos; también puede renovar un documento que vence en los próximos siete días. La renovación es una nueva `SolicitudAfiliacion` del mismo local y se reconoce porque existe una solicitud anterior aprobada. El administrador la revisa en la misma pantalla. Al aprobarla, cada documento renovado reemplaza al anterior del mismo tipo y, si el local ya no tiene documentos vencidos, se reactiva y su suspensión se cierra. Si la rechaza, el local sigue en el mismo estado y puede presentar otra renovación.

## DEC-045 — Notificaciones

El buzón registra avisos para el propietario (resultado de la solicitud o la renovación, documento por vencer y suspensión) y para el consumidor (recordatorio de recojo, cancelación con reembolso, pedido no recogido y reserva en efectivo vencida). **El recordatorio de recojo se crea al confirmar la compra**, uno por pedido, con su código, dirección y horario. La cuenta compartida del local no recibe notificaciones porque no es un usuario personal.

## DEC-046 — Impacto visible

Cada recojo completado, tanto por entrega con código como por compra presencial en efectivo, registra un `ImpactoCO2Evitado` sin cifras. En esta etapa el impacto visible es el número de packs efectivamente entregados; no se muestran kilos de alimento recuperado ni CO₂ evitado hasta aprobar una fórmula.

## DEC-047 — Mensajes de error comprensibles

Las respuestas de error de la API incluyen el motivo de negocio (por ejemplo, «Stock insuficiente») y nunca la traza interna; las pantallas muestran ese motivo. En Spring Boot 4 se configura con `spring.web.error.include-message` y `spring.web.error.include-stacktrace`.

## DEC-048 — Entrega desde el pago

El fin del horario es el único límite (DEC-008): el personal puede entregar un pedido pagado desde el momento de la compra, aunque el inicio del horario aún no haya llegado. Para que una oferta pueda publicarse con inicio «ahora», se tolera un inicio de hasta cinco minutos en el pasado (desfase del reloj del navegador). El formulario sugiere inicio ahora y fin tres horas después.

## DEC-049 — Resolución de una solicitud en un solo paso

El administrador revisa todos los documentos de una solicitud o renovación y la decide en una sola operación atómica (). La solicitud se aprueba únicamente si todos los documentos se aprueban; un documento vencido solo puede rechazarse. El motivo se arma con el comentario opcional del administrador o, si se rechaza, con las observaciones de cada documento. Ya no existe un paso de «decisión final» separado. El administrador no edita los datos transcritos: la fecha de vencimiento la declara el propietario al adjuntar el documento.

## DEC-050 — Sesiones por rol

La persona (consumidor o propietario), la cuenta del local y el administrador comparten la cookie del navegador, pero cada rol guarda su propio atributo de sesión. Iniciar o cerrar sesión en un rol no afecta a los demás, de modo que pueden estar abiertos a la vez los portales de empresa, trabajador y administración en un mismo navegador.

## Documentos relacionados

- [Visión y alcance](../PROYECTO.md)
- [Requisitos](../requisitos/REQUISITOS.md)
- [Flujos de negocio](../FLUJOS_NEGOCIO.md)
- [Modelo de dominio](MODELO_DOMINIO.md)
- [Arquitectura](../ARQUITECTURA.md)
