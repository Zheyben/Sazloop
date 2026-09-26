# Modelado del dominio — Sazloop

## Propósito del documento

Este documento explica las clases de dominio de Sazloop, qué representa cada una y por qué es necesaria.

Su finalidad es que todos los integrantes comprendan las responsabilidades del modelo y mantengan alineados el informe, los diagramas y el backend.

> **Estado al 26 de septiembre de 2026:** Las 31 clases del catálogo existen como entidades JPA. Hay diagramas editables y un informe corregido, pero la coherencia completa sigue pendiente; véase [la revisión](../informe/REVISION_APF1.md). Los métodos de negocio representados en UML son diseño previsto, no métodos ejecutados y verificados en Java.

## Criterios para escoger las clases

- Cada clase debe representar una persona, objeto, proceso o concepto importante del negocio.
- No se crearán clases artificiales solamente para alcanzar el mínimo solicitado por el curso.
- Los nombres deberán mantenerse, en lo posible, en los requerimientos, diagramas, base de datos y código.
- Los valores que solo tengan una lista cerrada de opciones, como estados, roles o métodos de pago, se representarán mediante enumeraciones.
- Los controllers, services, repositories y DTO no son clases de dominio.
- El modelo de esta primera versión debe cubrir el alcance académico sin añadir funciones demasiado avanzadas.

## Visión general del dominio

Sazloop será una plataforma web mobile-first en la que los establecimientos de comida podrán ofrecer sus excedentes mediante packs sorpresa. Los consumidores podrán encontrarlos, comprarlos mediante pagos simulados y recogerlos presencialmente.

Una empresa podrá tener varios establecimientos físicos. Cada establecimiento tendrá su propia ubicación, revisión documental, estado de aprobación y acceso operativo. La cuenta propietaria podrá consultar información general de todos los establecimientos pertenecientes a su empresa.

## Grupo 1 — Usuarios y estructura empresarial

### Clases seleccionadas

| Clase | ¿Qué representa? | ¿Por qué existe? |
|---|---|---|
| `Usuario` | La identidad principal con la que una persona se registra e inicia sesión. | Centraliza los datos comunes de acceso y evita crear cuentas separadas cuando una misma persona quiere comprar packs y administrar su empresa. |
| `Consumidor` | El perfil de un usuario que utiliza la plataforma para buscar, comprar, recoger y calificar packs sorpresa. | Guarda la información y las funciones propias del comprador sin mezclarlas con las responsabilidades empresariales. |
| `PropietarioEmpresa` | El perfil empresarial de la persona responsable de registrar y administrar una empresa. | Permite que el mismo usuario pueda cambiar entre su perfil de consumidor y su perfil empresarial. |
| `Empresa` | La organización comercial registrada en la plataforma. | Agrupa los establecimientos que pertenecen al mismo negocio y permite consultar resultados generales de todos ellos. |
| `Establecimiento` | Cada local físico en el que se preparan o venden los packs sorpresa. | Cada local posee ubicación, documentos, aprobación, ofertas y operación propias, incluso cuando pertenece a una empresa con varios locales. |
| `Direccion` | La ubicación estructurada de un establecimiento. | Permite mostrar dónde se encuentra el local y realizar búsquedas por datos como distrito y dirección, sin necesitar GPS ni mapas en la primera versión. |
| `CuentaEstablecimiento` | La cuenta operativa compartida por el personal de un local aprobado. | Permite gestionar las ventas y confirmar pagos o recojos desde un establecimiento específico sin registrar individualmente a cada trabajador. |

### Relaciones principales

