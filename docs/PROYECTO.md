# Sazloop: guía interna y estado

Sazloop es el proyecto académico N.º 91, «SalvemosLaComida», del curso Desarrollo Web Integrado. Conecta establecimientos con consumidores de Lima Metropolitana para vender excedentes aptos para consumo en packs sorpresa con recojo presencial y pagos simulados.

El objetivo es reducir desperdicio de alimentos mediante una plataforma que permita gestionar personas, empresas y locales, revisar su afiliación y completar compra, pago simulado, cancelación y entrega con control de stock. El trabajo comprende diseño de arquitectura, modelo y pantallas, implementación y validación.

## Dónde consultar cada tema

| Necesidad | Documento principal |
|---|---|
| Instalar y arrancar | [README](../README.md) |
| Saber qué debe cumplir el sistema | [Requisitos](requisitos/REQUISITOS.md) |
| Conocer acuerdos y sus cambios | [Decisiones](decisiones/DECISIONES_PROYECTO.md) |
| Ubicar código, API, entornos y dependencias | [Arquitectura](ARQUITECTURA.md) |
| Entender entidades, relaciones y restricciones | [Modelo de dominio](decisiones/MODELO_DOMINIO.md) |
| Seguir una operación de principio a fin | [Flujos](FLUJOS_NEGOCIO.md) |
| Aclarar vocabulario | [Glosario](GLOSARIO.md) |
| Revisar evidencia y problemas históricos | [Auditorías](../auditoria/) |
| Trabajar como agente de IA | [AGENTS](../AGENTS.md) |

## Alcance y actores

Una persona puede comprar y administrar su empresa desde la misma identidad. El propietario registra locales y presenta sus documentos; cada local se aprueba por separado y utiliza una cuenta operativa compartida para publicar y entregar. El administrador de plataforma revisa las solicitudes. Los resultados empresariales se consultan por local y de forma consolidada.

V1 no incluye delivery, pagos reales, GPS/mapas, OCR, certificación sanitaria propia, aplicación móvil nativa, Google Sign-In, registro individual de empleados, varios tipos de oferta activos a la vez por local ni microservicios. La fórmula científica del impacto sigue postergada. La portada contiene menciones de delivery que no corresponden a una integración implementada; se registran como problema de la interfaz, sin ampliar el alcance.

## Estado por requisito

Referencia de código: `73d92dde8e4d6c7dcc3f3f5460c92d72ab030447`, sin cambios respecto del diagnóstico inicial. La auditoría de esta sincronización ejecutó **12 pruebas existentes, todas aprobadas**, y **15 escenarios HTTP locales**, todos con el resultado esperado. Se utilizó H2 aislada; no se comprobó MySQL ni el uso completo de las pantallas en navegador.

«Existe» significa que hay implementación identificada, no cumplimiento total del requisito. T1–T7 identifican las clases de prueba de la sección siguiente; H01–H15 son los escenarios descritos en la [auditoría 001](../auditoria/AUDITORIA_001_2026-10-27_20-18.md). Una comprobación acredita solo el caso y entorno indicados.

