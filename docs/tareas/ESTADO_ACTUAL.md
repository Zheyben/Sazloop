# Estado actual del proyecto

**Actualización:** 26 de septiembre de 2026.  
**Proyecto:** N.º 91 — SalvemosLaComida.  
**Plataforma:** Sazloop.  
**Etapa:** Inicio del segundo avance del proyecto final.

## Avance comprobado

- Informe corregido en `docs/informe/Informe_Primer_Avance.docx`.
- Problema delimitado a Lima Metropolitana; objetivo general y cuatro objetivos específicos.
- 26 RF agrupados en seis módulos, con redacción “El sistema deberá…”, prioridad y dependencias.
- Cinco antecedentes, fundamento teórico, RNF y referencias presentes.
- Tres diagramas con archivos editables; 31 clases y 33 tablas físicas contando dos asociativas.
- Cinco prototipos de Google Stitch incorporados al informe.
- Proyecto Spring Boot con Java 21 declarado, 31 entidades JPA y perfiles H2/MySQL.
- Nombre Sazloop sincronizado en documentación vigente, Maven, paquete Java, clase de entrada, prueba y título DBML.

## Pendientes confirmados en la revisión

- Sin pendientes

## Estado real del código

Hay modelo de persistencia y configuración inicial. No hay controllers, services, repositories ni DTO; `frontend/` contiene solo `.gitkeep`. Las reglas de stock, pago, cancelación y recojo están especificadas, pero no se acreditan como ejecutadas. No se creó funcionalidad adicional en esta actualización.

La comprobación estática del cambio de identidad verifica rutas, imports y conservación de mapeos. Maven no pudo descargar el POM padre por fallo DNS de `repo.maven.apache.org`. El entorno de revisión dispone de Java 17, mientras el proyecto exige Java 21. Se necesita ejecutar las pruebas existentes con JDK 21 y acceso a dependencias; no hay resultado de compilación exitosa en esta revisión.

## Siguiente tarea solicitada, aún no incorporada

- Evaluación de integración futura con empresas externas de delivery.
- Investigación del registro de ubicación mediante GPS.
- Evaluación de MySQL para la cantidad teórica de usuarios que se defina.
- Confirmación formal de la tasa de comisión del 10 % registrada en DEC-036.
- Renovación del cronograma completo y Project Charter.
- Corrección de citas y aclaración de la asignación oficial dentro del DOCX.

Las notas de GPS/delivery ya presentes en prototipos permanecen como investigación futura. No se habilitan en V1 ni se añaden RF.

Los planes [SEMANA_04.md](SEMANA_04.md) y [SEMANA_05.md](SEMANA_05.md) conservan la planificación histórica. Sus casillas no representan el estado actual.