- Un `Usuario` puede tener un perfil `Consumidor`, un perfil `PropietarioEmpresa` o ambos.
- `Consumidor` y `PropietarioEmpresa` se relacionan con `Usuario` como perfiles. No serán tipos de usuario separados ni utilizarán cuentas distintas.
- Un `PropietarioEmpresa` administra la `Empresa` con la que se afilió.
- Una `Empresa` puede registrar varios `Establecimiento`.
- Cada `Establecimiento` pertenece a una sola `Empresa`.
- Cada `Establecimiento` tiene una sola `Direccion`.
- Cada `CuentaEstablecimiento` pertenece exclusivamente a un `Establecimiento`.
- La `CuentaEstablecimiento` es creada por el propietario después de que el establecimiento sea aprobado.

### Decisiones de modelado

#### Perfiles asociados en lugar de herencia

`Consumidor` y `PropietarioEmpresa` estarán asociados a `Usuario`. No se modelarán como usuarios completamente separados porque una misma persona puede desempeñar ambos papeles y cambiar de perfil desde una sola cuenta.

Esto evita duplicar sus datos personales y sus credenciales de acceso.

#### Diferencia entre empresa y establecimiento

`Empresa` y `Establecimiento` no significan lo mismo:

- La empresa representa al negocio completo.
- El establecimiento representa un local físico concreto.
- Una empresa puede tener varios establecimientos en diferentes ubicaciones.
- La revisión y aprobación de documentos se realizará individualmente para cada establecimiento.

Esta separación permitirá que el propietario consulte resultados generales de la empresa y, al mismo tiempo, que cada local gestione su propia operación.

#### Cuenta operativa por establecimiento

Cada establecimiento aprobado podrá tener una cuenta operativa compartida. Esta cuenta solo permitirá trabajar con la información del local al que fue asignada.

En la primera versión no se registrará a cada trabajador por separado. Por esa razón, no se incluirá todavía una clase `Empleado`. La ausencia de seguimiento individual se acepta como una limitación del alcance inicial.

#### Dirección como parte del establecimiento

La `Direccion` pertenece al establecimiento y no tiene sentido por sí sola dentro del sistema. Se mantiene como clase porque reúne datos relacionados, como distrito, calle, número y referencia, y facilita el filtrado de locales.

## Grupo 2 — Afiliación, documentos y revisión administrativa

### Clases seleccionadas

| Clase | ¿Qué representa? | ¿Por qué existe? |
|---|---|---|
| `SolicitudAfiliacion` | El pedido formal para que un establecimiento pueda operar en la plataforma. | Reúne el proceso de evaluación de un local y permite conocer si está pendiente, aprobado o rechazado. Cada nuevo intento se registra como una solicitud nueva. |
| `DocumentoEstablecimiento` | Cada archivo presentado por un establecimiento, junto con datos como tipo, número, emisión y vencimiento. | Los documentos tienen vigencias y resultados propios, por lo que no deben guardarse como simples campos generales del establecimiento. |
| `RevisionDocumento` | La revisión manual realizada sobre un documento. | Permite registrar quién lo revisó, cuándo lo hizo, cuál fue el resultado y qué observaciones encontró. También sirve para revisar documentos renovados. |
| `EvaluacionAfiliacion` | La decisión final tomada sobre una solicitud de afiliación. | Separa la aprobación o rechazo general del resultado individual de cada documento y deja constancia del motivo de la decisión. |
| `AdministradorPlataforma` | El perfil interno autorizado para revisar documentos y solicitudes. | Identifica al responsable de cada revisión. Su cuenta es creada internamente y no mediante el registro público. |
| `SuspensionEstablecimiento` | El registro de una suspensión aplicada a un establecimiento ya aprobado. | Conserva la causa y duración de la suspensión, especialmente cuando vence un documento obligatorio, sin borrar el local ni su historial. |

### Relaciones principales

- Un `Establecimiento` puede tener varias `SolicitudAfiliacion` a lo largo del tiempo, pero solo una solicitud activa a la vez.
- Cada `SolicitudAfiliacion` corresponde a un solo `Establecimiento`.
- La solicitud reúne los `DocumentoEstablecimiento` necesarios para evaluar ese local.
- Cada documento puede recibir una o más `RevisionDocumento`, por ejemplo, después de presentar una renovación.
- Un `AdministradorPlataforma` realiza las revisiones documentales.
- Una solicitud puede recibir una `EvaluacionAfiliacion` final cuando la revisión termina.
- Un establecimiento aprobado puede tener registros de `SuspensionEstablecimiento` sin perder sus datos, ventas ni historial.

