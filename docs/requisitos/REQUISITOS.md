# Requisitos del sistema — Sazloop

## Fuente y estado

Los requisitos corresponden a las secciones 3.1 y 3.2 del [informe del segundo avance](../informe/Avance_del_proyecto_02.docx) y se precisan en su anexo «Definiciones funcionales aprobadas», resumido al final de este documento. Se conservan los 26 IDs, actores, prioridades y dependencias. Los IDs no se renumeran al agrupar por módulos. Si un texto antiguo contradice el anexo aprobado, prevalece el anexo.

El [estado actual](../tareas/ESTADO_ACTUAL.md) indica qué está implementado y cómo se comprobó.

## Requisitos funcionales

### Módulo de Identidad

Este módulo administra el acceso, los perfiles y la estructura
empresarial registrada en la plataforma.

| **ID**    | **Requisito Funcional**                                                                                                                         | **Actor Principal** | **Prioridad** | **Dependencia**                                                    |
|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------|---------------------|---------------|--------------------------------------------------------------------|
| **RF-01** | El sistema deberá permitir que el usuario registre una cuenta mediante sus datos personales básicos.                                            | Usuario             | **Esencial**  | **-**                                                              |
| **RF-02** | El sistema deberá gestionar el acceso del usuario mediante las opciones de inicio y cierre de sesión.                                           | Usuario             | **Esencial**  | **-**                                                              |
| **RF-03** | El sistema deberá permitir que una misma cuenta utilice un perfil de consumidor y un perfil empresarial.                                        | Usuario             | **Esencial**  | **-**                                                              |
| **RF-04** | El sistema deberá permitir que el propietario registre los datos de su empresa.                                                                 | Propietario         | **Esencial**  | **-**                                                              |
| **RF-05** | El sistema deberá permitir que el propietario registre uno o más establecimientos asociados a su empresa, cada uno con su respectiva ubicación. | Propietario         | **Esencial**  | **-**                                                              |
| **RF-10** | El sistema deberá permitir que el propietario cree una cuenta operativa para cada establecimiento previamente aprobado.                         | Propietario         | **Esencial**  | Solo se habilita después de la aprobación realizada en Afiliación. |

### Módulo de Afiliación

Este módulo administra la presentación, revisión y vigencia de los
documentos requeridos para aprobar cada establecimiento.

| **ID**    | **Requisito Funcional**                                                                                                                                                                   | **Actor Principal** | **Prioridad** | **Dependencia** |
|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------|---------------|-----------------|
| **RF-06** | El sistema deberá permitir que el propietario adjunte los documentos obligatorios correspondientes a cada establecimiento.                                                                | Propietario         | **Esencial**  | **-**           |
| **RF-07** | El sistema deberá permitir que el administrador registre el resultado de la revisión manual de cada documento, junto con las observaciones correspondientes.                              | Administrador       | **Esencial**  | **-**           |
| **RF-08** | El sistema deberá permitir que el administrador registre la aprobación o el rechazo de la solicitud de un establecimiento e indique el motivo de la decisión.                             | Administrador       | **Esencial**  | **-**           |
| **RF-09** | El sistema deberá suspender automáticamente un establecimiento cuando venza uno de sus documentos obligatorios y mantener la suspensión hasta que el administrador apruebe su renovación. | Administrador       | **Esencial**  | **-**           |

### Módulo de Ofertas

Este módulo administra la definición, publicación y consulta de los
packs sorpresa disponibles.

| **ID**    | **Requisito Funcional**                                                                                                                                              | **Actor Principal** | **Prioridad** | **Dependencia** |
|-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------|---------------|-----------------|
| **RF-11** | El sistema deberá permitir que el establecimiento registre la descripción y los posibles contenidos de su pack sorpresa.                                             | Establecimiento     | **Esencial**  | **-**           |
| **RF-12** | El sistema deberá permitir que el establecimiento publique una sola oferta activa de pack sorpresa con una o más variantes de tamaño, precio y stock.                | Establecimiento     | **Esencial**  | **-**           |
| **RF-13** | El sistema deberá permitir que el establecimiento configure el inicio y el fin del horario de recojo de los packs sorpresa. El fin del horario es el único límite para entregar o cancelar una reserva. | Establecimiento     | **Esencial**  | **-**           |
| **RF-14** | El sistema deberá permitir que el consumidor busque y filtre establecimientos por distrito, dirección textual, precio y calificación.                                | Consumidor          | **Esencial**  | **-**           |
| **RF-15** | El sistema deberá mostrar al consumidor la información del establecimiento, la oferta activa, el descuento, los posibles alérgenos y las restricciones alimentarias. | Consumidor          | **Esencial**  | **-**           |
| **RF-17** | El sistema deberá mostrar directamente la oferta activa de un establecimiento cuando el consumidor acceda mediante su código QR físico.                              | Consumidor          | **Esencial**  | **-**           |

