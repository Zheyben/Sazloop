# Glosario del proyecto

Este glosario establece los términos que utilizarán el informe, los diagramas, los prototipos y el código.

| Término | Significado |
|---|---|
| Sazloop | Nombre de la plataforma del proyecto N.º 91 — SalvemosLaComida. |
| Usuario | Identidad principal utilizada para registrarse e iniciar sesión. |
| Consumidor | Perfil que busca, compra, recoge y califica packs. |
| Propietario de empresa | Perfil que administra la empresa y observa sus establecimientos. |
| Empresa | Organización comercial que puede poseer uno o varios establecimientos. |
| Establecimiento | Local físico individual con ubicación, documentos, oferta y operación propias. |
| Cuenta del establecimiento | Acceso operativo compartido por el personal de un único local. |
| Afiliación | Proceso mediante el cual se solicita que un establecimiento pueda operar en la plataforma. |
| Documento del establecimiento | Archivo y datos presentados para comprobar un requisito del local. |
| Revisión documental | Evaluación manual de un documento por un administrador. |
| Pack sorpresa | Definición general del conjunto de alimentos cuyo contenido exacto no se garantiza antes de la compra. |
| Oferta de pack | Publicación del pack para una fecha, horario y disponibilidad determinados. |
| Variante de pack | Tamaño disponible dentro de la oferta, con precio y stock propios. |
| Stock | Cantidad de unidades disponibles de una variante. |
| Retención de stock | Separación temporal de unidades mientras se confirma una compra. |
| Reserva | Registro de una operación de compra. El modelo contempla un estado inicial pendiente de pago; la reserva solo queda confirmada después de confirmar el pago. |
| Detalle de reserva | Variante, cantidad y precio conservados dentro de una reserva. |
| Pago simulado | Registro académico de un pago que no mueve dinero real. |
| Boleta simulada | Resumen interno de la compra sin validez tributaria. |
| Código de recojo | Código único que el consumidor presenta para recibir el pack. |
| Recojo | Entrega presencial del pack y cierre satisfactorio de la reserva. |
| No recojo | Reserva que superó su límite definitivo sin ser entregada. |
| Cancelación | Finalización solicitada de una reserva remota antes de su recojo y dentro del tiempo permitido. |
| Reembolso simulado | Registro de la devolución del 50 % del importe pagado cuando corresponde. |
| Comisión | Porción del importe cobrado que corresponde a la plataforma. |
| Reseña | Calificación y comentario habilitados después de un recojo completado. |
| QR presencial | Enlace físico del establecimiento que abre directamente su oferta activa. |
| Administrador de plataforma | Cuenta interna que revisa documentos y solicitudes. |
| Mobile-first | Diseño que comienza por la experiencia en celulares y se adapta a pantallas mayores. |
| V1 | Primera versión académica y funcional del sistema. |

## Diferencias que no deben confundirse

- **Empresa y establecimiento:** la empresa agrupa; el establecimiento es un local físico.
- **Pack y oferta:** el pack describe; la oferta publica esa descripción para un día y horario.
- **Oferta y variante:** la oferta es una publicación; la variante es un tamaño con precio y stock.
- **Retención y reserva:** la retención es temporal; la reserva solo queda confirmada cuando el pago queda confirmado. Un registro en estado `PENDIENTE_PAGO` no equivale a una compra confirmada.
- **Pago y recojo:** pagar no demuestra que el consumidor recibió el pack.
- **Rechazo y suspensión:** se rechaza una solicitud pendiente; se suspende un establecimiento que ya estaba aprobado.