### Decisiones de modelado

#### La afiliación se evalúa por establecimiento

La empresa no se aprueba en bloque. Cada local presenta sus propios documentos y se evalúa de forma independiente. Por eso, una empresa puede tener un establecimiento aprobado mientras otro continúa pendiente o se encuentra suspendido.

#### Revisión individual de documentos

Cada documento posee sus propias fechas y estado. El administrador lo revisará manualmente; la primera versión no utilizará OCR ni lectura automática de archivos.

El resultado de un documento no reemplaza la decisión final sobre la solicitud. Primero se revisan los documentos y después se registra la evaluación general del establecimiento.

#### Nueva solicitud después de un rechazo

Si se encuentran problemas, la solicitud se rechaza y se informa el motivo al propietario. No se elimina la cuenta, la empresa ni el establecimiento. Para volver a intentarlo, el propietario deberá presentar una nueva solicitud para ese local.

Conservar cada intento permite mantener un historial claro de lo sucedido.

#### Suspensión por documento vencido

Si vence un documento obligatorio de un establecimiento aprobado, el local se suspende y no podrá publicar nuevas ofertas. La información existente se conserva.

El propietario deberá presentar el documento renovado. Cuando este sea revisado y aprobado manualmente, la suspensión podrá finalizar y el establecimiento volverá a operar.

### Elementos que se representarán como enumeraciones

No se crearán clases independientes para listas cerradas como:

- Tipo de documento.
- Estado de una solicitud.
- Resultado de una revisión.
- Motivo general de suspensión cuando provenga de una lista predefinida.

## Grupo 3 — Packs sorpresa y ofertas

### Clases seleccionadas

| Clase | ¿Qué representa? | ¿Por qué existe? |
|---|---|---|
| `PackSorpresa` | La definición general del pack ofrecido por un establecimiento. | Reúne su descripción, posibles productos y advertencias, sin revelar el contenido exacto que recibirá el consumidor. |
| `OfertaPack` | La publicación concreta del pack disponible en una fecha determinada. | Guarda la vigencia, ventana de recojo, hora límite y estado de la oferta sin modificar la definición general del pack. |
| `VariantePack` | Cada tamaño disponible dentro de una oferta, por ejemplo pequeño, mediano o grande. | Cada opción necesita su propio precio y cantidad disponible, aunque todas formen parte del mismo pack activo. |
| `Alergeno` | Una sustancia que el pack podría contener y que puede causar una reacción alérgica. | Permite advertir al consumidor antes de comprar, aunque el contenido exacto del pack sea sorpresa. |
| `RestriccionAlimentaria` | Una condición o característica alimentaria relevante, como presencia de gluten o falta de compatibilidad vegana. | Ayuda al consumidor a saber si el pack podría ser inadecuado para sus necesidades alimentarias. |

### Relaciones principales

- Cada `PackSorpresa` pertenece a un solo `Establecimiento`.
- Un `PackSorpresa` puede utilizarse en varias `OfertaPack` a lo largo del tiempo.
- Un establecimiento solo puede tener una `OfertaPack` activa simultáneamente en la primera versión.
- Cada oferta contiene una o más `VariantePack`.
- Cada variante guarda su tamaño, precio referencial, precio con descuento, stock inicial y stock disponible.
- Un pack puede relacionarse con varios `Alergeno` y varias `RestriccionAlimentaria`.

### Decisiones de modelado

#### Diferencia entre pack y oferta

El `PackSorpresa` describe lo que normalmente ofrece el establecimiento. La `OfertaPack` representa una publicación específica para un día y horario concretos.

