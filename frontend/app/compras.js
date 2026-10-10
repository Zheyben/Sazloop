import { solicitar, el, dinero, fechaHora, aviso, tamanios } from './comun.js';
import { leerCarrito, guardarCarrito } from './portal.js';
import { abrirResena } from './resenas-portal.js';

const formatoFechaCompra = new Intl.DateTimeFormat('es-PE', { day: 'numeric', month: 'short', year: 'numeric' });
const formatoHoraCompra = new Intl.DateTimeFormat('es-PE', { hour: '2-digit', minute: '2-digit', hour12: false });

function horarioCompra(compra) {
  const inicio = new Date(compra.inicioRecojo);
  const fin = new Date(compra.finRecojo);
  const fechaInicio = formatoFechaCompra.format(inicio);
  const fechaFin = formatoFechaCompra.format(fin);
  return fechaInicio === fechaFin
    ? `${fechaInicio} · ${formatoHoraCompra.format(inicio)} – ${formatoHoraCompra.format(fin)}`
    : `${fechaInicio} ${formatoHoraCompra.format(inicio)} – ${fechaFin} ${formatoHoraCompra.format(fin)}`;
}

const estados = {
  PENDIENTE_PAGO: 'Pendiente de pago', CONFIRMADA: 'Lista para recoger',
  RECOGIDA: 'Recogida', CANCELADA: 'Cancelada', EXPIRADA: 'Expirada'
};
let usuario;
export function montarCompras(usuarioActual) {
  usuario = usuarioActual;
  mostrarCarrito();
  mostrarHistorial();
}

