# Modelado del dominio — SalvemosLaComida

## Propósito del documento

Este documento explica las clases de dominio propuestas para SalvemosLaComida, qué representa cada una y por qué es necesaria.

Su finalidad es que todos los integrantes comprendan el modelo antes de elaborar el diagrama de clases y comenzar la programación. El contenido se ampliará por grupos conforme avance el análisis del proyecto.

> **Estado:** En construcción. Las clases aquí registradas corresponden a decisiones ya revisadas, pero podrán ajustarse si durante el desarrollo aparece una necesidad real.

## Criterios para escoger las clases

- Cada clase debe representar una persona, objeto, proceso o concepto importante del negocio.
- No se crearán clases artificiales solamente para alcanzar el mínimo solicitado por el curso.
- Los nombres deberán mantenerse, en lo posible, en los requerimientos, diagramas, base de datos y código.
- Los valores que solo tengan una lista cerrada de opciones, como estados, roles o métodos de pago, se representarán mediante enumeraciones.
- Los controllers, services, repositories y DTO no son clases de dominio.
- El modelo de esta primera versión debe cubrir el alcance académico sin añadir funciones demasiado avanzadas.

## Visión general del dominio

SalvemosLaComida será una plataforma web mobile-first en la que los establecimientos de comida podrán ofrecer sus excedentes mediante packs sorpresa. Los consumidores podrán encontrarlos, comprarlos mediante pagos simulados y recogerlos presencialmente.

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

## Conteo actual

Hasta este punto se han definido **13 clases de dominio justificadas**:

1. `Usuario`
2. `Consumidor`
3. `PropietarioEmpresa`
4. `Empresa`
5. `Establecimiento`
6. `Direccion`
7. `CuentaEstablecimiento`
8. `SolicitudAfiliacion`
9. `DocumentoEstablecimiento`
10. `RevisionDocumento`
11. `EvaluacionAfiliacion`
12. `AdministradorPlataforma`
13. `SuspensionEstablecimiento`

El siguiente grupo por modelar será el correspondiente a los packs sorpresa, sus variantes y las ofertas publicadas.
