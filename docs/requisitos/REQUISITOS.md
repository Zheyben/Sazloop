# Requisitos del sistema — Sazloop

## Fuente y estado

Sincronizados el 26 de septiembre de 2026 con las secciones 3.1 y 3.2 de [Informe_Primer_Avance.docx](../informe/Informe_Primer_Avance.docx). Se conservan los 26 IDs, descripciones, actores, prioridades y dependencias del informe corregido. Los IDs no se renumeran al agrupar por módulos.

Son requisitos de la solución; su presencia aquí no acredita implementación. Las operaciones del backend y el frontend todavía están pendientes. La revisión del APF1 está en [REVISION_APF1.md](../informe/REVISION_APF1.md).

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
| **RF-13** | El sistema deberá permitir que el establecimiento configure el horario y la hora límite para el recojo de los packs sorpresa.                                        | Establecimiento     | **Esencial**  | **-**           |
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
| **RF-23** | El sistema deberá permitir que el consumidor registre una calificación y un comentario después de completar el recojo de su pedido.                                                                                    | Consumidor          | **Esencial**   | **-**                                                       |
| **RF-24** | El sistema deberá mostrar al propietario un panel con información de ventas, comisiones, ingresos, cancelaciones, reservas no recogidas, reseñas e indicadores de impacto por establecimiento y de manera consolidada. | Propietario         | **Importante** | Consolida información de Ofertas, Reservas y Transacciones. |
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

## Requisitos no funcionales

Se reproduce la tabla de seis criterios del informe. Sustituye la lista anterior de 12 RNF de este archivo, disponible en el historial de Git; las decisiones técnicas y de privacidad continúan documentadas en Arquitectura y Decisiones.

| Criterio | Descripción del requisito no funcional |
|---|---|
| Usabilidad (Mobile-First) | La interfaz web debe diseñarse y maquetarse bajo el enfoque mobile-first, adaptándose a dispositivos móviles sin degradar la experiencia de usuario. |
| Rendimiento | El sistema debe responder a las consultas de búsqueda y procesamiento de compras simuladas en tiempos mínimos (de 2 a 3 segundos). |
| Arquitectura | La aplicación backend debe desarrollarse siguiendo una estructura monolítica modular organizada por capas (Controlador, Servicio, Repositorio, Modelo). |
| Tecnología y Stack | La solución debe ser construida con Java 21, Spring Boot, Maven, Spring Data JPA, MySQL en el backend; y HTML, CSS, JavaScript y Tailwind CSS en el frontend. |
| Seguridad de Acceso | Las cuentas operativas de establecimientos y usuarios deben mantener la separación estricta de roles y permisos mediante autenticación segura. |
| Persistencia e Integridad | Las transacciones de reservas y control de stock retenido deben mantener consistencia e integridad relacional en MySQL para evitar la sobreventa de packs. |

**Pendiente de precisar en el informe:** Rendimiento usa “tiempos mínimos (de 2 a 3 segundos)”. Debe expresar un límite máximo verificable y las condiciones de medición; no se han inventado usuarios concurrentes ni resultados de pruebas para completar ese dato.

## Relación con los demás elementos

- [Decisiones](../decisiones/DECISIONES_PROYECTO.md).
- [Modelo de dominio](../decisiones/MODELO_DOMINIO.md).
- [Flujos](../FLUJOS_NEGOCIO.md).
- [Diagramas](../diagramas/README.md).

La distribución funcional de un RF no obliga a que todas sus entidades estén en el mismo módulo. Por ejemplo, Reservas coordina RF-22, mientras `CancelacionReserva` y `Reembolso` pertenecen a Transacciones. Los módulos colaboran; no se cambian esas clases de paquete solo por la agrupación del requisito.