Separarlos evita tener que repetir toda la descripción del pack cada vez que cambia el precio, el stock o la ventana de recojo.

#### Variantes dentro de una sola oferta

En la primera versión no existirán varios tipos de ofertas activos en un mismo establecimiento. Existirá un solo pack activo con diferentes `VariantePack`.

Por ejemplo, una oferta puede incluir:

- Variante pequeña: precio y stock propios.
- Variante mediana: precio y stock propios.
- Variante grande: precio y stock propios.

#### Información visible antes de comprar

El consumidor podrá conocer:

- La descripción general del pack.
- Ejemplos de productos que podría recibir.
- El tamaño seleccionado.
- El precio referencial y el precio con descuento.
- La ventana y el límite de recojo.
- Los posibles alérgenos.
- Las restricciones alimentarias informadas.

La lista exacta de alimentos seguirá siendo sorpresa.

#### QR del establecimiento

No se creará una clase de dominio solo para representar el código QR. El QR será un enlace estable asociado al establecimiento que abrirá su oferta activa.

Si no existe una oferta activa, la plataforma mostrará que el pack no está disponible. La imagen o enlace del QR podrá resolverse técnicamente sin convertirlo en una entidad del negocio.

## Grupo 4 — Compra, pago y recojo

### Clases seleccionadas

| Clase | ¿Qué representa? | ¿Por qué existe? |
|---|---|---|
| `RetencionStock` | La separación temporal de unidades mientras una compra está en proceso. | Evita que otra persona compre el mismo stock y permite esperar la confirmación manual de un pago presencial en efectivo. |
| `Reserva` | La compra confirmada de uno o varios packs que deberán recogerse en el establecimiento. | Es el registro principal que conecta al consumidor, la oferta, el pago y el recojo. |
| `DetalleReserva` | Cada variante y cantidad incluida dentro de una reserva. | Conserva exactamente qué tamaños se compraron, cuántas unidades y a qué precio, incluso si la oferta cambia después. |
| `Pago` | La operación de pago asociada con la reserva. | Registra el medio utilizado, el importe, el contexto remoto o presencial y su estado. En la primera versión todos los pagos digitales serán simulados. |
| `BoletaSimulada` | El comprobante interno generado después de confirmar el pago. | Permite mostrar el resumen de la compra y el código de recojo, dejando claro que no es un comprobante tributario real. |
| `CodigoRecojo` | El código único que demuestra qué reserva debe entregar el establecimiento. | El consumidor lo dicta al personal y el sistema impide reutilizarlo después de confirmar el recojo. |
| `Recojo` | El registro de la entrega física del pack al consumidor. | Permite saber cuándo se completó la reserva, quién la confirmó desde la cuenta del local y cuándo se habilita la reseña. |
| `CancelacionReserva` | El registro de una cancelación solicitada por el consumidor. | Conserva la fecha, el motivo, la penalización aplicada y la devolución del stock. |
| `Reembolso` | La devolución simulada de una parte del pago. | Permite representar de forma separada el 50 % devuelto al consumidor cuando corresponde. |
| `ComisionPlataforma` | El importe que Sazloop retiene por una venta. | Representa directamente el modelo de negocio y permite calcular el ingreso neto del establecimiento. |

### Relaciones principales

- Una `RetencionStock` corresponde a una variante, una cantidad y un consumidor durante un tiempo limitado.
- Una `Reserva` pertenece a un `Consumidor` y a una `OfertaPack`.
- Cada reserva tiene uno o más `DetalleReserva`.
- Cada detalle señala la `VariantePack` seleccionada, la cantidad y el precio aplicado.
- Una reserva confirmada tiene un `Pago`, una `BoletaSimulada` y un `CodigoRecojo`.
- Una reserva puede terminar en un `Recojo` o en una `CancelacionReserva`, pero no en ambos.
- Una cancelación que cumpla las reglas genera un `Reembolso` simulado.
- Cada importe efectivamente cobrado genera una `ComisionPlataforma`.

