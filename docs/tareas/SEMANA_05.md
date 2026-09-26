# Semana 05 — Entrega del primer avance

> **Plan histórico.** Se conserva el contenido y las casillas de esta semana; el nombre usado entonces era MealFlow (hoy Sazloop). Para progreso y pendientes vigentes, consultar [ESTADO_ACTUAL.md](ESTADO_ACTUAL.md).

## Objetivo

Completar y revisar todos los entregables del primer avance mediante trabajo paralelo entre los cinco integrantes.

**Entrega:** martes 8 de septiembre de 2026 a las 6:00 p. m.

## Forma de trabajo

La distribución sigue cuatro reglas:

1. Cada integrante tendrá un entregable principal diferente.
2. Cada integrante investigará un antecedente.
3. Cada integrante diseñará al menos una pantalla o formulario.
4. Cada integrante se hará responsable de cinco o más clases de dominio.
5. Cada categoría tendrá un único responsable; nadie modificará las clases de otro rubro sin coordinación previa.

## Propiedad exclusiva por rubro

| Responsable | Rubro del dominio | Cantidad de clases |
|---|---|---:|
| Emanuel | Identidad y estructura empresarial | 7 |
| Daniel | Afiliación y control documental | 6 |
| Franco | Packs y ofertas | 5 |
| Piero | Reservas y recojo | 5 |
| Alejandro | Pagos, postventa y experiencia | 8 |

Las clases pueden relacionarse entre rubros, pero su archivo y sus reglas pertenecen únicamente al responsable indicado. Una relación con una clase ajena no autoriza a modificarla.

Los archivos individuales deberán guardarse separados para evitar que dos personas editen el mismo documento al mismo tiempo. La integración final se realizará después de recibir todos los aportes.

## Fuentes que todos deben leer

Antes de empezar, cada integrante debe revisar:

- [Descripción y alcance](../PROYECTO.md)
- [Glosario](../GLOSARIO.md)
- [Requisitos](../requisitos/REQUISITOS.md)
- [Flujos del negocio](../FLUJOS_NEGOCIO.md)
- [Modelo de dominio](../decisiones/MODELO_DOMINIO.md)
- [Arquitectura](../ARQUITECTURA.md)
- [Guía de colaboración](../../CONTRIBUTING.md)

No se deben cambiar nombres, reglas o relaciones sin actualizar primero la documentación correspondiente.

## Trabajo común de cada integrante

Cada persona deberá entregar:

- Un antecedente de una solución similar.
- Una pantalla o formulario mobile-first.
- Las clases de dominio que le fueron asignadas.
- Una explicación breve de su trabajo para la exposición.
- Una revisión del informe y los diagramas antes de la entrega.

### Contenido mínimo de cada antecedente

El antecedente debe explicar:

1. Nombre y propósito de la solución.
2. Problema que atiende.
3. Funcionamiento principal.
4. Similitudes con MealFlow.
5. Diferencias o limitaciones.
6. Una enseñanza aplicable al proyecto.
7. Fuentes consultadas.

No debe copiarse el texto de las páginas consultadas. Cada integrante redactará con sus propias palabras.

## Distribución individual

### Emanuel — Coordinación, capítulo 1 e identidad

#### Entregable principal

Redactar el capítulo 1 del informe:

- Definición y descripción del problema.
- Objetivo general.
- Objetivos específicos.
- Alcance.
- Limitaciones.

Usar [PROYECTO.md](../PROYECTO.md) como fuente principal. No agregar funciones que estén fuera del alcance aprobado.

#### Antecedente

- **Too Good To Go**
- Fuente inicial: https://www.toogoodtogo.com/en-us/how-does-the-app-work
- Guardar el aporte por separado con el nombre `emanuel_too_good_to_go`.

#### Prototipo

- Formulario de solicitud de afiliación.
- Debe permitir registrar el establecimiento y adjuntar sus documentos.
- Debe mostrar que la solicitud será revisada antes de permitir ventas.

#### Clases de dominio

1. `Usuario`
2. `Consumidor`
3. `PropietarioEmpresa`
4. `Empresa`
5. `Establecimiento`
6. `Direccion`
7. `CuentaEstablecimiento`

Para este avance se implementará la estructura acordada, no toda la lógica futura.

#### Exposición

Presentar el problema, la oportunidad, la solución y el alcance.

#### Responsabilidad adicional

