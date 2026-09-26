# Flujos principales del negocio

Este documento explica cómo interactúan los usuarios con la plataforma. Describe el comportamiento esperado de Sazloop, no una implementación terminada. Los 26 RF corregidos se mantienen en [REQUISITOS.md](requisitos/REQUISITOS.md); la ejecución de estos flujos no está acreditada por los prototipos.

## 1. Registro y afiliación de un establecimiento

```mermaid
flowchart TD
    A["Usuario crea una cuenta"] --> B["Activa su perfil empresarial"]
    B --> C["Registra empresa y establecimiento"]
    C --> D["Adjunta documentos obligatorios"]
    D --> E["Administrador revisa cada documento"]
    E --> F{"¿Solicitud conforme?"}
    F -->|Sí| G["Establecimiento aprobado"]
    F -->|No| H["Solicitud rechazada con motivo"]
    H --> I["Propietario presenta una nueva solicitud"]
```

Reglas principales:

- Los documentos se evalúan por establecimiento.
- El administrador realiza la revisión manualmente.
- Un rechazo no elimina la cuenta, la empresa ni el establecimiento.
- La cuenta operativa del local se crea después de su aprobación.

## 2. Publicación de una oferta

```mermaid
flowchart TD
    A["Cuenta del establecimiento ingresa"] --> B["Define la oferta del día"]
    B --> C["Agrega tamaños, precios y stock"]
    C --> D["Configura horario y límite de recojo"]
    D --> E{"¿Ya existe una oferta activa?"}
    E -->|No| F["Publica la oferta"]
    E -->|Sí| G["Debe cerrar o esperar la oferta actual"]
```

El establecimiento puede mantener la descripción general de su pack, pero solo tendrá una oferta activa simultáneamente. Cada tamaño funciona como una variante con precio y stock propios.

## 3. Compra remota

```mermaid
flowchart TD
    A["Consumidor busca establecimientos"] --> B["Abre una oferta"]
    B --> C["Elige variante y cantidad"]
    C --> D["Realiza pago digital simulado"]
    D --> E{"¿Pago aprobado?"}
    E -->|Sí| F["Se confirma reserva y stock"]
    E -->|No| G["No se confirma la compra"]
    F --> H["Se genera boleta y código"]
```

Reglas principales:

- Los medios remotos son tarjeta de débito simulada y Yape simulado.
- La reserva y la actualización definitiva del stock se ejecutan de forma consistente.
- El contenido exacto del pack continúa siendo sorpresa.

## 4. Compra presencial mediante QR

```mermaid
flowchart TD
    A["Consumidor escanea QR del local"] --> B["Abre la oferta activa"]
    B --> C["Elige variante y forma de pago"]
    C --> D{"¿Pago en efectivo?"}
    D -->|Sí| E["Se retiene el stock"]
    E --> F["Personal confirma dinero recibido"]
    F --> G["Pago y recojo completados"]
    D -->|No| H["Pago digital simulado"]
    H --> I["Se genera código de recojo"]
    I --> J["Personal confirma la entrega"]
```

El efectivo solo está disponible para una compra presencial. La retención temporal evita vender las mismas unidades a otra persona mientras el personal confirma el cobro.

## 5. Confirmación del recojo y reseña

```mermaid
flowchart TD
    A["Consumidor presenta su código"] --> B["Personal busca la reserva"]
    B --> C{"¿Código válido y pendiente?"}
    C -->|Sí| D["Marca el recojo"]
    D --> E["Código queda utilizado"]
    E --> F["Consumidor puede reseñar"]
    C -->|No| G["No se entrega desde esa reserva"]
```

Pagar no equivale a recibir el pack. El recojo es el hecho que cierra correctamente la compra y habilita la calificación.

## 6. Cancelación de una reserva remota

```mermaid
flowchart TD
    A["Consumidor solicita cancelar"] --> B{"¿Sigue pendiente y dentro del límite?"}
    B -->|Sí| C["Reserva cancelada"]
    C --> D["Reembolso simulado del 50 %"]
    C --> E["Stock vuelve si aún puede venderse"]
    C --> F["Comisión sobre importe retenido"]
    B -->|No| G["Cancelación no permitida"]
```

No se puede cancelar una reserva ya recogida, cancelada o expirada. Las compras presenciales completadas no tienen cancelación mediante la plataforma.

## 7. Vencimiento de un documento

```mermaid
flowchart TD
    A["Vence documento obligatorio"] --> B["Establecimiento suspendido"]
    B --> C["No puede publicar nuevas ofertas"]
    C --> D["Propietario presenta renovación"]
    D --> E["Administrador revisa manualmente"]
    E --> F{"¿Documento aprobado?"}
    F -->|Sí| G["Establecimiento reactivado"]
    F -->|No| H["Continúa suspendido"]
```

La suspensión conserva la cuenta, el historial y la información del establecimiento. Las reservas pagadas antes de la suspensión deben ser atendidas.
