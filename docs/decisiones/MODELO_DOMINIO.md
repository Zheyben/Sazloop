# Modelo de dominio de Sazloop

Este documento explica las entidades implementadas y las restricciones que afectan su mantenimiento. Las reglas aprobadas están en [Decisiones](DECISIONES_PROYECTO.md), los recorridos en [Flujos](../FLUJOS_NEGOCIO.md) y las comprobaciones en [Proyecto](../PROYECTO.md).

## Entidades y relaciones

Las clases se encuentran bajo [com.sazloop](../../backend/src/main/java/com/sazloop/), dentro de `domain` de cada módulo. Son 31 entidades JPA: Identidad 7, Afiliación 6, Ofertas 5, Reservas 5, Transacciones 5 y Experiencia 3. Se conserva la justificación académica: superan el mínimo documentado de 25 para cinco integrantes sin contar controladores, servicios, DTO ni estados como entidades adicionales.

| Módulo | Entidad | Responsabilidad y relación principal |
|---|---|---|
| Identidad | `Usuario` | Identidad personal y credenciales; puede tener perfiles Consumidor y PropietarioEmpresa por asociación, sin herencia. |
| Identidad | `Consumidor` | Perfil comprador; como máximo uno por Usuario. |
| Identidad | `PropietarioEmpresa` | Perfil empresarial; como máximo uno por Usuario, asociado a una Empresa. |
| Identidad | `Empresa` | Organización con RUC único; una por propietario en el modelo y servicio actuales; agrupa varios establecimientos. |
| Identidad | `Establecimiento` | Local físico de una Empresa; aprobación y operación independientes. |
| Identidad | `Direccion` | Ubicación estructurada; relación uno a uno y ciclo de vida ligado al establecimiento. |
| Identidad | `CuentaEstablecimiento` | Acceso compartido por trabajadores; una cuenta por local y nombre de usuario único. No existe una entidad Empleado. |
| Afiliación | `SolicitudAfiliacion` | Un intento de afiliación o renovación de un local; reúne documentos y una posible evaluación final. |
| Afiliación | `DocumentoEstablecimiento` | Archivo privado, tipo, datos y fechas; pertenece a una solicitud, no directamente a Empresa. |
| Afiliación | `RevisionDocumento` | Resultado y observaciones del administrador para un documento. La relación admite varias revisiones; el servicio actual impide revisar otra vez el mismo documento. |
| Afiliación | `EvaluacionAfiliacion` | Decisión general; relación uno a uno con solicitud y FK única. |
| Afiliación | `AdministradorPlataforma` | Cuenta interna independiente de Usuario; responsable de revisiones, evaluaciones y suspensiones. |
| Afiliación | `SuspensionEstablecimiento` | Motivo, inicio, fin y estado activo de una suspensión; conserva el historial del local. |
| Ofertas | `PackSorpresa` | Descripción reutilizable de un local; posibles productos, alérgenos y restricciones. |
| Ofertas | `OfertaPack` | Publicación concreta de un pack con fecha, horario, variantes y token QR propio. Varias ofertas históricas pueden usar el mismo pack. |
| Ofertas | `VariantePack` | Tamaño de una oferta, precios y stock inicial/disponible. |
| Ofertas | `Alergeno` | Catálogo por nombre único; asociación muchos a muchos con packs. |
| Ofertas | `RestriccionAlimentaria` | Característica alimentaria por nombre único; asociación muchos a muchos con packs. |
| Reservas | `RetencionStock` | Unidades temporalmente apartadas de una variante para un consumidor; vinculadas a reserva y vencimiento en el flujo de efectivo. |
| Reservas | `Reserva` | Compra pendiente de pago, confirmada o terminada; pertenece a un consumidor y una oferta. |
| Reservas | `DetalleReserva` | Variante, unidades, precio y subtotal guardados al comprar; varios por reserva. |
| Reservas | `CodigoRecojo` | Identificador único de reserva, con estado y fecha de uso; uno por reserva. |
| Reservas | `Recojo` | Entrega completada de una reserva, confirmada por una cuenta del local; relación uno a uno. |
| Transacciones | `Pago` | Medio, contexto, importe y estado; uno por reserva. V1 no procesa dinero real. |
| Transacciones | `BoletaSimulada` | Comprobante interno sin validez tributaria; uno por reserva, número único. |
| Transacciones | `CancelacionReserva` | Cancelación de una reserva, penalización, reembolso y devolución de stock; uno por reserva. |
| Transacciones | `Reembolso` | Devolución simulada; uno por cancelación mediante `cancelacion_id`. |
| Transacciones | `ComisionPlataforma` | Base retenida, comisión e ingreso del local; una por pago. |
| Experiencia | `Resena` | Nota y comentario de una compra recogida; una por reserva. Además existe una unicidad consumidor/local que contradice D2 y sigue pendiente de adaptación. |
| Experiencia | `Notificacion` | Aviso persistente de una persona con motivo, referencia y estado de lectura; no es solamente un mensaje momentáneo de pantalla. |
| Experiencia | `ImpactoCO2Evitado` | Registro uno a uno por recojo; cifras ambientales nulas y estimación no disponible hasta aprobar una fórmula. |