### Decisiones de modelado

#### Reserva y pago remoto

En una compra remota, el pago digital simulado y la confirmación de la reserva se tratarán como una sola operación lógica. Si el pago no se confirma, la reserva no debe quedar confirmada ni el stock descontado definitivamente.

Los medios disponibles serán tarjeta de débito simulada y Yape simulado.

#### Compra presencial mediante QR

Desde el QR del establecimiento se podrá pagar mediante:

- Tarjeta de débito simulada.
- Yape simulado.
- Efectivo.

Cuando se elija efectivo, la `RetencionStock` separará las unidades temporalmente. El personal confirmará manualmente que recibió el dinero y, en ese momento, el pago y el recojo quedarán completados.

En una compra digital, incluso si se realiza estando dentro del local, el personal confirmará la entrega utilizando el código de recojo.

#### Boleta y código de recojo

La `BoletaSimulada` mostrará claramente que no posee validez tributaria. Incluirá el resumen de la compra y su `CodigoRecojo`.

El personal buscará ese código desde la cuenta del establecimiento. Al confirmar la entrega, el código quedará utilizado y no podrá emplearse nuevamente.

#### Cancelación y devolución de stock

Una reserva remota podrá cancelarse mientras continúe pendiente de recojo y no haya superado el límite definitivo de la oferta.

Al cancelar:

- Se devuelve de forma simulada el 50 % de lo pagado.
- El otro 50 % se conserva como penalización.
- El stock vuelve a estar disponible cuando la oferta todavía puede venderse.
- No se permite cancelar una reserva ya recogida, expirada o cancelada.

Las compras presenciales completadas en el establecimiento no tendrán cancelación mediante la plataforma.

#### Comisión de la plataforma

La comisión será el **10 % del importe que realmente permanezca cobrado**.

Ejemplos:

- Venta completada de S/ 20: la plataforma recibe S/ 2 y el establecimiento S/ 18.
- Reserva de S/ 20 cancelada: se devuelven S/ 10 al consumidor; de los S/ 10 retenidos, la plataforma recibe S/ 1 y el establecimiento S/ 9.

En pagos presenciales en efectivo, la comisión se registrará como un saldo simulado que el establecimiento mantiene con la plataforma.

## Grupo 5 — Experiencia posterior e impacto

### Clases seleccionadas

| Clase | ¿Qué representa? | ¿Por qué existe? |
|---|---|---|
| `Resena` | La calificación y comentario que deja el consumidor después de recibir su compra. | Permite evaluar la experiencia real y construir la reputación del establecimiento. |
| `Notificacion` | Un aviso enviado a un usuario por un suceso importante. | Informa sobre resultados de afiliación, documentos por vencer, suspensiones, recordatorios de recojo y otros cambios relevantes. |
| `ImpactoCO2Evitado` | El registro del impacto ambiental atribuido a una compra recogida. | Permite mostrar cuánto alimento se rescató y una estimación del CO₂ evitado sin definir todavía una fórmula definitiva. |

### Relaciones principales

- Una `Reserva` solo puede recibir una `Resena` después de tener un `Recojo` completado.
- Cada reseña pertenece a un consumidor y califica a un establecimiento.
- Una `Notificacion` tiene un usuario destinatario y se origina por un acontecimiento del sistema.
- Un `Recojo` completado puede generar un registro `ImpactoCO2Evitado`.

### Decisiones de modelado

#### Reseñas verificadas

No se permitirá calificar únicamente por haber pagado. La reseña se habilita después de confirmar el recojo, porque solo entonces el consumidor recibió realmente el pack.

La calificación y el comentario formarán parte de la misma clase `Resena`; no necesitan entidades separadas.

#### Notificaciones como concepto del negocio

