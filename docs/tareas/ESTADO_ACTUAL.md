# Estado actual del proyecto

**Actualización:** 28 de septiembre de 2026.  
**Proyecto:** N.º 91 — SalvemosLaComida.  
**Plataforma:** Sazloop.  
**Etapa:** Avance 1 completo; planificación del avance 2 desde la semana 08.

## Avance 1 completado

- Informe en [`docs/informe/Avance_del_proyecto_01.docx`](../informe/Avance_del_proyecto_01.docx).
- Problema delimitado a Lima Metropolitana; objetivo general y cuatro objetivos específicos.
- 26 RF agrupados en seis módulos, con redacción “El sistema deberá…”, prioridad y dependencias.
- Cinco antecedentes, fundamento teórico, RNF y referencias presentes.
- Tres diagramas con archivos editables; 31 clases y 33 tablas físicas contando dos asociativas.
- Cinco prototipos de Google Stitch incorporados al informe.
- Proyecto Spring Boot con Java 21 declarado, 31 entidades JPA y perfiles H2/MySQL.
- Nombre Sazloop sincronizado en documentación vigente, Maven, paquete Java, clase de entrada, prueba y título DBML.

## Estado real del código

Hay modelo de persistencia y configuración inicial. No hay controllers, services, repositories ni DTO; `frontend/` contiene solo `.gitkeep`. Las reglas de stock, pago, cancelación y recojo están especificadas, pero no se acreditan como ejecutadas. No se creó funcionalidad adicional en esta actualización.

## Plan semanal del avance 2 y cierre final

Las asignaciones propuestas para los cinco integrantes están en los archivos
[`SEMANA_08.md`](SEMANA_08.md) a [`SEMANA_18.md`](SEMANA_18.md), uno por semana.
La entrega del segundo avance corresponde a la semana 10 y la entrega final a la
semana 18, conforme a las fechas confirmadas en [`PROYECTO.md`](../PROYECTO.md).
Las tareas planificadas no acreditan funciones implementadas. El estado real del
código se actualizará cuando los aportes hayan sido incorporados y comprobados.

Los planes [SEMANA_04.md](SEMANA_04.md) y [SEMANA_05.md](SEMANA_05.md) conservan la planificación histórica. Sus casillas no representan el estado actual.
