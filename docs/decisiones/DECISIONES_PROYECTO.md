# Decisiones del proyecto

Registro de acuerdos y sus razones. Una decisión aprobada no demuestra que esté implementada ni probada: el estado real está en [Proyecto](../PROYECTO.md) y la evidencia histórica en [Auditorías](../../auditoria/).

Se conservan DEC-001 a DEC-050. La autorización de esta sincronización incorpora D1–D6 como DEC-051 a DEC-056 y precisa qué acuerdos anteriores actualizan. Las fuentes académicas excluidas no se inspeccionaron de nuevo.

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

Solo se puede reseñar una compra efectivamente recogida. La cantidad permitida se precisa por compra en D2 / DEC-052; su adaptación todavía está pendiente en el código.

## DEC-011 — Separar PackSorpresa de OfertaPack
`PackSorpresa` es una definición reutilizable; `OfertaPack` es la publicación concreta para una fecha, precio, stock y horario determinados.

## DEC-012 — Valores cerrados mediante enums
Roles, estados y métodos de pago cerrados se modelarán como enums cuando corresponda, en vez de crear entidades artificiales solo para alcanzar el mínimo de clases.

## DEC-013 — Acceso presencial mediante QR

El QR abre la oferta de una compra presencial. D1 / DEC-051 fija que cada oferta tiene su propio código; queda descartada la propuesta anterior de un QR permanente por local.

## DEC-014 — Medios de pago según contexto
- Remoto: solo pago digital simulado.
- QR presencial: pago digital simulado o efectivo.
  El efectivo existe únicamente cuando el consumidor ya está en el establecimiento.

## DEC-015 — Afiliación de comercios sujeta a aprobación
Un usuario no puede publicar packs inmediatamente; debe solicitar afiliación y ser aprobado por un administrador de plataforma.

## DEC-016 — Verificación documental, no certificación propia
La plataforma revisa documentación aportada, pero no se presenta como autoridad sanitaria ni emite certificaciones sanitarias propias.

## DEC-017 — Documentos obligatorios dentro del registro/solicitud

Los documentos se adjuntan antes de enviar la solicitud de afiliación. D3 / DEC-053 deja pendiente el conjunto exacto de documentos exigibles; no se debe inventar esa lista ni afirmar que el sistema ya la valida.

## DEC-018 — Transparencia sin exposición de documentos privados

El consumidor debe conocer la verificación sin acceder a documentos privados. D4 / DEC-054 precisa la indicación visible y exige distinguir aprobación del establecimiento de vigencia documental. Su presentación al consumidor está pendiente.

## DEC-019 — Vigencia individual por documento
No se asume una frecuencia universal de renovación. Cada documento puede tener fecha de emisión/vencimiento y su propia vigencia.

## DEC-020 — Revisión documental manual en V1

La revisión es humana, sin OCR. El planteamiento inicial incluía transcribir o corregir tipo, número y fechas. DEC-049 define la pantalla actual con datos de solo lectura y resolución conjunta; las operaciones anteriores conservan la edición y permanecen temporalmente por D5 / DEC-055.

## DEC-021 — Registro público para consumidores y aspirantes a comercio
Las cuentas internas administrativas no tienen registro público.

## DEC-022 — Administradores provisionados internamente
Las cuentas de personal de plataforma se crean previamente por personal autorizado y solo utilizan login.

## DEC-023 — Google Sign-In fuera del alcance funcional de V1
Puede mostrarse como función futura/“Próximamente”, pero no debe aparentar funcionar si no está implementada.

## DEC-024 — Funciones futuras visibles como “Próximamente”
Toda función futura presente en el prototipo debe identificarse claramente como no implementada.

## DEC-025 — Arquitectura de monolito modular por capas

**Estado:** aprobada.

Una sola aplicación Spring Boot y una base relacional compartida, organizadas por módulos de negocio y capas según su necesidad: controller, service, repository, domain y dto. El frontend se comunica por HTTP; los servicios coordinan reglas y transacciones y los repositorios acceden a datos mediante JPA.

La elección permite repartir el trabajo académico entre cinco integrantes, comprender el sistema y mantener sus modelos sin la complejidad de microservicios. Estos últimos quedan fuera de V1.

Los controladores deben delegar las decisiones importantes; los repositorios no deben decidir reglas de negocio; el frontend no accede a la base de datos. Los cambios de reserva, pago y stock deben conservarse juntos mediante transacciones. Las funciones técnicas compartidas deben separarse de la lógica de negocio. Los contratos entre módulos requieren coordinación de sus responsables.

