# Requisitos del sistema

## Propósito

Estos requisitos describen la primera versión de MealFlow y sirven como fuente para el informe, los casos de uso, los prototipos, los diagramas y el código.

Que una función aparezca aquí no significa que deba estar completamente programada en el primer avance. El primer avance exige principalmente análisis, diseño, prototipos y configuración inicial del backend.

## Requisitos funcionales

| ID | Requisito | Actor principal | Prioridad |
|---|---|---|---|
| RF-01 | El sistema permitirá registrar una cuenta con datos personales básicos. | Usuario | Esencial |
| RF-02 | El sistema permitirá iniciar y cerrar sesión. | Usuario | Esencial |
| RF-03 | Una misma cuenta podrá utilizar un perfil de consumidor y un perfil empresarial. | Usuario | Esencial |
| RF-04 | El propietario podrá registrar los datos de su empresa. | Propietario | Esencial |
| RF-05 | El propietario podrá registrar varios establecimientos, cada uno con una ubicación. | Propietario | Esencial |
| RF-06 | El propietario deberá adjuntar los documentos obligatorios de cada establecimiento. | Propietario | Esencial |
| RF-07 | El administrador podrá revisar manualmente cada documento y registrar observaciones. | Administrador | Esencial |
| RF-08 | El administrador podrá aprobar o rechazar la solicitud de un establecimiento e indicar el motivo. | Administrador | Esencial |
| RF-09 | El sistema suspenderá un establecimiento cuando venza un documento obligatorio y permitirá revisar su renovación. | Administrador | Esencial |
| RF-10 | El propietario podrá crear una cuenta operativa para cada establecimiento aprobado. | Propietario | Esencial |
| RF-11 | El establecimiento podrá definir la descripción y los posibles contenidos de su pack sorpresa. | Establecimiento | Esencial |
| RF-12 | El establecimiento podrá publicar una oferta activa con una o más variantes de tamaño, precio y stock. | Establecimiento | Esencial |
| RF-13 | El establecimiento podrá configurar la ventana y el límite definitivo de recojo. | Establecimiento | Esencial |
| RF-14 | El consumidor podrá buscar y filtrar establecimientos por ubicación textual, precio y calificación. | Consumidor | Esencial |
| RF-15 | El consumidor podrá consultar información del establecimiento, oferta, descuentos, alérgenos y restricciones. | Consumidor | Esencial |
| RF-16 | El consumidor podrá comprar una o más unidades disponibles mediante tarjeta o Yape simulados. | Consumidor | Esencial |
| RF-17 | El QR físico del establecimiento abrirá directamente su oferta activa. | Consumidor | Esencial |
| RF-18 | Una compra presencial desde el QR permitirá seleccionar efectivo además de los pagos digitales simulados. | Consumidor | Esencial |
| RF-19 | El sistema podrá retener temporalmente el stock mientras se confirma un pago presencial en efectivo. | Sistema | Esencial |
| RF-20 | Después de confirmar el pago, el sistema generará una boleta simulada y un código único de recojo. | Sistema | Esencial |
| RF-21 | El personal podrá buscar el código y confirmar la entrega del pack. | Establecimiento | Esencial |
| RF-22 | El consumidor podrá cancelar una reserva remota pendiente dentro del límite y recibirá un reembolso simulado del 50 %. | Consumidor | Esencial |
| RF-23 | El consumidor podrá calificar y comentar después de un recojo completado. | Consumidor | Esencial |
| RF-24 | El propietario podrá consultar ventas, comisiones, ingresos, cancelaciones, no recojos, reseñas e impacto por local y de forma consolidada. | Propietario | Importante |
| RF-25 | El sistema enviará avisos sobre solicitudes, documentos, suspensiones y recojos. | Sistema | Importante |
| RF-26 | El sistema registrará de forma conceptual el alimento recuperado y el CO₂ evitado después del recojo. | Sistema | Importante |

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

## Requisitos no funcionales

| ID | Requisito |
|---|---|
| RNF-01 | La interfaz deberá diseñarse primero para celulares y adaptarse a tabletas y computadoras. |
| RNF-02 | El frontend se comunicará con el backend mediante servicios REST sobre HTTP. |
| RNF-03 | El backend utilizará Java 21, Spring Boot y Maven. |
| RNF-04 | El sistema utilizará MySQL y Spring Data JPA para la persistencia. |
| RNF-05 | El backend se organizará como monolito modular por capas. |
| RNF-06 | Las operaciones de reserva y stock deberán mantener consistencia mediante transacciones. |
| RNF-07 | Las contraseñas no deberán almacenarse como texto visible y el acceso deberá limitarse según el perfil. |
| RNF-08 | Los archivos documentales privados no serán visibles para los consumidores. |
| RNF-09 | Los formularios deberán validar campos obligatorios y mostrar mensajes comprensibles. |
| RNF-10 | La interfaz deberá utilizar textos legibles, botones claros y navegación sencilla en pantallas pequeñas. |
| RNF-11 | El código deberá mantener nombres coherentes con los requisitos, diagramas y modelo de dominio. |
| RNF-12 | Toda función simulada o futura deberá identificarse claramente para no aparentar una operación real. |

## Trazabilidad

- Las decisiones que justifican estos requisitos se encuentran en [DECISIONES_PROYECTO.md](../decisiones/DECISIONES_PROYECTO.md).
- Las clases relacionadas se encuentran en [MODELO_DOMINIO.md](../decisiones/MODELO_DOMINIO.md).
- Los procesos completos se explican en [FLUJOS_NEGOCIO.md](../FLUJOS_NEGOCIO.md).

Cuando cambie un requisito, deberán revisarse también sus casos de uso, prototipos, diagramas y clases relacionadas.