| RF | Código y situación actual | Comprobación y límite relevante |
|---|---|---|
| RF-01 | Registro mediante `RegistroUsuarioService`. Existe. | H02: registro e ingreso HTTP. |
| RF-02 | Sesiones de persona, local y administrador. Existen. | T2 y H03: coexistencia y cierre de administrador sin cerrar otros roles. |
| RF-03 | Perfiles asociados a Usuario; Empresa crea propietario y Compra crea consumidor si falta. Existe. | Lectura de código; no se ejecutó un recorrido completo de alternancia dual en pantalla. |
| RF-04 | `EmpresaService.registrarEmpresa`. Existe. | H11: alta empresarial. |
| RF-05 | Locales con dirección y pertenencia a Empresa. Existe. | H11/H13: varios locales de una empresa. |
| RF-06 | Adjuntos privados y controles de archivo, fechas y solicitud. Parcial respecto del requisito. | H11: envío vacío rechazado y envío con OTRO aceptado. D3 deja pendiente la lista exacta exigible. |
| RF-07 | Revisión humana conjunta y operaciones anteriores. Existen. | T1/T2 y H12/H13. Las rutas anteriores permiten editar datos. |
| RF-08 | Evaluación y estado de solicitud. Existe. | T1/T2: aprobación y rechazo; H12/H13: aprobación por ambos recorridos. |
| RF-09 | Vigencia, suspensión y renovación en `VigenciaDocumentosService`. Existe. | T6: llamadas directas con fechas controladas; no se esperó el ciclo real del programador. |
| RF-10 | Cuenta operativa de local aprobado. Existe. | T1 y H12. |
| RF-11 | Pack nuevo o reutilizable con contenido posible. Existe. | T1 y H12. |
| RF-12 | Una oferta activa por local con variantes. Existe. | T1 rechaza publicación simultánea vigente; H12/H14 publican variantes. No hay cierre anticipado. |
| RF-13 | Inicio y fin; entrega desde el pago y fin como límite. Existe. | H12 entrega antes del inicio; T1 comprueba vencimiento mediante llamada directa. |
| RF-14 | Filtros de distrito, dirección, precio y calificación. Existen. | T1 y H04 comprueban casos concretos de búsqueda; no toda combinación. |
| RF-15 | Oferta con información del pack, precios y advertencias. Existe; D4 pendiente. | T1/H04; falta indicador documental sencillo al consumidor. |
| RF-16 | Compra remota digital simulada, individual y por lote. Existe. | T1 y H08/H09/H14. Simular pagos rechazados sigue pendiente de decisión. |
| RF-17 | QR por oferta, conforme a D1. Existe. | H05 verifica token y compra; generación del QR leída en JavaScript, sin escaneo físico. |
| RF-18 | Compra presencial digital o en efectivo con token. Existe. | T1 y H05/H10. |
| RF-19 | Retención de efectivo hasta diez minutos o fin del horario. Existe. | H10 comprueba estado pendiente/cobro; T1 fuerza vencimiento y liberación. |
| RF-20 | Boleta simulada y código por reserva. Existen. | H05/H10. En efectivo el código existe antes del cobro; la boleta después. |
| RF-21 | Consulta y confirmación de código por local. Existe. | H06: no reutilización ni acceso desde otro local; H12: entrega anticipada permitida. |
| RF-22 | Cancelación remota y devolución simulada del 50 %. Existe. | T1/T6 y H08. No se permite cancelar compras presenciales. |
| RF-23 | Reseña tras entrega. Adaptación D2 pendiente. | H07 confirma que una segunda compra del mismo local no puede reseñarse. Obligatoriedad/omisión sin resolver. |
| RF-24 | Indicadores por local/empresa y panel operativo. Existen. | T1/T2 y H15 comprueban importes y unidades; no todos los bordes de períodos. |
| RF-25 | Buzón personal y lectura de avisos. Existe. | T6 y H15: eventos y lectura; no se acreditan correo, SMS ni notificaciones del dispositivo. |
| RF-26 | Registro conceptual por entrega y contador de packs. Existe. | T6 comprueba registro; H15 comprueba unidades entregadas. No hay cifras de kilos ni CO₂. |

## Verificación y pruebas

| ID | Clase en `backend/src/test/java/com/sazloop` | Pruebas | Qué comprueba el código de prueba |
|---|---|---:|---|
| T1 | [FlujoAfp2Tests](../backend/src/test/java/com/sazloop/FlujoAfp2Tests.java) | 3 | Perfil, afiliación, publicación, lotes y rollback, stock, entrega, reseña, cancelación, efectivo, vencimiento e indicadores. |
| T2 | [ResolucionYSesionesTests](../backend/src/test/java/com/sazloop/ResolucionYSesionesTests.java) | 3 | Resolución, ganancia y sesiones de persona/administrador; el nombre menciona local, pero ese test no lo inicia. |
| T3 | [PortalEmpresaEtapasTests](../backend/src/test/java/com/sazloop/PortalEmpresaEtapasTests.java) | 1 | Consultas empresariales durante distintas etapas de afiliación. |
| T4 | [DatosDemoH2Tests](../backend/src/test/java/com/sazloop/DatosDemoH2Tests.java) | 1 | Datos, accesos, documentos y ofertas ficticios; activa `dev,demo`. |
| T5 | [SolicitudAfiliacionServiceTests](../backend/src/test/java/com/sazloop/afiliacion/SolicitudAfiliacionServiceTests.java) | 1 | Adjunto, envío y descarga restringida al propietario. |
| T6 | [VigenciaYAvisosTests](../backend/src/test/java/com/sazloop/VigenciaYAvisosTests.java) | 2 | Suspensión, conservación de entrega pagada, renovación, impacto y avisos. |
| T7 | [SazloopApplicationTests](../backend/src/test/java/com/sazloop/SazloopApplicationTests.java) | 1 | Carga del contexto; no acredita todos los casos de uso. |