async function mostrarCarrito() {
  const zona = document.querySelector('#carrito');
  const pedidos = leerCarrito(usuario.id);
  if (!pedidos.length) {
    zona.replaceChildren(el('div', { className: 'estado-vacio' },
      el('p', { textContent: 'Tu carrito está vacío.' }),
      el('a', { href: 'catalogo.html', textContent: 'Explorar productos' })));
    return;
  }

  zona.replaceChildren(el('p', { textContent: 'Revisando disponibilidad…' }));
  const ids = [...new Set(pedidos.map(p => p.ofertaId))];
  const consultas = await Promise.allSettled(ids.map(id => solicitar(`/ofertas/${id}`)));
  if (!zona.isConnected) return;
  const ofertas = new Map(ids.map((id, indice) => [id,
    consultas[indice].status === 'fulfilled' ? consultas[indice].value : null]));
  const formulario = el('form', { className: 'carrito-formulario' });
  const pedidosZona = el('div', { className: 'carrito-pedidos' });
  const mensaje = el('p', { role: 'status', ariaLive: 'polite' });
  const total = el('strong', { className: 'carrito-total' });
  const entradas = [];
  let noDisponibles = false;

  for (const [indice, pedido] of pedidos.entries()) {
    const oferta = ofertas.get(pedido.ofertaId);
    const quitar = el('button', { type: 'button', className: 'secundario carrito-quitar', textContent: 'Quitar del carrito', onclick: () => {
      guardarCarrito(usuario.id, leerCarrito(usuario.id).filter(p => p.clave !== pedido.clave));
      mostrarCarrito();
    } });
    if (!oferta) {
      noDisponibles = true;
      pedidosZona.append(el('article', { className: 'carrito-pedido no-disponible' },
        el('span', { className: 'etiqueta', textContent: `PEDIDO ${indice + 1}` }),
        el('p', { textContent: 'Esta oferta ya no está disponible. Quita el pedido para continuar.' }), quitar));
      continue;
    }
    const campos = pedido.lineas.map(linea => {
      const variante = oferta.variantes.find(v => v.id === linea.varianteId);
      if (!variante) return null;
      const input = el('input', { type: 'number', min: 0, max: variante.stockDisponible,
        step: 1, value: linea.cantidad, ariaLabel: `Cantidad de ${tamanios[variante.tamanio] || variante.tamanio} en pedido ${indice + 1}` });
      return { variante, input };
    }).filter(Boolean);
    if (!campos.length) {
      noDisponibles = true;
      pedidosZona.append(el('article', { className: 'carrito-pedido no-disponible' },
        el('span', { className: 'etiqueta', textContent: `PEDIDO ${indice + 1}` }),
        el('p', { textContent: 'Las variantes de este pedido ya no están disponibles.' }), quitar));
      continue;
    }
    const subtotal = el('strong', { className: 'carrito-subtotal' });
    entradas.push({ pedido, oferta, campos, subtotal });
    pedidosZona.append(el('article', { className: 'carrito-pedido' },
      el('header', { className: 'carrito-pedido-cabecera' },
        el('span', { className: 'etiqueta', textContent: `PEDIDO ${indice + 1}` }),
        el('h3', { textContent: oferta.pack }),
        el('p', { className: 'carrito-local', textContent: `${oferta.establecimiento} · ${oferta.distrito}` }),
        el('p', { className: 'carrito-horario', textContent: `Recojo: ${fechaHora(oferta.inicioRecojo)} a ${fechaHora(oferta.finRecojo)}` })),
      el('div', { className: 'carrito-lineas' }, ...campos.map(({ variante, input }) => filaVariante(variante, input))),
      el('div', { className: 'carrito-pedido-pie' },
        el('div', {}, el('span', { textContent: 'Subtotal' }), subtotal), quitar)));
  }

  const medio = el('select', { name: 'medioPago', hidden: true, tabIndex: -1 },
    el('option', { value: 'YAPE', textContent: 'Yape' }),
    el('option', { value: 'TARJETA_DEBITO', textContent: 'Tarjeta de débito' }));
  const metodos = el('div', { className: 'carrito-metodos', role: 'radiogroup', ariaLabel: 'Medio de pago' },
    ...[['YAPE', 'Yape', 'Con tu número de celular'], ['TARJETA_DEBITO', 'Tarjeta de débito', 'Con los datos de tu tarjeta']]
      .map(([valor, titulo, ayuda]) => {
        const boton = el('button', { type: 'button', className: 'carrito-metodo', role: 'radio', ariaChecked: 'false' },
          el('strong', { textContent: titulo }), el('small', { textContent: ayuda }));
        boton.dataset.valor = valor;
        boton.addEventListener('click', () => {
          if (medio.value === valor) return;
          medio.value = valor;
          medio.dispatchEvent(new Event('change'));
        });
        return boton;
      }));
  const marcarMetodo = () => metodos.querySelectorAll('.carrito-metodo')
    .forEach(boton => boton.setAttribute('aria-checked', String(boton.dataset.valor === medio.value)));
  const telefono = el('input', { type: 'tel', inputMode: 'numeric', maxLength: 9,
    pattern: '9[0-9]{8}', required: true, ariaLabel: 'Número de Yape' });
  const codigo = el('input', { type: 'text', inputMode: 'numeric', maxLength: 6,
    pattern: '[0-9]{6}', required: true, disabled: true, ariaLabel: 'Código de aprobación de Yape' });
  const pasoCodigo = el('div', { hidden: true },
    el('label', {}, 'Código de aprobación de seis dígitos', codigo));
  const siguiente = el('button', { type: 'button', textContent: 'Siguiente' });
  const validarYape = el('button', { type: 'button', textContent: 'Validar' });
  pasoCodigo.append(validarYape);
  const yape = el('fieldset', {}, el('legend', { textContent: 'Yape' }),
    el('label', {}, 'Número telefónico peruano', telefono), siguiente, pasoCodigo);

  const numero = el('input', { type: 'text', inputMode: 'numeric', required: true,
    autoComplete: 'cc-number', ariaLabel: 'Número de tarjeta' });
  const titular = el('input', { type: 'text', required: true,
    autoComplete: 'cc-name', ariaLabel: 'Nombre del titular' });
  const vencimiento = el('input', { type: 'month', required: true,
    autoComplete: 'cc-exp', ariaLabel: 'Fecha de vencimiento' });
  const cvv = el('input', { type: 'password', inputMode: 'numeric', minLength: 3,
    maxLength: 4, required: true, autoComplete: 'off', ariaLabel: 'CVV o CVC' });
  const validarTarjeta = el('button', { type: 'button', textContent: 'Validar' });
  const tarjeta = el('fieldset', { hidden: true, disabled: true },
    el('legend', { textContent: 'Tarjeta de débito' }),
    el('label', {}, 'Número de tarjeta', numero),
    el('label', {}, 'Nombre del titular', titular),
    el('label', {}, 'Vencimiento', vencimiento),
    el('label', {}, 'CVV o CVC', cvv), validarTarjeta);
  const terminos = el('input', { type: 'checkbox', disabled: true });
  const confirmar = el('button', { type: 'submit', textContent: 'Confirmar compra', disabled: true });
  const mensajePago = el('p', { role: 'status', ariaLive: 'polite' });
  let pagoValidado = false;

  function actualizarConfirmacion() {
    confirmar.disabled = !pagoValidado || !terminos.checked || noDisponibles;
  }
  function resetPago() {
    marcarMetodo();
    pagoValidado = false;
    terminos.checked = false;
    terminos.disabled = true;
    telefono.readOnly = false;
    codigo.readOnly = false;
    codigo.disabled = true;
    codigo.value = '';
    pasoCodigo.hidden = true;
    siguiente.disabled = false;
    validarYape.disabled = false;
    for (const campo of [numero, titular, vencimiento, cvv]) campo.readOnly = false;
    validarTarjeta.disabled = false;
    yape.hidden = medio.value !== 'YAPE';
    yape.disabled = medio.value !== 'YAPE';
    tarjeta.hidden = medio.value !== 'TARJETA_DEBITO';
    tarjeta.disabled = medio.value !== 'TARJETA_DEBITO';
    mensajePago.textContent = '';
    actualizarConfirmacion();
  }
  medio.addEventListener('change', resetPago);
  siguiente.addEventListener('click', () => {
    if (!/^9[0-9]{8}$/.test(telefono.value.trim())) {
      mensajePago.textContent = 'Ingresa un número peruano de nueve dígitos que empiece por 9.';
      return;
    }
    pasoCodigo.hidden = false;
    codigo.disabled = false;
    codigo.focus();
    mensajePago.textContent = '';
  });
  validarYape.addEventListener('click', () => {
    if (!/^[0-9]{6}$/.test(codigo.value.trim())) {
      mensajePago.textContent = 'Ingresa los seis dígitos del código.';
      return;
    }
    telefono.readOnly = true;
    codigo.readOnly = true;
    siguiente.disabled = true;
    validarYape.disabled = true;
    pagoValidado = true;
    terminos.disabled = false;
    mensajePago.textContent = 'Datos validados. Acepta los términos para continuar.';
    actualizarConfirmacion();
  });
  validarTarjeta.addEventListener('click', () => {
    const digitos = numero.value.replace(/\s/g, '');
    const mesActual = new Date().toISOString().slice(0, 7);
    if (!/^[0-9]{13,19}$/.test(digitos) || !titular.value.trim()
        || !vencimiento.value || vencimiento.value < mesActual
        || !/^[0-9]{3,4}$/.test(cvv.value)) {
      mensajePago.textContent = 'Revisa el número, titular, vencimiento y CVV o CVC.';
      return;
    }
    for (const campo of [numero, titular, vencimiento, cvv]) campo.readOnly = true;
    validarTarjeta.disabled = true;
    pagoValidado = true;
    terminos.disabled = false;
    mensajePago.textContent = 'Datos validados. Acepta los términos para continuar.';
    actualizarConfirmacion();
  });
  terminos.addEventListener('change', actualizarConfirmacion);

  function pedidoActualizado() {
    const guardados = [];
    const vendidos = new Map();
    let suma = 0;
    for (const { pedido, oferta, campos, subtotal } of entradas) {
      let parcial = 0;
      for (const { variante, input } of campos) {
        const unidades = Number(input.value);
        if (Number.isFinite(unidades) && unidades > 0) parcial += unidades * Number(variante.precioDescuento);
        input.closest('.detalle-variante')?.classList.toggle('seleccionada', unidades > 0);
      }
      subtotal.textContent = dinero(parcial);
      const lineas = [];
      for (const { variante, input } of campos) {
        const cantidad = Number(input.value);
        if (!Number.isInteger(cantidad) || cantidad < 0 || cantidad > variante.stockDisponible) {
          mensaje.textContent = 'Revisa las cantidades de los pedidos.';
          return null;
        }
        if (cantidad) {
          const clave = `${oferta.id}-${variante.id}`;
          vendidos.set(clave, (vendidos.get(clave) || 0) + cantidad);
          lineas.push({ varianteId: variante.id, cantidad });
          suma += cantidad * Number(variante.precioDescuento);
        }
      }
      if (lineas.length) guardados.push({ ...pedido, lineas });
    }
    if ([...vendidos].some(([clave, cantidad]) => {
      const [ofertaId, varianteId] = clave.split('-').map(Number);
      return cantidad > ofertas.get(ofertaId).variantes.find(v => v.id === varianteId).stockDisponible;
    })) {
      mensaje.textContent = 'La suma de los pedidos supera el stock disponible.';
      return null;
    }
    if (!guardados.length || noDisponibles) {
      mensaje.textContent = noDisponibles ? 'Quita los pedidos no disponibles para continuar.' : 'Elige al menos una unidad.';
      return null;
    }
    total.textContent = dinero(suma);
    mensaje.textContent = '';
    return guardados;
  }
  formulario.addEventListener('change', evento => {
    if (evento.target.type !== 'number') return;
    const guardados = pedidoActualizado();
    if (guardados) guardarCarrito(usuario.id, guardados);
  });
  formulario.addEventListener('submit', async evento => {
    evento.preventDefault();
    if (!pagoValidado || !terminos.checked) return;
    const guardados = pedidoActualizado();
    if (!guardados) return;
    confirmar.disabled = true;
    try {
      const compras = await solicitar('/compras/lote', { method: 'POST', body: JSON.stringify({
        medioPago: medio.value,
        pedidos: guardados.map(p => ({ ofertaId: p.ofertaId, lineas: p.lineas }))
      }) });
      guardarCarrito(usuario.id, []);
      if (!zona.isConnected) return;
      zona.replaceChildren(el('section', { className: 'compra-confirmada' },
        el('h3', { textContent: 'Compra confirmada' }),
        el('p', { textContent: `Total: ${dinero(compras.reduce((s, c) => s + Number(c.total), 0))}` }),
        ...compras.map((compra, indice) => el('article', {},
          el('h4', { textContent: `Pedido ${indice + 1}: ${compra.establecimiento}` }),
          el('p', { textContent: `Boleta ${compra.numeroBoleta}` }),
          el('p', { textContent: `Código de recojo: ${compra.codigoRecojo}` }),
          el('p', { textContent: `Recojo: ${horarioCompra(compra)}` })))));
      mostrarHistorial();
    } catch (error) {
      mensaje.textContent = error.message;
      actualizarConfirmacion();
    }
  });

  formulario.append(pedidosZona, el('section', { className: 'carrito-pago-panel' },
    el('h3', { textContent: 'Resumen y pago' }),
    el('div', { className: 'carrito-total-fila' }, el('span', { textContent: 'Total de todos los pedidos' }), total),
    el('p', { className: 'carrito-nota', textContent: 'Todos los pedidos se pagan en una sola operación; cada uno genera su propia boleta y su código de recojo.' }),
    el('span', { className: 'pack-dato-etiqueta', textContent: 'MEDIO DE PAGO' }), medio, metodos, yape, tarjeta,
    el('details', {}, el('summary', { textContent: 'Términos y condiciones' }),
      el('p', { textContent: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aceptar para confirmar la compra.' })),
    el('label', { className: 'carrito-terminos' }, terminos, ' Acepto los términos y condiciones'),
    mensajePago, confirmar, mensaje));
  zona.replaceChildren(formulario);
  resetPago();
  pedidoActualizado();
}

async function cancelarCompra(compra) {
  const reembolso = dinero(Number(compra.total) * 0.5);
  if (!await confirmarDialogo('¿Cancelar esta compra?', `Recibirás ${reembolso} de reembolso (50 % del total) y el código de recojo dejará de ser válido.`, 'Sí, cancelar compra')) return;
  try {
    await solicitar(`/compras/${compra.reservaId}/cancelar`, { method: 'POST' });
    await mostrarHistorial();
  } catch (error) { aviso(error.message, true); }
}

async function mostrarHistorial() {
  const lista = document.querySelector('#lista');
  try {
    const compras = await solicitar('/compras/mias');
    if (!lista.isConnected) return;
    lista.replaceChildren(...(compras.length
      ? compras.map(c => el('article', { className: `tarjeta-compra estado-${c.estado.toLowerCase()}` },
        el('p', { className: 'estado-etiqueta', textContent: estados[c.estado] || c.estado }),
        el('h3', { textContent: dinero(c.total) }),
        el('p', { className: 'compra-local', textContent: c.establecimiento }),
        el('p', { textContent: `Medio de pago: ${c.medioPago === 'TARJETA_DEBITO' ? 'Tarjeta de débito' : c.medioPago}` }),
        c.estado === 'CANCELADA' ? null : el('p', { className: 'codigo', textContent: c.codigoRecojo }),
        c.numeroBoleta ? el('p', { textContent: `Boleta ${c.numeroBoleta}` })
          : c.estado === 'PENDIENTE_PAGO' ? el('p', { textContent: 'Boleta disponible al confirmar el efectivo.' }) : null,
        el('p', { className: 'chico', textContent: horarioCompra(c) }),
        c.estado === 'CANCELADA' ? el('p', { textContent: `Reembolso: ${dinero(c.importeReembolso)}` }) : null,
        c.cancelable ? el('button', { type: 'button', textContent: 'Cancelar compra',
          onclick: () => cancelarCompra(c) }) : null,
        c.resenaPendiente ? el('button', { type: 'button', textContent: 'Calificar recojo',
          onclick: () => abrirResena(c, mostrarHistorial) }) : null))
      : [el('div', { className: 'estado-vacio', textContent: 'Aún no tienes compras realizadas.' })]));
    aviso('');
  } catch (error) { if (lista.isConnected) aviso(error.message, true); }
}

/** Contador +/− de una variante dentro de un pedido del carrito. */
function filaVariante(variante, input) {
  const nombre = tamanios[variante.tamanio] || variante.tamanio;
  const cambiar = delta => {
    input.value = Math.max(0, Math.min(variante.stockDisponible, Math.trunc(Number(input.value) || 0) + delta));
    input.dispatchEvent(new Event('change', { bubbles: true }));
  };
  return el('div', { className: 'detalle-variante' },
    el('div', { className: 'detalle-variante-info' },
      el('strong', { textContent: nombre }),
      el('span', {}, el('span', { className: 'detalle-variante-precio', textContent: dinero(variante.precioDescuento) }),
        ' ', el('s', { textContent: dinero(variante.precioReferencial) })),
      el('small', { textContent: `Quedan ${variante.stockDisponible}` })),
    el('div', { className: 'detalle-cantidad' },
      el('button', { type: 'button', className: 'detalle-cantidad-boton detalle-menos', ariaLabel: `Quitar una unidad de ${nombre}`, onclick: () => cambiar(-1) }),
      input,
      el('button', { type: 'button', className: 'detalle-cantidad-boton detalle-mas', ariaLabel: `Añadir una unidad de ${nombre}`, onclick: () => cambiar(1) })));
}

/** Confirmación con el diseño de la plataforma, en lugar de la ventana nativa del navegador. */
function confirmarDialogo(titulo, texto, aceptar) {
  return new Promise(resolver => {
    const dialogo = el('dialog', { className: 'dialogo-confirmar' });
    const cerrar = valor => { dialogo.close(); dialogo.remove(); resolver(valor); };
    dialogo.append(el('h2', { textContent: titulo }), el('p', { textContent: texto }),
      el('div', { className: 'dialogo-acciones' },
        el('button', { type: 'button', className: 'secundario', textContent: 'Volver', onclick: () => cerrar(false) }),
        el('button', { type: 'button', textContent: aceptar, onclick: () => cerrar(true) })));
    dialogo.addEventListener('cancel', evento => { evento.preventDefault(); cerrar(false); });
    document.body.append(dialogo);
    dialogo.showModal();
  });
}