La notificación se mantiene como clase porque debe conservar destinatario, contenido, fecha, motivo y estado de lectura. No representa únicamente un mensaje visual momentáneo.

#### Impacto ambiental pendiente de fórmula

`ImpactoCO2Evitado` se incluye porque forma parte de la propuesta de valor del proyecto. Sin embargo, el peso recuperado, los factores de conversión y la fórmula exacta se definirán en una etapa posterior.

Para el primer avance bastará con mostrar que el impacto se registra después de un recojo completado.

## Información calculada que no necesita una entidad propia

El panel empresarial mostrará ventas, comisiones, ingresos netos, cancelaciones, no recojos, calificaciones e impacto. Estos valores se calcularán utilizando las clases ya definidas.

Por ahora no se crearán clases como `EstadisticaEmpresa`, `ReporteVentas` o `Dashboard`, porque representan consultas o presentaciones de datos y no objetos independientes del negocio.

## Elementos que no se contarán como clases de dominio

Algunos conceptos serán necesarios en el código, pero no deben utilizarse para completar artificialmente la cantidad mínima de clases:

- Roles de usuario.
- Estados de cuenta o establecimiento.
- Métodos de pago.
- Controllers.
- Services.
- Repositories.
- DTO de entrada o salida.
- Clases de configuración.

Los roles, estados y métodos de pago se representarán mediante enumeraciones cuando corresponda.

## Conteo verificado

El modelo contiene **31 clases de dominio justificadas**. El mínimo académico para cinco integrantes es de 25 clases, por lo que el proyecto cumple el requisito sin crear conceptos artificiales.

### Usuarios y estructura empresarial

1. `Usuario`
2. `Consumidor`
3. `PropietarioEmpresa`
4. `Empresa`
5. `Establecimiento`
6. `Direccion`
7. `CuentaEstablecimiento`

### Afiliación y revisión documental

8. `SolicitudAfiliacion`
9. `DocumentoEstablecimiento`
10. `RevisionDocumento`
11. `EvaluacionAfiliacion`
12. `AdministradorPlataforma`
13. `SuspensionEstablecimiento`

### Packs y ofertas

14. `PackSorpresa`
15. `OfertaPack`
16. `VariantePack`
17. `Alergeno`
18. `RestriccionAlimentaria`

### Compra, pago y recojo

19. `RetencionStock`
20. `Reserva`
21. `DetalleReserva`
22. `Pago`
23. `BoletaSimulada`
24. `CodigoRecojo`
25. `Recojo`
26. `CancelacionReserva`
27. `Reembolso`
28. `ComisionPlataforma`

### Experiencia e impacto

29. `Resena`
30. `Notificacion`
31. `ImpactoCO2Evitado`

## Persistencia y relaciones verificadas

El código contiene 31 entidades JPA, repartidas en Identidad (7), Afiliación (6), Ofertas (5), Reservas (5), Transacciones (5) y Experiencia (3). El DBML contiene esas 31 tablas y las tablas asociativas `pack_alergenos` y `pack_restricciones`: 33 en total. Esta diferencia de cantidad está justificada y no exige inventar dos clases adicionales.

El código y el DBML actuales representan:

- `AdministradorPlataforma` como entidad independiente, sin herencia de `Usuario`.
- Una evaluación final por solicitud: `EvaluacionAfiliacion.solicitudAfiliacion` es `@OneToOne` con FK única.
- `OfertaPack` asociada a `PackSorpresa` mediante `pack_id`.
- `BoletaSimulada` asociada a `Reserva` mediante `reserva_id`.
- `Reembolso` asociado a `CancelacionReserva` mediante `cancelacion_id`.

El diagrama de clases del informe aún difiere en estas relaciones. No debe declararse coherencia completa por coincidir solo los nombres o el número de clases. Las correcciones concretas están en [REVISION_APF1.md](../informe/REVISION_APF1.md). Este registro describe el mapeo existente; no incorpora nuevas relaciones ni implementa reglas de negocio.
