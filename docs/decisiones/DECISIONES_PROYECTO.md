# Decisiones del proyecto

Este documento registra las decisiones aprobadas durante el desarrollo del proyecto y la justificación necesaria para comprenderlas y sustentarlas.

## Aclaraciones académicas confirmadas

- El segundo avance se entregará en la semana 10.
- La indicación de semana 14 presente en el PDF corresponde a un error confirmado.
- El PDF contiene los requisitos académicos generales.
- La plantilla DOCX entregada posteriormente será la base estructural y visual del informe.

## DEC-001 — Pagos simulados en V1
El sistema modelará pagos y estados, pero no moverá dinero real.

## DEC-002 — Ubicación textual en V1
Filtrado por datos como distrito/dirección. No GPS ni mapas inicialmente.

## DEC-003 — Web responsive mobile-first
Aplicación web optimizada para celular en lugar de app nativa Android/iOS.

## DEC-004 — Reserva y pago como operación lógica única
Para reserva remota, al confirmarse el pago se crea la reserva y se actualiza el stock de forma consistente.

## DEC-005 — Múltiples unidades
Un consumidor puede adquirir más de una unidad de una oferta si existe stock suficiente.

## DEC-006 — Cancelación/reembolso solo para reservas remotas
Una reserva remota no entregada puede cancelarse. Penalización: 50% de lo abonado. Reembolso simulado: 50%. Las compras presenciales por QR no tienen cancelación/reembolso mediante la plataforma.

## DEC-007 — Oferta con reservas confirmadas no cancelable
El comercio no puede cancelar una oferta después de que tenga reservas confirmadas.

## DEC-008 — Horarios definidos por el establecimiento
Cada oferta tiene ventana de recojo y límite definitivo configurados por el comercio según el producto.

## DEC-009 — Gestión de no recojo
Si termina la ventana normal, puede notificarse al consumidor y existir margen adicional. Tras el límite definitivo, la reserva pasa a no recogida/expirada y no puede entregarse.

## DEC-010 — Reseñas solo tras recojo completado
No se puede reseñar una experiencia que no haya terminado en entrega.

## DEC-011 — Separar PackSorpresa de OfertaPack
`PackSorpresa` es una definición reutilizable; `OfertaPack` es la publicación concreta para una fecha, precio, stock y horario determinados.

## DEC-012 — Valores cerrados mediante enums
Roles, estados y métodos de pago cerrados se modelarán como enums cuando corresponda, en vez de crear entidades artificiales solo para alcanzar el mínimo de clases.

## DEC-013 — Acceso presencial mediante QR
Un establecimiento puede mostrar QR que lleva directamente a una oferta activa. Sirve para quien ya está físicamente allí y también para dar visibilidad a la plataforma.

## DEC-014 — Medios de pago según contexto
- Remoto: solo pago digital simulado.
- QR presencial: pago digital simulado o efectivo.
  El efectivo existe únicamente cuando el consumidor ya está en el establecimiento.

## DEC-015 — Afiliación de comercios sujeta a aprobación
Un usuario no puede publicar packs inmediatamente; debe solicitar afiliación y ser aprobado por un administrador de plataforma.

## DEC-016 — Verificación documental, no certificación propia
La plataforma revisa documentación aportada, pero no se presenta como autoridad sanitaria ni emite certificaciones sanitarias propias.

## DEC-017 — Documentos obligatorios dentro del registro/solicitud
La documentación requerida se adjunta como parte del mismo proceso de solicitud de afiliación. No se completa “después de registrarse” para poder enviar la solicitud.

## DEC-018 — Transparencia sin exposición de documentos privados
El consumidor puede ver estados de verificación/vigencia del establecimiento, pero no los archivos documentales originales.

## DEC-019 — Vigencia individual por documento
No se asume una frecuencia universal de renovación. Cada documento puede tener fecha de emisión/vencimiento y su propia vigencia.

## DEC-020 — Revisión documental manual en V1
Un administrador humano abre el documento y registra manualmente datos relevantes como tipo, número, fecha de emisión, vencimiento y observaciones. No OCR/análisis automático en V1.

## DEC-021 — Registro público para consumidores y aspirantes a comercio
Las cuentas internas administrativas no tienen registro público.

## DEC-022 — Administradores provisionados internamente
Las cuentas de personal de plataforma se crean previamente por personal autorizado y solo utilizan login.

## DEC-023 — Google Sign-In fuera del alcance funcional de V1
Puede mostrarse como función futura/“Próximamente”, pero no debe aparentar funcionar si no está implementada.

## DEC-024 — Funciones futuras visibles como “Próximamente”
Toda función futura presente en el prototipo debe identificarse claramente como no implementada.

## DEC-025 — Arquitectura de monolito modular por capas

**Estado:** Aprobada.

### Decisión

La primera versión de la plataforma se desarrollará mediante una arquitectura de monolito modular por capas.

El backend será una única aplicación Java con Spring Boot y utilizará una única base de datos relacional. Internamente, el código se organizará en módulos correspondientes a las principales áreas del negocio. Dentro de cada módulo se separarán las responsabilidades mediante las capas `controller`, `service`, `repository`, `domain` y `dto`.

