# Semana 06 — Corrección del primer avance

## Objetivo

Levantar las observaciones realizadas por el profesor al primer avance y dejar el informe corregido, coherente y trazable antes de comenzar el segundo avance.

**Responsable único de este archivo:** Emanuel  
**Alcance geográfico acordado:** Lima Metropolitana como etapa inicial, con posibilidad de expansión progresiva a otras ciudades y, posteriormente, a todo el Perú.

## Regla de trabajo

Las tareas se realizarán en el orden indicado. Emanuel trabajará una tarea principal a la vez y solo la marcará como completada cuando cumpla su criterio de aprobación.

Las casillas corresponden únicamente a entregables principales. Los puntos internos describen el trabajo necesario y no se marcarán por separado.

La versión presentada del informe se conservará como evidencia. Las correcciones se realizarán sobre una copia nueva y no se reemplazará el archivo original hasta terminar la revisión completa.

## Criterio general de evaluación

Cada resultado será revisado con el nivel de exigencia de la rúbrica del profesor:

- Debe responder exactamente a la observación recibida.
- Debe utilizar evidencia y referencias cuando corresponda.
- Debe mantener coherencia con los requerimientos, diagramas, prototipos y código.
- No debe afirmar que una función está implementada si solo ha sido diseñada o prototipada.
- No se aprobará una tarea por cantidad de contenido, sino por claridad, corrección y trazabilidad.
- La meta de la revisión simulada será alcanzar al menos 18 de 20 puntos y no dejar ningún criterio en nivel Regular o Insuficiente.

## Tareas de Emanuel

### 1. Corrección del Capítulo 1

- [ ] Corregir la identificación del proyecto, la definición del problema, los objetivos, el alcance y las limitaciones.

Trabajo requerido:

- Indicar que la propuesta corresponde oficialmente al **proyecto N.º 91 SalvemosLaComida**.
- Explicar que MealFlow fue la identidad comercial adoptada posteriormente y que el problema, la oportunidad y la solución del proyecto original se mantienen.
- Mantener MealFlow como nombre provisional mientras se evalúa una identidad diferenciable de otras soluciones del sector.
- Reescribir la problemática empezando por la situación negativa, sus causas, los afectados y sus consecuencias.
- Incorporar evidencia procedente de fuentes institucionales y citarla dentro del texto.
- Delimitar la primera etapa del proyecto a Lima Metropolitana.
- Presentar la expansión a otras ciudades y al resto del Perú como crecimiento futuro, no como alcance implementado de la primera versión.
- Redactar un objetivo general con un solo verbo principal en infinitivo.
- Reducir los objetivos específicos a un máximo de cuatro, con un solo verbo principal y un resultado verificable en cada uno.
- Revisar que el alcance describa únicamente funciones comprometidas.
- Mantener GPS, mapas completos e integración con empresas de delivery como elementos por investigar o extensiones futuras mientras no se apruebe su implementación.

Archivos relacionados:

- `docs/informe/Informe_Primer_Avance.docx`
- `docs/PROYECTO.md`, únicamente si la decisión aprobada también debe consolidarse en la documentación principal.

**Criterio de aprobación:** el problema debe quedar delimitado, sustentado y centrado en las consecuencias negativas; el objetivo general debe ser único y medible; deben existir como máximo cuatro objetivos específicos coherentes con el alcance.

### 2. Normalización de los requerimientos

- [ ] Revisar y normalizar los requerimientos funcionales y no funcionales del informe.

Trabajo requerido:

- Conservar identificadores estables.
- Agrupar los requerimientos funcionales según los módulos del sistema.
- Redactar cada requerimiento con la estructura «El sistema deberá...».
- Separar requerimientos que contengan más de una función.
- Identificar actor, acción, condición y resultado cuando corresponda.
- Añadir o revisar su prioridad.
- Convertir los requerimientos no funcionales en condiciones verificables.
- Eliminar términos vagos como «rápido», «seguro» o «fácil» cuando no tengan un criterio medible.
- Comprobar que ningún requerimiento contradiga el alcance de Lima Metropolitana ni presente GPS o delivery como funciones ya implementadas.

Archivos relacionados:

- `docs/requisitos/REQUISITOS.md`
- `docs/informe/Informe_Primer_Avance.docx`

**Criterio de aprobación:** todos los requerimientos deben ser comprensibles, clasificables, verificables y consistentes con el alcance aprobado.

### 3. Control de observaciones y trazabilidad

- [ ] Elaborar el control de observaciones del primer avance y la base de la matriz de trazabilidad.

Trabajo requerido:

- Registrar cada observación textual del profesor.
- Relacionar cada observación con el criterio de la rúbrica afectado.
- Indicar el archivo o sección que debe corregirse.
- Registrar evidencia de la corrección y su estado.
- Construir la cadena de trazabilidad:
  - Módulo.
  - Requerimiento funcional.
  - Caso de uso.
  - Clase de dominio.
  - Tabla o justificación de no persistencia.
  - Pantalla o prototipo.
  - Prueba prevista.
- No inventar relaciones para completar la matriz; cada vínculo debe existir realmente.

Entregables previstos:

- `docs/seguimiento/OBSERVACIONES_PRIMER_AVANCE.md`
- `docs/trazabilidad/MATRIZ_TRAZABILIDAD.md`

**Criterio de aprobación:** ninguna observación del profesor debe quedar sin responsable, evidencia esperada o estado; cada elemento trazado debe utilizar nombres consistentes.

### 4. Auditoría de coherencia del modelado

- [ ] Auditar la correspondencia entre requerimientos, diagramas, prototipos y código.

Trabajo requerido:

- Verificar que cada caso de uso derive de uno o más requerimientos funcionales.
- Revisar que los actores tengan nombres consistentes en todo el proyecto.
- Comprobar que el diagrama de clases represente el dominio completo o esté dividido en diagramas modulares legibles.
- Confirmar que las clases relevantes incluyan atributos, métodos, relaciones y multiplicidades correctas.
- Verificar que el modelo entidad-relación sea legible y corresponda con las entidades persistentes del diagrama de clases y del código.
- Comprobar que los prototipos representen los mismos flujos definidos por los requerimientos.
- Registrar las diferencias encontradas y comunicarlas al responsable del archivo técnico.
- No modificar directamente los archivos fuente pertenecientes a Daniel, Franco o Piero sin coordinación previa.

**Criterio de aprobación:** no debe existir una contradicción sin documentar entre un requerimiento, un diagrama, una clase, una tabla, una pantalla y la implementación correspondiente.

### 5. Corrección académica y bibliográfica

- [ ] Corregir las citas, referencias y afirmaciones inconsistentes del informe.

Trabajo requerido:

- Verificar que los cinco antecedentes tengan citas dentro del texto y referencias completas.
- Mantener el fundamento teórico dentro del máximo de dos páginas establecido por la consigna.
- Agregar las nuevas fuentes utilizadas en la definición del problema.
- Corregir referencias duplicadas, incompletas o asociadas al antecedente equivocado.
- Eliminar marcadores internos como `[cite: 5]` y cualquier texto pendiente de edición.
- Unificar nombres de actores, módulos, clases y tecnologías.
- Comprobar las versiones tecnológicas contra el repositorio antes de presentarlas como definitivas.
- Diferenciar con claridad lo implementado, lo diseñado, lo prototipado y lo propuesto para el futuro.

**Criterio de aprobación:** toda afirmación externa debe tener respaldo identificable y el documento no debe contener marcadores, contradicciones o referencias que no correspondan al texto.

### 6. Integración y evaluación final del informe corregido

- [ ] Integrar las correcciones y evaluar nuevamente el informe completo con la rúbrica.

Trabajo requerido:

- Conservar la versión originalmente presentada.
- Crear una copia de trabajo corregida.
- Incorporar únicamente diagramas y evidencias validados por sus responsables.
- Actualizar la tabla de contenido, numeración, títulos de figuras, tablas y anexos.
- Comprobar la legibilidad de los diagramas; adjuntarlos por separado si no pueden leerse dentro del informe.
- Verificar que el cronograma abarque el proyecto completo hasta la semana 18.
- Revisar visualmente el documento exportado antes de considerarlo terminado.
- Simular la calificación en los seis criterios del primer informe y justificar el puntaje estimado.
- Registrar cualquier observación que deba trasladarse al segundo avance.

**Criterio de aprobación:** la revisión simulada no debe dejar criterios en nivel Regular o Insuficiente y todas las observaciones del primer avance deben estar corregidas o justificadas antes de iniciar el segundo avance.

## Orden de ejecución de la semana

1. Corregir el Capítulo 1.
2. Normalizar los requerimientos.
3. Crear el control de observaciones y la matriz de trazabilidad.
4. Auditar la coherencia del modelado.
5. Corregir citas y consistencia académica.
6. Integrar y evaluar la nueva versión del informe.

## Condición para comenzar el segundo avance

El segundo avance comenzará únicamente cuando:

- El informe corregido haya sido evaluado nuevamente con la rúbrica.
- Las observaciones del profesor estén registradas y atendidas.
- Los requerimientos y diagramas utilicen nombres consistentes.
- Las diferencias que dependan de otros integrantes tengan un responsable y una acción concreta.
