# Diagramas del primer avance

Los tres diagramas ya están incorporados en el informe corregido de Sazloop. Se conservan sus fuentes:

| Diagrama | Archivo editable | Estado de revisión |
|---|---|---|
| Casos de uso | [01-Diagrama de casos de uso.drawio](<01-Diagrama de casos de uso.drawio>) | Incluye los 26 IDs CU/RF agrupados; quedan observaciones de notación UML. |
| Clases | [02-Diagrama de clases.drawio](<02-Diagrama de clases.drawio>) | Presenta 31 clases, atributos y métodos; persisten diferencias de relaciones frente al código y ER. |
| Entidad-relación | [03-Diagrama ER de base de datos.txt](<03-Diagrama ER de base de datos.txt>) | Fuente DBML: 33 tablas, incluidas dos asociativas. Se actualizó únicamente el nombre del proyecto a Sazloop. |

Las clases y el ER están reducidos a un tamaño difícil de leer en el informe. Debe facilitarse el diagrama legible como archivo adjunto o exportación enlazada, como indicó el profesor. Se pueden utilizar estos editables; no hace falta inventar diagramas adicionales ni agregar una tabla de clases al informe.

Las 31 entidades originan 31 tablas principales y dos tablas muchos a muchos (`pack_alergenos`, `pack_restricciones`). La coherencia requiere revisar también las relaciones, claves y cardinalidades, no igualar artificialmente el número de clases y tablas.
