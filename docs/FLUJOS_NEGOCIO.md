# Flujos de negocio de Sazloop

Estos recorridos permiten seguir las operaciones del sistema. El [estado y las pruebas](PROYECTO.md) distinguen lo implementado de lo comprobado; los [requisitos](requisitos/REQUISITOS.md) conservan el comportamiento exigido y las [decisiones](decisiones/DECISIONES_PROYECTO.md) explican las reglas.

## 1. Registro y afiliación

1. La persona crea una cuenta e ingresa. Al registrar su empresa obtiene el perfil propietario sin duplicar credenciales.
2. Registra uno o más locales, cada uno con su ubicación.
3. Adjunta archivos privados a una solicitud del local y la envía a revisión.
4. El administrador descarga los archivos, registra aprobado/rechazado y observaciones; la pantalla actual guarda revisión y resolución juntas.
5. Si se aprueba, el local queda activo y el propietario puede crear su cuenta operativa. Si se rechaza, se conserva la información y se presenta un nuevo intento.

**Comprobaciones actuales:** propiedad del local, solicitud editable, adjuntos no vacíos de tipo admitido y hasta 10 MB, coherencia de fechas y al menos un adjunto al enviar. No se permite aprobar una solicitud con documentos rechazados o vencidos. La lista exacta de documentos obligatorios está pendiente (D3); no se atribuye al código una validación de esa lista.

La pantalla utiliza `/resolucion`; se conservan temporalmente las rutas anteriores de revisión y decisión (D5), detalladas en [Arquitectura](ARQUITECTURA.md).

## 2. Publicación y QR

1. El trabajador ingresa con la cuenta de su establecimiento.
2. Elige un pack anterior o define uno nuevo, con posibles contenidos y advertencias alimentarias.
3. Configura variantes, precios, stock e inicio y fin del recojo.
4. Publica si el local está activo y no tiene otra oferta activa sin vencer.
5. La oferta genera su propio QR; el cliente abre esa publicación mediante su identificador y token (D1).

La tolerancia de cinco minutos en el inicio permite publicar con la hora actual del navegador. Al crear otra oferta se utiliza otro QR. El código anterior continúa asociado a su publicación anterior y no redirige a la nueva.

**Pendiente de decisión:** cierre anticipado. Hoy se espera al fin de la oferta para publicar otra; no existe una acción de cierre o cancelación anticipada identificada. Se conserva la regla que prohíbe cancelar una oferta con reservas confirmadas si esa operación se desarrolla.

## 3. Compra remota

1. El consumidor consulta ofertas y filtros de distrito, dirección, precio y calificación.
2. Agrega variantes y cantidades al carrito; esto todavía no retiene stock.
3. Elige tarjeta de débito o Yape simulados y confirma uno o varios pedidos.
4. El backend comprueba oferta, local, cantidades y stock; registra conjuntamente reserva, descuento de unidades y pago confirmado.
5. Cada pedido obtiene su boleta, código, comisión y recordatorio en Notificaciones. Si falla una compra del lote, no se debe conservar un lote parcialmente confirmado.

La validación de tarjeta/Yape en pantalla es de formato y no consulta entidades financieras. No hay un escenario propio de pago digital rechazado implementado; si debe simularse, continúa pendiente de decisión. No se debe confundir el rechazo por stock con el rechazo de un pago.

## 4. Compra presencial por QR

El cliente abre el QR de una oferta e ingresa con su cuenta. Puede elegir pago digital simulado o efectivo.

| Medio | Recorrido actual |
|---|---|
| Digital | Se valida el token y se confirma la compra como presencial. Se genera código y el trabajador confirma la entrega. |
| Efectivo | Se crea una reserva pendiente y se retienen unidades hasta diez minutos, o hasta el fin del horario si llega antes. El trabajador confirma cobro y entrega juntos; se generan boleta, comisión e impacto. |

Si el efectivo no se confirma a tiempo, una tarea libera las unidades y vence la reserva y el código. No hay cobro confirmado ni reembolso en ese caso. Las compras presenciales no admiten la cancelación remota.

## 5. Entrega y reseña

El trabajador busca el código de una compra de su local. Si está pagada, el código está pendiente y todavía no llegó el fin del horario, confirma la entrega y deja el código utilizado. Puede entregar desde el pago, incluso antes del inicio anunciado (DEC-048). Después del fin no se permite entregar ni cancelar; una tarea marca las compras pagadas no recogidas como expiradas y avisa al consumidor.

Cada entrega registra impacto conceptual y permite calificar. **D2 aprobó una reseña como máximo por compra recogida**, con nuevas reseñas para compras diferentes del mismo establecimiento. El código aún aplica una por consumidor/local: su adaptación está pendiente. La interfaz también permite omitir la reseña de una compra; su relación con la obligatoriedad de calificar sigue pendiente de definición.

## 6. Cancelación remota

El consumidor puede cancelar una compra propia de origen remoto que siga confirmada y esté antes del fin del horario. Se devuelve de forma simulada el 50 %, se recalcula la comisión sobre el importe retenido y se invalida el código. El stock solo vuelve si la oferta y el local siguen disponibles para vender.

Una compra recogida, cancelada o expirada no admite esta operación. La regla económica y su ejemplo se mantienen en DEC-036 para no repetir el cálculo en todos los documentos.

## 7. Vencimiento y renovación de documentos

1. Una revisión programada avisa una vez cuando un documento aprobado entra en los próximos siete días de vencimiento.
2. El documento vale hasta terminar su fecha en Perú. Si vence sin renovación aprobada, la siguiente revisión suspende el local y avisa al propietario.
3. El local deja de aparecer en búsqueda y no admite nuevas ventas ni publicaciones. La cuenta operativa conserva la entrega de compras pagadas.
4. El propietario adjunta documentos renovables en una nueva solicitud. En un local suspendido debe cubrir los tipos vencidos faltantes; también se admite renovar documentos próximos a vencer.
5. El administrador resuelve. Al aprobar, los nuevos documentos sustituyen a los anteriores del mismo tipo para calcular vigencia, conservando el historial. Si no quedan vencidos, se cierra la suspensión y se reactiva el local.

**D4 pendiente de implementación:** el consumidor debe ver una indicación sencilla y real de verificación, sin documentos privados. No se debe presentar la aprobación inicial como garantía indefinida de vigencia; la forma exacta de combinar ambos estados necesita precisarse antes de implementarla.