[Arquitectura](../ARQUITECTURA.md) distingue estas reglas de la estructura implementada, que no contiene todas las capas en cada módulo y tiene dependencias directas entre repositorios de distintas áreas.

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

La comisión de la plataforma es el 10 % del importe que permanece cobrado, con redondeo monetario a dos decimales. Si una reserva se cancela, se aplica sobre la mitad retenida, no sobre el precio original. La cifra estaba confirmada en RF-24 y en las definiciones funcionales documentadas del avance 2.

Ejemplo: en una compra de S/ 20, la comisión es S/ 2 y el ingreso del local S/ 18. Si se cancela, se devuelven S/ 10; de los S/ 10 retenidos, Sazloop recibe S/ 1 y el local S/ 9. En efectivo, la comisión queda como saldo simulado del local con la plataforma.

`ComisionService.recalcular` implementa el cálculo. El panel empresarial incluye el importe retenido de cancelaciones en ingresos y comisiones, pero informa cancelaciones por separado de las ventas. El panel del trabajador calcula la ganancia de lo entregado.

## DEC-037 — Panel basado en información calculada

Ventas, comisiones, ingresos, cancelaciones, no recojos, calificaciones e impacto se calcularán desde los registros existentes. No se crearán entidades artificiales para representar el dashboard.

## DEC-038 — Cálculo ambiental postergado

El impacto ambiental se registrará conceptualmente después de un recojo completado. La fórmula exacta de alimento recuperado y CO₂ evitado se definirá en una etapa posterior.

## DEC-039 — Stack tecnológico principal

Java 21, Spring Boot, Maven, Spring Data JPA y MySQL; HTML, CSS propio y JavaScript en el frontend. El POM declara Spring Boot 4.1.1; H2 sirve para desarrollo y pruebas. La inclusión histórica de Tailwind queda sustituida por D6 / DEC-056. Los detalles actuales se mantienen en Arquitectura.

## DEC-040 — Identidad del proyecto

La plataforma se llama **Sazloop**, proyecto académico **N.º 91 — SalvemosLaComida**. MealFlow se conserva como antecedente histórico. La dirección actual es `Zheyben/Sazloop`, el paquete Java `com.sazloop`, el artefacto `sazloop-backend` y el esquema configurado de MySQL `sazloop`; no se presume que las instalaciones personales antiguas tengan el mismo nombre.

## DEC-041 — Delimitación territorial de la primera versión

La primera versión se dirige a establecimientos y consumidores de Lima Metropolitana, con recojo presencial y pagos simulados. Las menciones de GPS y delivery externo son investigación futura; no introducen funcionalidades activas en esta actualización.

## DEC-042 — Separación entre diseño e implementación

Los 26 RF conservan sus identificadores y trazabilidad. Las 31 entidades JPA y dos asociaciones muchos a muchos describen 33 tablas de aplicación en el código. No se declara comprobada su coincidencia con los diagramas excluidos. Encontrar una clase, una ruta o un prototipo no demuestra una ejecución correcta: [Proyecto](../PROYECTO.md) separa implementación, pruebas concretas, adaptación pendiente y definición pendiente.

## DEC-043 — Vigencia de documentos y aviso previo

Todo documento aprobado con vencimiento cuenta para la vigencia actual; uno sin fecha no vence. Vale hasta el final de su fecha de vencimiento en Perú. Se avisa una vez cuando entra en los próximos siete días y, si vence sin renovación aprobada, se suspende el local, se registra el motivo y se notifica al propietario.

La fecha la presenta el propietario; la corrección administrativa prevista originalmente permanece en las operaciones anteriores conservadas, mientras la pantalla de DEC-049 es de solo lectura. La revisión programada actual empieza a los 15 segundos y se repite cada minuto, con intervalos configurables. La suspensión se atribuye a una cuenta interna desactivada del sistema para conservar su relación con AdministradorPlataforma.

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

El administrador revisa y resuelve una solicitud o renovación desde una pantalla mediante `POST /api/administradores/solicitudes/{id}/resolucion`. Se guardan conjuntamente revisiones y evaluación; la solicitud solo se aprueba con todos los documentos aprobados y no vencidos. El motivo se obtiene del comentario general opcional o de las observaciones de rechazo.