Las asociaciones `pack_alergenos` y `pack_restricciones` añaden dos tablas a las 31 tablas de entidades. El conteo describe el mapeo del código; no se inspeccionaron los diagramas excluidos ni se certifica aquí su coincidencia.

## Estados que afectan los recorridos

Los valores cerrados se representan mediante enums anidados en las entidades; no se crean clases artificiales para completar el conteo académico.

| Entidad | Estados definidos |
|---|---|
| Establecimiento | `PENDIENTE_APROBACION`, `ACTIVO`, `SUSPENDIDO`, `INACTIVO` |
| SolicitudAfiliacion | `PENDIENTE`, `EN_REVISION`, `APROBADA`, `RECHAZADA` |
| OfertaPack | `BORRADOR`, `ACTIVA`, `AGOTADA`, `FINALIZADA`, `CANCELADA` |
| Reserva | `PENDIENTE_PAGO`, `CONFIRMADA`, `RECOGIDA`, `CANCELADA`, `EXPIRADA` |
| Pago | `PENDIENTE`, `CONFIRMADO`, `RECHAZADO` |
| CodigoRecojo | `PENDIENTE`, `UTILIZADO`, `VENCIDO` |
| RetencionStock | `ACTIVA`, `CONFIRMADA`, `LIBERADA`, `VENCIDA` |

Que exista un estado no demuestra que haya una operación pública para alcanzarlo. Por ejemplo, no hay cierre anticipado de ofertas ni simulación de rechazo digital identificados; ambas decisiones siguen pendientes.

## Restricciones y diferencias relevantes

- **Identidad:** la creación de Empresa añade el perfil propietario; la compra crea el perfil consumidor si falta. Las credenciales siguen siendo las de Usuario. Las cuentas de local y administrador son independientes.
- **Solicitud:** el servicio trabaja con el último intento y bloquea adjuntos después de enviarlo. Un rechazo conserva el local y permite un nuevo intento. La lista exacta de documentos exigibles continúa pendiente (D3).
- **Revisión y renovación:** la pantalla usa resolución conjunta; las operaciones anteriores siguen expuestas por D5. Cada renovación usa una nueva solicitud y nuevos documentos. La vigencia selecciona los documentos de la última solicitud aprobada por tipo; no borra el historial. Solo se reactiva un local suspendido cuando no quedan documentos vencidos.
- **Suspensión:** la automática se atribuye a una cuenta interna desactivada del sistema, para conservar la relación obligatoria con AdministradorPlataforma. El local y sus reservas pagadas se conservan.
- **Oferta:** el servicio bloquea el local al publicar y comprueba que no exista otra oferta activa sin vencer. No hay una restricción única de base de datos que por sí sola imponga esa regla. Los tamaños son pequeño, mediano y grande; se rechazan tamaños repetidos, stock inicial inválido y descuentos no menores al precio referencial.
- **Horario:** `limiteDefinitivo` se conserva por compatibilidad y se establece igual a `finRecojo`. El inicio no bloquea la entrega de una compra pagada; el fin es el límite. No debe reintroducirse un margen posterior al fin.
- **QR:** `OfertaPack.tokenQr` identifica una oferta (D1); no existe una entidad QR ni un enlace permanente de local en el recorrido actual.
- **Reserva:** una compra remota guarda pago confirmado, detalles, boleta, código y comisión conjuntamente. En efectivo existe primero una reserva pendiente con retenciones; el código se genera antes del cobro y solo se utiliza al confirmar pago y entrega. Boleta y recojo no existen todavía durante esa espera.
- **Stock:** los servicios usan transacciones y bloqueos para las operaciones previstas. Los detalles conservan el precio aplicado; no deben recalcularse usando un precio de publicación posterior.
- **Finalización:** el servicio no permite que una misma reserva termine recogida y cancelada. Esa exclusión se controla por estados y servicios, no por una única restricción relacional entre las dos tablas.
- **Reseñas:** `reserva_id` es único, pero también existe `uk_resena_consumidor_local`. D2 exige retirar la limitación por local en un trabajo funcional posterior, coordinando entidad, servicio y disponibilidad presentada al consumidor. `resenaOmitida` impide reseñar esa compra; obligatoriedad y omisión siguen pendientes de acuerdo.

## Información calculada

Los paneles calculan ventas, comisiones, ingresos, cancelaciones, no recojos y reseñas a partir de registros existentes. No se necesitan entidades Dashboard, EstadisticaEmpresa o ReporteVentas. El impacto visible suma unidades de compras recogidas, no la cantidad de registros de impacto ni kilos o CO₂.

El cálculo de comisión y reembolso se conserva como regla en DEC-036. Las diferencias pendientes respecto de las nuevas decisiones se siguen en [Proyecto](../PROYECTO.md) y en [Auditorías](../../auditoria/).
