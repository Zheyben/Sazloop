# Instrucciones para trabajar en Sazloop

## Antes de cambiar archivos

- Leer [Proyecto](docs/PROYECTO.md) para ubicar el estado, la verificación y los pendientes; consultar [Arquitectura](docs/ARQUITECTURA.md) para localizar código y contratos.
- Contrastar requisitos y decisiones con el código actual. Una decisión aprobada puede seguir pendiente; una clase existente no demuestra que el recorrido funcione.
- Respetar el alcance autorizado. En una tarea documental no modificar Java, JavaScript, HTML, CSS, configuración, dependencias ni pruebas.
- No modificar `README.md` sin autorización explícita y específica. No inspeccionar ni modificar `docs/informe/` o `docs/diagramas/` sin una ampliación expresa del alcance.
- No realizar commits, pushes ni operaciones remotas de escritura sin autorización. Conservar los cambios ajenos que ya existan.

## Implementación y documentación

- Respetar módulos, responsables y reglas de dependencia de Arquitectura; revisar ambos lados antes de modificar un contrato compartido.
- Preservar IDs de RF y DEC. Registrar un cambio aprobado sin borrar su antecedente ni ajustar requisitos solo para declarar cumplimiento del código.
- Consultar DEC-051 a DEC-056: QR por oferta; reseña por compra pendiente de adaptación; lista documental pendiente; verificación visible pendiente; operaciones administrativas anteriores conservadas; CSS propio sin Tailwind.
- No resolver por cuenta propia obligatoriedad/omisión de reseñas, cierre anticipado, pagos rechazados o lista documental. Registrar la definición pendiente.
- Mantener una ubicación principal por tema y enlazarla. No copiar tablas completas del código ni crear manuales sin necesidad.

## Verificación segura

- Leer las propiedades y las anotaciones de las pruebas antes de ejecutarlas. El perfil por defecto es MySQL: seleccionar explícitamente `dev` y una H2 temporal aislada.
- Seguir los comandos y límites de [Verificación en Proyecto](docs/PROYECTO.md). Usar una copia temporal y directorios temporales para adjuntos; no utilizar datos persistentes/reales sin autorización específica.
- Preferir argumentos o configuración externa para preparar el entorno; no alterar el POM ni las pruebas para hacerlas pasar sin permiso.
- Informar comando, entorno, resultado y límites. No confundir un bloqueo del entorno con un defecto de la aplicación ni una prueba H2 con validación de MySQL.
- No incluir contraseñas, tokens, cookies ni otros secretos en documentación o informes.

## Auditorías solicitadas

- Revisar informes previos relevantes en [auditoria](auditoria/) y comprobar sus pendientes contra la versión actual.
- Crear un único informe por auditoría real: `AUDITORIA_NNN_AAAA-MM-DD_HH-mm.md`, con el siguiente número disponible y fecha/hora reales en `America/Lima`, formato 24 horas.
- No reutilizar números ni sobrescribir informes históricos. Un problema resuelto después se registra en el informe nuevo con referencia al hallazgo original.
- Incluir versión y alcance, verificaciones y resultados, limitaciones, hallazgos con evidencia/prioridad/estado, cambios documentales y pendientes. Distinguir documentación, código y definiciones del equipo.
- Documentar un problema de código no lo convierte en corregido. Evitar informes nuevos por retoques pequeños y repeticiones extensas del historial.
