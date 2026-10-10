# Estado actual del proyecto

**Actualización:** 9 de octubre de 2026.  
**Proyecto:** N.º 91 — SalvemosLaComida.  
**Plataforma:** Sazloop.  
**Etapa:** avance 2 (entrega: martes 13 de octubre de 2026).

## Resumen

Los 26 requisitos funcionales tienen backend y pantallas conectados. El sitio público, el portal del consumidor, las pantallas de acceso y los portales de trabajador, empresa y administración usan la misma API REST. Las reglas se precisan en el anexo «Definiciones funcionales aprobadas» del [informe del avance 2](../informe/Avance_del_proyecto_02.docx) y en [Decisiones](../decisiones/DECISIONES_PROYECTO.md).

## Estado por requisito

| Módulo | RF | Estado | Dónde se ve |
|---|---|---|---|
| Identidad | 01, 02, 03 | Implementado | Acceso de consumidor y de locales; una misma cuenta compra y administra su empresa. |
| Identidad | 04, 05 | Implementado | Portal de empresa: registro de empresa y de locales. |
| Identidad | 10 | Implementado | Portal de empresa: cuenta del trabajador de cada local aprobado. |
| Afiliación | 06, 07, 08 | Implementado | Empresa adjunta y envía; administración revisa cada documento (con datos transcritos) y decide con motivo. |
| Afiliación | 09 | Implementado | Aviso 7 días antes, suspensión automática, renovación solo de documentos vencidos y reactivación al aprobarla. |
| Ofertas | 11, 12, 13 | Implementado | Portal del trabajador: formulario de tres pasos; una oferta activa; el fin del horario es el único límite. |
| Ofertas | 14, 15, 17 | Implementado | Portal del consumidor y QR de cada oferta. |
| Reservas | 19, 21, 22 | Implementado | Retención de efectivo (10 min), validación de código y cancelación con reembolso del 50 %. |
| Transacciones | 16, 18, 20 | Implementado | Pago conjunto de varios pedidos (Yape o tarjeta simulados), efectivo por QR, boleta y código. |
| Experiencia | 23 | Implementado | Aviso de reseña tras el recojo; calificación obligatoria y comentario opcional. |
| Experiencia | 24 | Implementado | Portal de empresa: ventas, comisiones, ingresos, cancelaciones, no recogidas, reseñas e impacto por local y total. |
| Experiencia | 25 | Implementado | Buzón del propietario y del consumidor (campana); recordatorio de recojo al confirmar la compra. |
| Experiencia | 26 | Implementado | Cada recojo registra su impacto conceptual; se muestra como número de packs entregados. |

## Cómo se comprobó

- **Pruebas automáticas** (`mvnw.cmd test`): 11 pruebas en 6 clases, todas en verde. `FlujoAfp2Tests` recorre afiliación, publicación, compra, cancelación, efectivo, vencimiento e indicadores; `VigenciaYAvisosTests` comprueba suspensión, renovación, avisos e impacto; `ResolucionYSesionesTests` comprueba la resolución en un paso, la entrega desde el pago con la comisión del 10 % y las sesiones simultáneas; `PortalEmpresaEtapasTests` comprueba que las consultas del portal de empresa respondan en cada etapa de la afiliación.
- **Recorrido completo por la API** con la aplicación en ejecución (perfil `dev`, 9 de octubre de 2026): registro, afiliación, revisión, cuenta del trabajador, publicación, compra de dos pedidos, validación del código, entrega, cancelación, indicadores, notificaciones y suspensión real provocada al vencer un documento desde la consola de H2, seguida de su renovación.

## Pendiente o limitado

- **Las pantallas no se han visto en un navegador real.** Se comprobaron por API, con un simulador del DOM que ejecuta cada script y con revisión de código (así se detectó que `trabajador.js` usaba constantes antes de definirlas). Falta revisarlas en celular y escritorio.
- Historial de packs entregados del trabajador, con su boleta: **no implementado** (pendiente de definir su alcance con el equipo).
- Un error inesperado reportado en el portal de empresa no se pudo reproducir. Las respuestas del servidor ya muestran su código y la ruta, y el navegador lo registra en la consola.
- Persistencia en MySQL: el perfil `mysql` existe, pero debe probarse en el equipo donde se hará la demostración. La base empieza vacía si no se restaura un respaldo.
- El sitio público menciona delivery con aplicaciones externas, aunque el informe lo deja fuera de la primera versión.
- Las pantallas usan CSS propio y no Tailwind, aunque el informe menciona Tailwind.

Los planes [SEMANA_04.md](SEMANA_04.md) y [SEMANA_05.md](SEMANA_05.md) conservan la planificación histórica. Sus casillas no representan el estado actual.
