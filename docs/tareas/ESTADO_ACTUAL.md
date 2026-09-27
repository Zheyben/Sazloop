# Estado actual del proyecto

**Actualización:** 27 de septiembre de 2026.  
**Proyecto:** N.º 91 — SalvemosLaComida.  
**Plataforma:** Sazloop.  
**Etapa:** Avance 1 completo; siguiente etapa: avance 2.

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

## Siguiente etapa

Corresponde avanzar con el **avance 2**. Sus tareas y especificaciones se definirán por separado.

Los planes [SEMANA_04.md](SEMANA_04.md) y [SEMANA_05.md](SEMANA_05.md) conservan la planificación histórica. Sus casillas no representan el estado actual.