La pantalla no permite editar datos transcritos: las fechas las declara el propietario. Presenta un documento vencido para rechazo y exige observaciones al rechazar. La regla de no aprobar documentos vencidos se conserva; no debe confundirse la validación de la aprobación final con todas las posibilidades de las rutas anteriores.

El paso separado desapareció de la pantalla, no del backend: D5 / DEC-055 conserva temporalmente `/revision` y `/decision`.

## DEC-050 — Sesiones por rol

La persona (consumidor o propietario), la cuenta del local y el administrador comparten la cookie del navegador, pero cada rol guarda su propio atributo de sesión. Iniciar o cerrar sesión en un rol no afecta a los demás, de modo que pueden estar abiertos a la vez los portales de empresa, trabajador y administración en un mismo navegador.

## DEC-051 — QR por oferta (D1)

**Aprobada:** cada oferta tiene su propio QR. El enlace identifica la publicación concreta y no se reutiliza como QR permanente del establecimiento. Precisa DEC-013 y sustituye ese planteamiento anterior del modelo. Está implementado mediante `OfertaPack.tokenQr`, `trabajador.js` y `qr.js`; al vencer o cambiar la oferta, el QR anterior no redirige a la nueva.

## DEC-052 — Una reseña por compra recogida (D2)

**Aprobada; adaptación pendiente:** como máximo una reseña por compra efectivamente recogida. Una persona puede reseñar varias compras del mismo establecimiento. Precisa DEC-010 y RF-23.

Continúan la restricción única consumidor/local de `Resena` y las comprobaciones por local de `ResenaService` y `CompraRecojoService`; no cumplen todavía esta decisión. Deben adaptarse conjuntamente en otra tarea. La contradicción entre calificación obligatoria y posibilidad de omitir sigue pendiente del equipo; no se resuelve cambiando su redacción sin acuerdo.

## DEC-053 — Lista documental pendiente (D3)

**Aprobada; definición detallada pendiente:** el equipo todavía no fija qué documentos exactos debe presentar cada establecimiento. Se conserva RF-06 y DEC-017 sin inventar documentos obligatorios.

La implementación comprueba propiedad, estado de solicitud, archivo y fechas; exige algún adjunto para enviar la afiliación inicial y cubre tipos vencidos faltantes al renovar un local suspendido. Estas comprobaciones no equivalen a una lista inicial aprobada.

## DEC-054 — Verificación visible para consumidores (D4)

**Aprobada; implementación pendiente:** mostrar una indicación sencilla, como «Establecimiento verificado», sin acceso a documentos privados. El estado debe ser real y distinguir aprobación del local de vigencia de sus documentos. La regla exacta de presentación cuando difieren requiere precisión del equipo antes de implementarse.

El catálogo actual filtra locales activos, pero su respuesta no incluye el indicador documental. No se presenta ese filtro como cumplimiento de DEC-018 ni de esta decisión.

## DEC-055 — Conservar operaciones administrativas anteriores (D5)

**Aprobada; conservación existente:** mantener temporalmente revisión y decisión separadas. La pantalla actual usa resolución conjunta. Las pruebas existentes y `DatosDemoH2` aún invocan servicios del recorrido anterior; no se ha establecido una lista completa de consumidores externos. [Arquitectura](../ARQUITECTURA.md) identifica las rutas. Su posible eliminación queda pendiente de evaluación de compatibilidad.

## DEC-056 — CSS propio, sin Tailwind (D6)

**Aprobada y reflejada en los requisitos:** Sazloop continúa con sus estilos CSS actuales y no utilizará Tailwind. Sustituye su inclusión en DEC-039 y en el requisito tecnológico anterior. La revisión del frontend y las dependencias no identificó integración técnica de Tailwind que retirar; si aparece alguna, se registrará su limpieza sin modificar código dentro de una tarea documental.

## Decisiones todavía abiertas

- Cierre anticipado de ofertas: no hay operación implementada identificada; DEC-007 se conserva para una eventual cancelación.
- Simulación de pagos digitales rechazados: la compra válida se confirma directamente; no se decide aquí agregar una pasarela ni un simulador de rechazos.
- Obligatoriedad y omisión de reseñas, lista exacta de documentos y criterio de presentación de la verificación, según DEC-052 a DEC-054.

## Documentos relacionados

- [Visión y alcance](../PROYECTO.md)
- [Requisitos](../requisitos/REQUISITOS.md)
- [Flujos de negocio](../FLUJOS_NEGOCIO.md)
- [Modelo de dominio](MODELO_DOMINIO.md)
- [Arquitectura](../ARQUITECTURA.md)