Integrar los aportes finales en la plantilla del informe sin reescribir el trabajo de los demás. Comprobar títulos, numeración y coherencia general.

---

### Daniel — Backend base, afiliación y diagrama de clases

#### Entregable principal

Generar primero el proyecto Spring Boot:

- Java 21.
- Maven.
- Spring Boot.
- Spring Data JPA.
- MySQL.
- Estructura modular definida en [ARQUITECTURA.md](../ARQUITECTURA.md).
- Verificar que el proyecto compile antes de que se agreguen las demás clases.

Esta es la única tarea habilitadora. Debe realizarse al inicio, mientras los demás avanzan sus documentos y prototipos.

También elaborará el diagrama de clases utilizando las 31 clases registradas. Deberá mostrar relaciones, multiplicidades y los atributos principales, sin llenar el diagrama con métodos innecesarios.

#### Antecedente

- **Phenix**
- Fuente inicial: https://www.wearephenix.com/en/application-anti-waste/
- Guardar el aporte por separado con el nombre `daniel_phenix`.

#### Prototipo

- Formulario de publicación de oferta.
- Debe permitir configurar tamaños, precio referencial, precio con descuento, stock, horario y límite de recojo.
- Debe respetar la regla de una sola oferta activa por establecimiento.

#### Clases de dominio

1. `SolicitudAfiliacion`
2. `DocumentoEstablecimiento`
3. `RevisionDocumento`
4. `EvaluacionAfiliacion`
5. `AdministradorPlataforma`
6. `SuspensionEstablecimiento`

#### Exposición

Explicar la arquitectura, el backend y la parte de afiliación documental.

#### Criterio de finalización

El backend base debe compilar y el diagrama debe coincidir con el modelo de dominio antes de marcar estas tareas como terminadas.

---

### Franco — Persistencia, ofertas y modelo entidad-relación

#### Entregable principal

Elaborar el modelo entidad-relación:

- Identificar qué clases necesitan una tabla.
- Definir claves primarias y foráneas.
- Mostrar cardinalidades.
- Resolver las relaciones de muchos a muchos cuando corresponda.
- No convertir automáticamente todas las clases de dominio en tablas.

También revisará la configuración de MySQL y Spring Data JPA, comprobará el arranque final del backend y preparará el ZIP cuando el código haya sido integrado.

#### Antecedente

- **Flashfood**
- Fuente inicial: https://flashfood.com/
- Guardar el aporte por separado con el nombre `franco_flashfood`.

#### Prototipo

- Pantalla de búsqueda y confirmación de recojo.
- Debe permitir ingresar el código presentado por el consumidor.
- Debe mostrar los datos esenciales de la reserva y un botón para confirmar la entrega.
- Un código utilizado no debe aparentar que puede utilizarse nuevamente.

#### Clases de dominio

1. `PackSorpresa`
2. `OfertaPack`
3. `VariantePack`
4. `Alergeno`
5. `RestriccionAlimentaria`

#### Exposición

Explicar la publicación del pack, sus variantes y la estructura general de la base de datos.

#### Criterio de finalización

El modelo ER deberá coincidir con el diagrama de clases y el backend deberá iniciar correctamente después de integrar todos los módulos.

---

### Piero — Casos de uso, base teórica, reservas y recojo

#### Entregable principal

Elaborar el diagrama de casos de uso utilizando los requisitos funcionales existentes.

Actores mínimos:

- Consumidor.
- Propietario de empresa.
- Cuenta del establecimiento.
- Administrador de plataforma.

El diagrama debe mostrar los casos principales, no cada clic o campo del sistema.

También redactará la base teórica con un máximo de dos páginas. Debe cubrir únicamente conceptos necesarios para comprender el proyecto, por ejemplo:

- Desperdicio alimentario.
- Marketplace digital.
- Diseño web mobile-first.
- Arquitectura por capas.
- Persistencia de datos.

#### Antecedente

- **ResQ Club**
- Fuente inicial: https://www.resq-club.com/
- Guardar el aporte por separado con el nombre `piero_resq_club`.

#### Prototipo

- Formulario de búsqueda y filtros junto con el detalle del pack.
- Debe permitir filtrar por ubicación textual, precio y calificación.
- El detalle debe mostrar posibles productos, precio, horario, alérgenos y restricciones.

#### Clases de dominio

1. `RetencionStock`
2. `Reserva`
3. `DetalleReserva`
4. `CodigoRecojo`
5. `Recojo`