Resultado de esta revisión: las siete clases terminaron con **12 ejecutadas, 0 fallos, 0 errores y 0 omitidas**. H14 añadió una comprobación HTTP concurrente con dos compradores para una unidad: una compra aceptada, otra rechazada y stock final cero en H2. No demuestra todos los casos de concurrencia ni su comportamiento en MySQL.

### Repetir las pruebas sin usar MySQL

Requiere JDK 21 completo y acceso a las dependencias de Maven. Ejecutar desde `backend`, preferentemente en una copia temporal, porque las pruebas generan archivos y registros ficticios. Varias clases no seleccionan perfil propio: no ejecutar simplemente `test` con la configuración MySQL por defecto.

Windows CMD:

```bat
mvnw.cmd -Dspring.profiles.active=dev "-Dspring.datasource.url=jdbc:h2:mem:sazloop_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE" -Duser.timezone=America/Lima test
```

Linux/macOS:

```sh
sh mvnw -Dspring.profiles.active=dev '-Dspring.datasource.url=jdbc:h2:mem:sazloop_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE' -Duser.timezone=America/Lima test
```

Los directorios de adjuntos definidos por las pruebas quedan bajo `target`. No usar MySQL persistente ni datos reales sin autorización específica. En esta auditoría se utilizó Maven externo y opciones temporales para suplir la falta de `ct.sym` en el Java del entorno; el comando exacto y sus límites están en el informe 001. No se cambió el POM.

No se acreditaron: recorridos visuales y accesibilidad en navegador, escaneo físico del QR, MySQL, tiempos de respuesta de 2–3 segundos bajo condiciones definidas, límites entre días/semanas/meses y zonas distintas, ni seguridad integral. Los vencimientos se comprobaron mediante las pruebas que llaman directamente a los servicios, no esperando diez minutos o un cambio de día real.

## Decisiones nuevas y trabajo pendiente

La fuente normativa de D1–D6 es DEC-051 a DEC-056. Estado de esta revisión:

| Decisión | Estado real |
|---|---|
| D1: QR por oferta | Implementado; token comprobado por HTTP. Imagen y escaneo físico no probados. |
| D2: reseña por compra recogida | Aprobada, pendiente de adaptar entidad y servicios. La limitación actual por local se reprodujo. |
| D3: documentos exactos | Definición pendiente del equipo. Solo se describen los controles actuales. |
| D4: verificación visible | Aprobada, sin indicador implementado en catálogo. Falta precisar presentación de aprobación/vigencia. |
| D5: conservar operaciones anteriores | Se conservan y funcionan en la comprobación HTTP; eliminación pendiente de evaluar compatibilidad. |
| D6: sin Tailwind | Documentación y requisito actualizados; código con CSS propio, sin integración Tailwind identificada. |

Continúan abiertos la obligatoriedad/omisión de reseñas, el cierre anticipado de ofertas y la simulación de pagos rechazados. También quedan por tratar las credenciales literales, las menciones de delivery y los términos provisionales de la interfaz, y la consistencia de zonas horarias. Los hallazgos numerados y sus prioridades están en la auditoría; actualizar documentación no corrige esos problemas de código o interfaz.

## Contexto académico y equipo

Se conservan los hitos documentados: primer avance, semana 5, martes a las 18:00; segundo, semana 10, martes a las 18:00; entrega final, semana 18, lunes a las 18:00. La referencia a semana 14 se registró como error confirmado. No se convierten esas semanas en fechas nuevas ni se revalidan las fuentes excluidas.

Equipo: Emanuel, Daniel, Franco, Piero y Alejandro. La asignación de módulos y revisión de componentes compartidos se conserva en Arquitectura.
