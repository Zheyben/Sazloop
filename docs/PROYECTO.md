# Proyecto MealFlow

> **Lo bueno sigue fluyendo.**

## Resumen

MealFlow es una plataforma web mobile-first que busca reducir el desperdicio de alimentos preparados.

Los restaurantes, panaderías y otros establecimientos afiliados podrán ofrecer sus excedentes del día mediante packs sorpresa con descuento. Los consumidores podrán encontrarlos, comprarlos mediante pagos simulados y recogerlos personalmente en el local.

La plataforma no será un servicio de delivery.

## Problema y oportunidad

Al finalizar la jornada, muchos establecimientos descartan alimentos que todavía se encuentran en condiciones adecuadas para el consumo. Esto genera pérdida económica para el comercio y desperdicio de recursos.

Al mismo tiempo, estudiantes, jóvenes profesionales y familias buscan alimentos a precios accesibles y muestran interés por prácticas de consumo responsables.

La oportunidad consiste en conectar a ambos grupos antes de que los productos sean descartados.

## Propuesta de valor

### Para el consumidor

- Acceder a alimentos de establecimientos conocidos por una fracción de su precio referencial.
- Encontrar opciones cercanas mediante filtros de ubicación textual.
- Participar en la reducción del desperdicio alimentario.

### Para el establecimiento

- Recuperar parte del costo de alimentos que podrían terminar descartados.
- Gestionar el stock y horario de sus packs.
- Obtener calificaciones y comentarios de clientes verificados.
- Consultar ventas, comisiones e impacto generado.

## Usuarios y actores

| Actor | Función principal |
|---|---|
| Consumidor | Busca, compra, recoge y califica packs sorpresa. |
| Propietario de empresa | Registra la empresa y sus establecimientos, crea cuentas operativas y consulta resultados generales. |
| Cuenta del establecimiento | Publica la oferta del local y confirma pagos presenciales o recojos. |
| Administrador de plataforma | Revisa manualmente documentos y decide sobre las solicitudes de afiliación. |

## Organización comercial

Una empresa puede tener varios establecimientos físicos. Cada establecimiento:

- Tiene una sola ubicación.
- Presenta y renueva sus propios documentos.
- Es aprobado o suspendido de forma independiente.
- Posee su propia cuenta operativa después de ser aprobado.
- Gestiona su oferta y sus reservas sin acceder a otros locales.

El propietario puede observar información consolidada de todos los establecimientos de su empresa.

## Alcance funcional de la primera versión

La primera versión contempla:

- Registro e inicio de sesión.
- Una sola identidad con perfil de consumidor y perfil empresarial.
- Registro de empresa y establecimientos.
- Presentación y revisión manual de documentos por establecimiento.
- Aprobación, rechazo y suspensión de establecimientos.
- Una oferta activa por establecimiento con tamaños, precios y stock diferentes.
- Búsqueda por distrito, dirección, precio y calificación.
- Visualización de posibles productos, alérgenos y restricciones alimentarias.
- Pagos simulados mediante tarjeta de débito o Yape.
- Compra presencial mediante QR, con posibilidad adicional de pago en efectivo.
- Boleta simulada y código único de recojo.
- Confirmación del recojo por el personal del establecimiento.
- Cancelación de reservas remotas bajo las reglas definidas.
- Calificación y comentario después de un recojo completado.
- Panel empresarial con ventas, comisiones, ingresos y métricas generales.
- Registro conceptual del alimento recuperado y CO₂ evitado.

## Fuera del alcance inicial

No se implementará durante la primera versión:

- Delivery.
- Pagos reales o conexión con entidades financieras.
- GPS, mapas o seguimiento en tiempo real.
- Lectura automática de documentos mediante OCR.
- Certificación sanitaria emitida por la plataforma.
- Aplicación móvil nativa.
- Inicio de sesión con Google.
- Registro individual de empleados del establecimiento.
- Varios tipos de packs activos simultáneamente en un mismo local.
- Fórmula científica definitiva para calcular alimento recuperado o CO₂ evitado.
- Microservicios.

Las funciones futuras que aparezcan en un prototipo deberán mostrarse como “Próximamente”.

## Modelo de negocio

La plataforma retendrá una comisión del 10 % sobre el importe que efectivamente permanezca cobrado.

Los pagos serán simulados. Si el pago presencial se realiza en efectivo, la comisión quedará registrada como un saldo simulado del establecimiento con la plataforma.

## Tecnologías acordadas

| Componente | Tecnología |
|---|---|
| Lenguaje backend | Java 21 |
| Backend | Spring Boot |
| Gestión del proyecto | Maven |
| Frontend | HTML, CSS y JavaScript |
| Estilos | Tailwind CSS |
| Base de datos | MySQL |
| Persistencia | Spring Data JPA |

La versión exacta de Spring Boot se registrará cuando se genere el proyecto base.

## Arquitectura

Se utilizará un monolito modular organizado por capas. El detalle se encuentra en [ARQUITECTURA.md](ARQUITECTURA.md).

## Fechas académicas confirmadas

- Primer avance: semana 5, martes a las 6:00 p. m.
- Segundo avance: semana 10, martes a las 6:00 p. m.
- Entrega final: semana 18, lunes a las 6:00 p. m.

La referencia a la semana 14 en el PDF del curso fue confirmada como un error.

## Equipo

- Emanuel
- Daniel
- Franco
- Piero
- Alejandro
