# Proyecto Sazloop

> **Lo bueno sigue fluyendo.**

## Identidad

- Proyecto asignado: **N.º 91 — SalvemosLaComida**, según la confirmación del equipo.
- Plataforma: **Sazloop**.
- Nombre anterior: MealFlow; permanece en la URL del repositorio y en el identificador técnico de la base MySQL existente.
- Ámbito de la primera versión: **Lima Metropolitana**.

## Resumen

Sazloop es una plataforma web mobile-first que busca reducir el desperdicio de alimentos preparados.

Los restaurantes, panaderías y otros establecimientos afiliados podrán ofrecer sus excedentes del día mediante packs sorpresa con descuento. Los consumidores podrán encontrarlos, comprarlos mediante pagos simulados y recogerlos personalmente en el local.

La plataforma no será un servicio de delivery.

## Problema

El problema abordado es el descarte de excedentes alimentarios todavía aptos para el consumo en establecimientos de Lima Metropolitana. La variación de la demanda, la disponibilidad superior a lo vendido y el tiempo limitado para aprovechar productos de corta duración pueden contribuir a este descarte. Sus consecuencias incluyen pérdidas económicas para los establecimientos y desaprovechamiento de alimentos y recursos.

Esta delimitación procede del capítulo 1 del informe del primer avance; no supone que se haya realizado un estudio de campo ni que establecimientos concretos hayan confirmado estas pérdidas.

## Objetivos

### Objetivo general

Desarrollar una plataforma web mobile-first dirigida a establecimientos
de alimentos y consumidores de Lima Metropolitana, orientada a la
reducción del desperdicio alimentario mediante la comercialización de
excedentes aptos para el consumo en packs sorpresa con recojo
presencial.

### Objetivos específicos

1.  Diseñar la arquitectura, el modelo de dominio, el modelo de datos y
    las interfaces de la plataforma de acuerdo con los requerimientos
    funcionales y no funcionales establecidos.

2.  Implementar la gestión de usuarios, empresas, establecimientos y
    ofertas de packs sorpresa, con revisión documental manual y
    administración independiente de cada local.

3.  Desarrollar el flujo transaccional del consumidor para la búsqueda,
    reserva, pago simulado, cancelación y recojo presencial de packs
    sorpresa, con control de stock, código único de recojo y registro
    posterior de reseñas.

4.  Validar el funcionamiento y la coherencia de la plataforma con los
    requerimientos, modelos, base de datos e interfaces mediante pruebas
    funcionales y técnicas.


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

Las funciones futuras que aparezcan en un prototipo deben identificarse como no disponibles en V1. El informe menciona delivery externo y GPS como investigación futura; no modifican los RF actuales.

## Modelo de negocio

La documentación previa registra una comisión del 10 % sobre el importe que efectivamente permanezca cobrado (DEC-036). Esta tasa está **documentada, pero pendiente de confirmación formal por el equipo**; el RF-24 del informe solicita mostrar comisiones, pero no especifica un porcentaje. Su cálculo aún no está implementado.

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

El POM declara Spring Boot 4.1.1 y Java 21. H2 está configurada para desarrollo; MySQL sigue siendo la base prevista de persistencia. El frontend está planificado, aún sin código en `main`.

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