El frontend se comunicará con el backend mediante HTTP y servicios REST. Los controllers recibirán las solicitudes y delegarán las operaciones; los services implementarán las reglas del negocio y los límites transaccionales; los repositories gestionarán la persistencia mediante JPA; y las clases de dominio representarán los conceptos, relaciones y estados del negocio.

Las funciones transversales de seguridad, validación, configuración y manejo de excepciones se mantendrán separadas de la lógica funcional.

### Justificación

Esta arquitectura satisface la exigencia académica de aplicar una arquitectura de software, separación por capas, persistencia con JPA, base de datos, formularios y servicios REST.

Es adecuada para el alcance de la primera versión porque facilita las pruebas, mantiene alineados los diagramas y el código, permite repartir el trabajo entre cinco integrantes y puede ser comprendida y sustentada por todo el equipo.

La aplicación no se dividirá en microservicios durante la V1. Una separación física futura solo se evaluará si aparece una necesidad concreta que la justifique.

### Consecuencias

- Existirá un único backend desplegable.
- La base de datos será compartida por todos los módulos.
- La lógica del negocio se concentrará en los services y el dominio.
- El frontend no accederá directamente a la base de datos.
- Los controllers no contendrán reglas importantes del negocio.
- Los repositories no tomarán decisiones del negocio.
- Las operaciones que requieran consistencia, como la creación de una reserva y la actualización del stock, se ejecutarán mediante transacciones.
- La organización modular deberá corresponder con los requerimientos, casos de uso y clases definitivas.
- Las versiones y tecnologías concretas se confirmarán según lo utilizado en clase.


## DEC-026 — Una identidad con perfiles combinables

Una persona utilizará una sola cuenta y podrá tener perfil de consumidor, perfil empresarial o ambos. El cambio de perfil no requiere crear credenciales duplicadas.

## DEC-027 — Empresa con varios establecimientos

Una empresa puede registrar varios establecimientos físicos. Cada establecimiento tiene una ubicación, documentos, aprobación y operación independientes.

## DEC-028 — Cuenta operativa por establecimiento

Después de la aprobación, el propietario crea una cuenta compartida para el personal de cada establecimiento. Esta cuenta solo puede gestionar el local asignado. No se registrarán empleados individualmente en la V1.

## DEC-029 — Nueva solicitud después de un rechazo

Si una solicitud presenta problemas, se rechaza y se informa el motivo. La cuenta, empresa y establecimiento se conservan; el propietario deberá presentar una nueva solicitud para ese local.

## DEC-030 — Suspensión por documento vencido

Un establecimiento aprobado se suspende si vence un documento obligatorio. No podrá publicar nuevas ofertas hasta que el documento renovado sea revisado y aprobado. Su cuenta e historial se conservan.

## DEC-031 — Una oferta activa con variantes

Cada establecimiento tendrá como máximo una oferta activa simultáneamente. La oferta puede contener varios tamaños con precios y stock propios.

## DEC-032 — Información previa del pack

Antes de comprar se mostrarán la descripción, posibles productos, tamaño, precio, horario, alérgenos y restricciones alimentarias. El contenido exacto seguirá siendo sorpresa.

## DEC-033 — Boleta simulada y código de recojo

Al confirmarse el pago se generará una boleta sin validez tributaria y un código único. El personal buscará ese código y lo invalidará al marcar la entrega.

## DEC-034 — Retención para efectivo presencial

Cuando se elija efectivo desde el QR del local, el stock se retendrá temporalmente. El personal confirmará manualmente el dinero recibido y el recojo quedará completado en ese momento.

## DEC-035 — Límites de cancelación remota

Una reserva remota puede cancelarse mientras esté pendiente de recojo y no haya superado el límite definitivo. Se registra un reembolso simulado del 50 % y el stock se recupera si todavía puede venderse.

## DEC-036 — Comisión del 10 % sobre lo cobrado

La comisión de la plataforma será el 10 % del importe que efectivamente permanezca cobrado. Si una reserva se cancela, la comisión se calcula sobre la mitad retenida, no sobre el precio original.

## DEC-037 — Panel basado en información calculada

Ventas, comisiones, ingresos, cancelaciones, no recojos, calificaciones e impacto se calcularán desde los registros existentes. No se crearán entidades artificiales para representar el dashboard.

## DEC-038 — Cálculo ambiental postergado

El impacto ambiental se registrará conceptualmente después de un recojo completado. La fórmula exacta de alimento recuperado y CO₂ evitado se definirá en una etapa posterior.

## DEC-039 — Stack tecnológico principal

- Java 21.
- Spring Boot.
- Maven.
- Spring Data JPA.
- MySQL.
- HTML, CSS y JavaScript.
- Tailwind CSS.

La versión exacta de Spring Boot se registrará al generar el proyecto base.

## Documentos relacionados

- [Visión y alcance](../PROYECTO.md)
- [Requisitos](../requisitos/REQUISITOS.md)
- [Flujos de negocio](../FLUJOS_NEGOCIO.md)
- [Modelo de dominio](MODELO_DOMINIO.md)
- [Arquitectura](../ARQUITECTURA.md)
