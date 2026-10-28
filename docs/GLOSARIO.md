# Glosario de Sazloop

Vocabulario común para documentos, código y conversaciones del equipo. Las relaciones detalladas están en el [Modelo de dominio](decisiones/MODELO_DOMINIO.md).

| Término | Significado |
|---|---|
| Usuario / persona | Identidad personal para ingresar como consumidor, propietario o ambos. |
| Consumidor | Perfil que compra y recoge packs. |
| Propietario | Perfil de la persona que registra una empresa y administra sus locales. |
| Empresa | Organización que agrupa establecimientos. |
| Establecimiento / local | Punto físico con ubicación, documentos, aprobación y operación propias. |
| Cuenta operativa / trabajador | Acceso compartido del personal de un local; no representa empleados registrados individualmente. |
| Administrador | Cuenta interna que revisa documentos y decide solicitudes. |
| Afiliación | Solicitud y revisión necesarias para permitir operar a un establecimiento. |
| Aprobación del local | Decisión administrativa que habilita su operación; no debe confundirse con una afirmación permanente de vigencia documental. |
| Vigencia documental | Situación de las fechas de los documentos aprobados: vigente, por vencer, vencido o sin vencimiento. |
| Renovación | Nueva solicitud con documentos vencidos o por vencer; conserva el historial anterior. |
| Suspensión | Bloqueo de nuevas ventas y publicaciones; conserva historial y entrega de compras ya pagadas. |
| Pack sorpresa | Descripción reutilizable; no garantiza un contenido exacto. |
| Oferta | Publicación concreta de un pack con horario, variantes y QR propios. |
| Variante | Tamaño con precio y stock propios dentro de una oferta. |
| Stock disponible | Unidades que todavía pueden comprarse; no incluye las retenidas para efectivo. |
| Carrito | Selección temporal en el navegador; no reserva ni retiene unidades. |
| Retención | Unidades apartadas temporalmente mientras el trabajador confirma un pago en efectivo. |
| Reserva / compra | Registro de la operación. `PENDIENTE_PAGO` no es una compra confirmada. |
| Pago simulado | Registro académico sin movimiento de dinero real; validar un formato no confirma un pago bancario. |
| Boleta simulada | Resumen interno sin validez tributaria. |
| Código de recojo | Código único de una reserva; se utiliza una sola vez para su entrega o cobro presencial. |
| Recojo / entrega | Recepción física confirmada por el trabajador; pagar por sí solo no acredita entrega. |
| No recojo | Compra pagada cuyo horario terminó sin entrega. |
| Cancelación | Finalización permitida de una reserva remota todavía confirmada y antes del fin del horario. |
| Reembolso / comisión | Importes simulados; reglas económicas en DEC-036 y DEC-035. |
| Reseña | Nota y comentario sobre una compra recogida. D2 permite una por compra; el código aún limita una por consumidor/local. |
| QR de oferta | Enlace a una publicación concreta; no es el código de recojo ni un QR permanente del establecimiento. |
| Notificación | Aviso guardado en el buzón personal, con estado de lectura. |
| Impacto conceptual | Registro posterior al recojo; por ahora se muestran packs entregados, sin estimación de kilos ni CO₂. |
| Mobile-first | Diseño que parte de celulares y se adapta a pantallas mayores. |
| V1 | Primera versión académica del sistema. |

Un requisito describe lo que debe cumplirse; una decisión registra un acuerdo; encontrar código acredita implementación, y una prueba ejecutada solo acredita el escenario y entorno comprobados. El [estado del proyecto](PROYECTO.md) mantiene esa separación.