#### Exposición

Explicar los actores, los casos de uso y el proceso de reserva y recojo.

#### Criterio de finalización

Los casos de uso deben corresponder con los requisitos RF-01 a RF-26 y la base teórica no debe superar dos páginas.

---

### Alejandro — Gestión, pagos, postventa e impacto

#### Entregable principal

Elaborar el Project Charter y el cronograma.

El Project Charter debe incluir:

- Nombre del proyecto y lema.
- Problema y justificación.
- Objetivo general.
- Alcance resumido.
- Entregables.
- Equipo y responsabilidades.
- Principales riesgos.
- Fechas académicas confirmadas.

El cronograma debe cubrir desde el primer avance hasta la entrega final de la semana 18 y mostrar actividades, responsables y semanas.

#### Antecedente

- **Karma**
- Fuente inicial: https://karma.life/app
- Guardar el aporte por separado con el nombre `alejandro_karma`.

#### Prototipo

- Formulario de reserva y pago simulado.
- Debe permitir elegir variante, cantidad y medio de pago.
- El efectivo solo debe aparecer cuando la compra provenga del QR presencial.
- Debe indicar claramente que el pago y la boleta son simulados.

#### Clases de dominio

1. `Pago`
2. `BoletaSimulada`
3. `CancelacionReserva`
4. `Reembolso`
5. `ComisionPlataforma`
6. `Resena`
7. `Notificacion`
8. `ImpactoCO2Evitado`

#### Exposición

Explicar los pagos simulados, el modelo de negocio, las cancelaciones, las comisiones, las reseñas y el impacto esperado.

#### Criterio de finalización

El Project Charter, el cronograma y las clases deben utilizar el mismo alcance y fechas que la documentación oficial.

## Orden de ejecución

### Bloque 1 — Trabajo paralelo inmediato

Todos pueden comenzar sin esperar:

- Antecedentes.
- Prototipos.
- Capítulo 1.
- Base teórica.
- Casos de uso.
- Modelo ER.
- Project Charter.
- Cronograma.
- Diagrama de clases.

Daniel debe generar el backend base como primera prioridad.

### Bloque 2 — Integración del backend

Cuando el proyecto base compile y el diagrama de clases haya sido revisado:

1. Cada integrante actualiza su rama desde `main`.
2. Implementa únicamente sus clases asignadas.
3. Daniel y Franco revisan las relaciones técnicas.
4. Se integran los módulos sin mezclar responsabilidades.

Mientras se espera este bloque, nadie debe quedarse detenido: continúa con su antecedente, prototipo o exposición.

### Bloque 3 — Integración académica

- Emanuel incorpora los textos en la plantilla.
- Se agregan los diagramas y prototipos terminados.
- Cada integrante revisa su aporte dentro del informe.
- Franco comprueba el backend y prepara el ZIP.
- Los cinco preparan y ensayan la exposición.

## Reglas para evitar conflictos

- Cada integrante trabajará en una rama propia.
- Cada rubro y cada clase tendrán un único responsable.
- Nadie modificará archivos de otro rubro sin conversarlo con su responsable.
- Las relaciones entre rubros se acordarán mediante el diagrama y la documentación, sin trasladar la propiedad de las clases.
- Los antecedentes se guardarán como archivos separados.
- Los prototipos tendrán nombres diferentes.
- Solo Emanuel editará la copia principal del informe durante la integración.
- Daniel será responsable del archivo base del diagrama de clases.
- Franco será responsable del archivo base del modelo ER.
- Piero será responsable del archivo base del diagrama de casos de uso.
- No se enviarán archivos finales únicamente por WhatsApp; deben quedar respaldados en el repositorio.
- Antes de comenzar código, se debe hacer Fetch y Pull desde GitHub Desktop.

## Lista final de entrega

- [ ] Informe en PDF.
- [ ] Cinco antecedentes incorporados.
- [ ] Project Charter.
- [ ] Cronograma.
- [ ] Diagrama de casos de uso.
- [ ] Diagrama de clases.
- [ ] Modelo entidad-relación.
- [ ] Cinco prototipos identificables.
- [ ] Backend configurado con las clases de dominio.
- [ ] Backend probado.
- [ ] Código preparado en ZIP.
- [ ] Presentación de 6 a 10 minutos.
- [ ] Participación preparada para los cinco integrantes.
