# Guía para colaborar

Esta guía explica la forma de trabajar en Sazloop mediante GitHub Desktop.

## Antes de comenzar

1. Lee [docs/README.md](docs/README.md).
2. Revisa [docs/tareas/ESTADO_ACTUAL.md](docs/tareas/ESTADO_ACTUAL.md).
3. Confirma cuál es tu tarea en el archivo de la semana.
4. En GitHub Desktop, selecciona **Fetch origin** y después **Pull origin** si existen cambios.
5. Comprueba que no haya otra persona modificando los mismos archivos.

## Uso de ramas

No trabajes directamente sobre `main` salvo que el coordinador lo acuerde expresamente.

Crea una rama corta según el tipo de trabajo:

- `docs/nombre-tarea`
- `feature/nombre-funcion`
- `fix/nombre-error`
- `design/nombre-prototipo`

Ejemplos:

- `docs/requisitos-funcionales`
- `feature/publicar-oferta`
- `design/formulario-afiliacion`

## Propiedad exclusiva de los rubros

| Responsable | Módulos o rubros |
|---|---|
| Emanuel | Identidad y estructura empresarial |
| Daniel | Afiliación y control documental |
| Franco | Packs y ofertas |
| Piero | Reservas y recojo |
| Alejandro | Transacciones y experiencia posterior |

Cada clase tiene un solo responsable. Relacionarse con una clase de otro módulo no permite modificarla.

Si necesitas un cambio en otro rubro:

1. Explica la necesidad a su responsable.
2. Acuerden el cambio en la documentación o el diagrama.
3. El responsable del rubro modifica su clase.
4. Ambos verifican que la relación siga funcionando.

Los cambios en `shared` requieren revisión técnica de Daniel y Franco.

## Flujo con GitHub Desktop

1. Actualiza `main`.
2. Crea una rama desde `main`.
3. Realiza únicamente la tarea asignada.
4. Revisa los archivos modificados en GitHub Desktop.
5. Escribe un commit claro.
6. Publica la rama con **Publish branch** o **Push origin**.
7. Abre un Pull Request.
8. Solicita que otro integrante revise el cambio.
9. Corrige las observaciones antes de unirlo a `main`.

## Formato de commits

Usaremos mensajes cortos:

- `docs: describir el cambio`
- `feat: describir la funcionalidad`
- `fix: describir la corrección`
- `test: describir la prueba`
- `chore: describir la configuración`

Cada commit debe contener un solo propósito comprensible.

## Cuándo una tarea está terminada

Una tarea se considera completa cuando:

- Cumple lo solicitado.
- Utiliza los nombres definidos en el glosario y modelo de dominio.
- Respeta la propiedad del módulo y no modifica clases ajenas sin coordinación.
- No contradice las decisiones registradas.
- Los archivos relacionados fueron actualizados.
- El proyecto continúa iniciando correctamente cuando el cambio contiene código.
- El cambio fue subido y revisado.
- La casilla correspondiente se marcó en el archivo semanal.

## Cambios en el funcionamiento

Si durante una tarea necesitas cambiar una regla:

1. No la cambies silenciosamente en el código.
2. Explícala al equipo.
3. Actualiza primero el documento oficial correspondiente.
4. Revisa requisitos, flujos, diagramas y prototipos afectados.
5. Después modifica el código.

## Conflictos

Si GitHub Desktop informa un conflicto:

- No elijas una versión al azar.
- Comunícate con la persona que modificó el mismo archivo.
- Comparen ambos cambios.
- Conserven el contenido vigente y vuelvan a comprobar el documento completo.

## Archivos que no deben subirse

- Contraseñas, tokens o claves.
- Archivos `.env` con datos reales.
- Carpetas generadas por el IDE.
- Archivos temporales del sistema.
- Dependencias o compilaciones generadas que Maven pueda reconstruir.

Revisa siempre la lista de cambios antes de realizar el commit.