### Módulo de Reservas

Este módulo administra la disponibilidad temporal del stock, la
cancelación y la entrega presencial de los packs.

| **ID**    | **Requisito Funcional**                                                                                                                                                             | **Actor Principal** | **Prioridad** | **Dependencia**                                                                  |
|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------|---------------|----------------------------------------------------------------------------------|
| **RF-19** | El sistema deberá retener temporalmente el stock seleccionado mientras se confirma el pago presencial en efectivo.                                                                  | Sistema             | **Esencial**  | Consulta el stock de Ofertas mientras se confirma la operación en Transacciones. |
| **RF-21** | El sistema deberá permitir que el personal del establecimiento busque el código único de recojo y confirme la entrega del pack.                                                     | Establecimiento     | **Esencial**  | **-**                                                                            |
| **RF-22** | El sistema deberá procesar la cancelación de una reserva remota pendiente cuando el consumidor la solicite dentro del límite establecido, aplicando un reembolso simulado del 50 %. | Consumidor          | **Esencial**  | Solicita a Transacciones el registro del reembolso simulado.                     |

### Módulo de Transacciones

Este módulo administra los pagos simulados y los comprobantes asociados
a las operaciones.

| **ID**    | **Requisito Funcional**                                                                                                                                                                                      | **Actor Principal** | **Prioridad** | **Dependencia**                                                        |
|-----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------|---------------|------------------------------------------------------------------------|
| **RF-16** | El sistema deberá permitir que el consumidor compre una o más unidades disponibles mediante pagos simulados con tarjeta de débito o Yape.                                                                    | Consumidor          | **Esencial**  | Utiliza la oferta seleccionada y origina una reserva.                  |
| **RF-18** | El sistema deberá permitir que el consumidor seleccione el pago en efectivo durante una compra presencial iniciada mediante el código QR del establecimiento, además de las modalidades digitales simuladas. | Consumidor          | **Esencial**  | **-**                                                                  |
| **RF-20** | El sistema deberá generar una boleta simulada y un código único de recojo después de confirmar el pago de la operación.                                                                                      | Sistema             | **Esencial**  | Genera un código utilizado posteriormente por Reservas para el recojo. |

### Módulo de Experiencia

Este módulo administra las reseñas, los indicadores, los reportes y las
notificaciones posteriores o complementarias a la operación principal.

| **ID**    | **Requisito Funcional**                                                                                                                                                                                                | **Actor Principal** | **Prioridad**  | **Dependencia**                                             |
|-----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------|----------------|-------------------------------------------------------------|
| **RF-23** | El sistema deberá permitir que el consumidor registre una calificación obligatoria y, opcionalmente, un comentario después de completar el recojo de su pedido.                                                       | Consumidor          | **Esencial**   | **-**                                                       |
| **RF-24** | El sistema deberá mostrar al propietario un panel con información de ventas, comisiones, ingresos, cancelaciones, reservas no recogidas, reseñas e indicadores de impacto por establecimiento y de manera consolidada. La comisión de Sazloop es el 10 % del importe que permanece cobrado; si el consumidor cancela, se calcula sobre el 50 % retenido. | Propietario         | **Importante** | Consolida información de Ofertas, Reservas y Transacciones. |
| **RF-25** | El sistema deberá enviar notificaciones a los usuarios correspondientes sobre el estado de las solicitudes, los documentos, las suspensiones y el recojo de los pedidos.                                               | Sistema             | **Importante** | Recibe eventos generados por Afiliación y Reservas.         |
| **RF-26** | El sistema deberá registrar de manera conceptual los indicadores de alimento recuperado y CO₂ evitado después de completar el recojo de un pedido.                                                                     | Sistema             | **Importante** | **-**                                                       |

## Reglas funcionales centrales

- Cada establecimiento se revisa y aprueba independientemente.
- Un establecimiento solo puede tener una oferta activa a la vez.
- Cada variante controla su propio precio y stock.
- El comercio no puede cancelar una oferta que ya tenga reservas confirmadas.
- Una reserva no puede quedar confirmada si el pago correspondiente falla.
- Un código de recojo utilizado no puede reutilizarse.
- Solo una reserva recogida puede recibir una reseña.
- Una reserva expirada o recogida no puede cancelarse.
- Los pagos digitales, reembolsos y boletas son simulados.
- La plataforma no garantiza el contenido exacto del pack, pero debe informar posibles alérgenos y restricciones.

## Precisiones del avance 2 (anexo aprobado)

- **RF-13:** cada oferta define inicio y fin del horario de recojo. El fin es el único límite: después no se entrega ni se cancela, y la reserva pagada no recogida pasa a expirada. No hay límite definitivo adicional.
- **RF-09:** siete días antes del vencimiento de un documento se avisa al propietario; si vence sin renovación aprobada, el local se suspende automáticamente. La renovación incluye solo los documentos vencidos y la aprueba un administrador. Durante la suspensión se conservan la cuenta, el historial y la entrega de reservas pagadas.
- **RF-25:** se notifican el resultado de solicitudes y renovaciones, los documentos por vencer, las suspensiones y los cambios del pedido. El recordatorio de recojo se crea al confirmar la compra.
- **RF-26:** cada recojo completado (entrega con código o compra presencial en efectivo) registra su impacto conceptual. El impacto visible es el número de packs entregados, sin cifras de kilos ni CO₂.
- **Panel del trabajador:** stock disponible, ganancia neta de pedidos entregados (descontada la comisión del 10 %) y unidades sin vender por día, semana de lunes a domingo y mes, en hora de Perú.
- **Panel empresarial:** las ventas cuentan reservas pagadas aunque no se hayan recogido; las cancelaciones se informan aparte y su importe retenido sí suma en ingresos y comisiones.

El detalle de cada regla y su justificación están en [Decisiones del proyecto](../decisiones/DECISIONES_PROYECTO.md) (DEC-008, DEC-009, DEC-030, DEC-035, DEC-036 y DEC-043 a DEC-047).

## Requisitos no funcionales

La tabla contiene los seis criterios del informe del primer avance. Las decisiones técnicas y de privacidad se documentan en Arquitectura y Decisiones.

| Criterio | Descripción del requisito no funcional |
|---|---|
| Usabilidad (Mobile-First) | La interfaz web debe diseñarse y maquetarse bajo el enfoque mobile-first, adaptándose a dispositivos móviles sin degradar la experiencia de usuario. |
| Rendimiento | El sistema debe responder a las consultas de búsqueda y procesamiento de compras simuladas en tiempos mínimos (de 2 a 3 segundos). |
| Arquitectura | La aplicación backend debe desarrollarse siguiendo una estructura monolítica modular organizada por capas (Controlador, Servicio, Repositorio, Modelo). |
| Tecnología y Stack | La solución debe ser construida con Java 21, Spring Boot, Maven, Spring Data JPA, MySQL en el backend; y HTML, CSS, JavaScript y Tailwind CSS en el frontend. |
| Seguridad de Acceso | Las cuentas operativas de establecimientos y usuarios deben mantener la separación estricta de roles y permisos mediante autenticación segura. |
| Persistencia e Integridad | Las transacciones de reservas y control de stock retenido deben mantener consistencia e integridad relacional en MySQL para evitar la sobreventa de packs. |

La formulación de rendimiento se conserva como aparece en el informe. Las condiciones de medición y los resultados de pruebas se documentarán cuando se realicen las pruebas de la implementación.

## Relación con los demás elementos

- [Decisiones](../decisiones/DECISIONES_PROYECTO.md).
- [Modelo de dominio](../decisiones/MODELO_DOMINIO.md).
- [Flujos](../FLUJOS_NEGOCIO.md).
- [Diagramas](../diagramas/README.md).

La distribución funcional de un RF no obliga a que todas sus entidades estén en el mismo módulo. Por ejemplo, Reservas coordina RF-22, mientras `CancelacionReserva` y `Reembolso` pertenecen a Transacciones. Los módulos colaboran; no se cambian esas clases de paquete solo por la agrupación del requisito.
