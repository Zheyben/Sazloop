import { solicitar, el, dinero, fechaHora, tamanios } from './comun.js';

const params = new URLSearchParams(location.search);
const ofertaId = Number(params.get('oferta'));
const token = params.get('token');
const zona = document.querySelector('#ofertaQr');
const form = document.querySelector('#formQr');
const mensaje = document.querySelector('#mensajeQr');
let oferta;
let validado = false;

async function iniciar() {
  try { await solicitar('/sesion'); }
  catch {
    location.replace('acceso-consumidor.html?continuar=' + encodeURIComponent(location.pathname + location.search));
    return;
  }
  if (!Number.isInteger(ofertaId) || ofertaId < 1 || !token) {
    zona.textContent = 'El enlace QR no es válido.';
    return;
  }
  try {
    oferta = await solicitar(`/ofertas/${ofertaId}`);
    zona.replaceChildren(
      el('p', { className: 'pack-local', textContent: `${oferta.establecimiento} · ${oferta.distrito}` }),
      el('h2', { textContent: oferta.pack }),
      oferta.descripcion ? el('p', { className: 'detalle-descripcion', textContent: oferta.descripcion }) : null,
      el('div', { className: 'detalle-posibles' },
        el('span', { className: 'pack-dato-etiqueta', textContent: 'PODRÍA INCLUIR' }),
        el('p', { textContent: oferta.posiblesProductos })),
      el('p', { className: 'pack-horario', textContent: `Recojo: ${fechaHora(oferta.inicioRecojo)} a ${fechaHora(oferta.finRecojo)}` }));
    const variantes = document.querySelector('#variantesQr');
    for (const v of oferta.variantes) {
      const nombre = tamanios[v.tamanio] || v.tamanio;
      const campo = el('input', { type: 'number', name: `v${v.id}`, min: 0, max: v.stockDisponible,
        step: 1, value: 0, disabled: v.stockDisponible === 0, ariaLabel: `Cantidad de ${nombre}` });
      const cambiar = delta => {
        campo.value = Math.max(0, Math.min(v.stockDisponible, Math.trunc(Number(campo.value) || 0) + delta));
        campo.dispatchEvent(new Event('input', { bubbles: true }));
      };
      variantes.append(el('div', { className: 'detalle-variante' },
        el('div', { className: 'detalle-variante-info' },
          el('strong', { textContent: nombre }),
          el('span', {}, el('span', { className: 'detalle-variante-precio', textContent: dinero(v.precioDescuento) }),
            ' ', el('s', { textContent: dinero(v.precioReferencial) })),
          el('small', { textContent: `${v.stockDisponible} disponibles` })),
        el('div', { className: 'detalle-cantidad' },
          el('button', { type: 'button', className: 'detalle-cantidad-boton detalle-menos', ariaLabel: `Quitar una unidad de ${nombre}`,
            disabled: v.stockDisponible === 0, onclick: () => cambiar(-1) }),
          campo,
          el('button', { type: 'button', className: 'detalle-cantidad-boton detalle-mas', ariaLabel: `Añadir una unidad de ${nombre}`,
            disabled: v.stockDisponible === 0, onclick: () => cambiar(1) }))));
    }
    form.hidden = false;
    actualizarPago();
    actualizarTotal();
  } catch (error) { zona.textContent = error.message; }
}

function lineas() {
  return oferta.variantes.map(v => ({ varianteId: v.id,
    cantidad: Number(form.elements[`v${v.id}`].value), variante: v }));
}

function actualizarTotal() {
  document.querySelector('#totalQr').textContent = dinero(lineas().reduce((s, l) =>
    s + l.cantidad * Number(l.variante.precioDescuento), 0));
  for (const fila of document.querySelectorAll('#variantesQr .detalle-variante'))
    fila.classList.toggle('seleccionada', Number(fila.querySelector('input').value) > 0);
}

function actualizarPago() {
  const medio = form.elements.medioPago.value;
  validado = medio === 'EFECTIVO';
  for (const boton of document.querySelectorAll('#metodosQr .carrito-metodo'))
    boton.setAttribute('aria-checked', String(boton.dataset.valor === medio));
  form.elements.terminos.checked = false;
  document.querySelector('#confirmarQr').disabled = true;
  for (const [id, activo] of [['yapeQr', medio === 'YAPE'], ['tarjetaQr', medio === 'TARJETA_DEBITO']]) {
    const grupo = document.querySelector('#' + id);
    grupo.hidden = !activo;
    grupo.disabled = !activo;
  }
  document.querySelector('#ayudaEfectivoQr').hidden = medio !== 'EFECTIVO';
  document.querySelector('#codigoYapeQr').hidden = true;
  form.elements.codigoYape.readOnly = false;
  for (const nombre of ['numeroTarjeta', 'titular', 'vencimiento', 'cvv']) form.elements[nombre].readOnly = false;
  mensaje.textContent = '';
}

form.addEventListener('input', evento => {
  if (evento.target.name?.startsWith('v')) actualizarTotal();
});
form.elements.medioPago.addEventListener('change', actualizarPago);
for (const boton of document.querySelectorAll('#metodosQr .carrito-metodo')) {
  boton.addEventListener('click', () => {
    if (form.elements.medioPago.value === boton.dataset.valor) return;
    form.elements.medioPago.value = boton.dataset.valor;
    form.elements.medioPago.dispatchEvent(new Event('change'));
  });
}
form.elements.terminos.addEventListener('change', () => {
  document.querySelector('#confirmarQr').disabled = !validado || !form.elements.terminos.checked;
});
document.querySelector('#siguienteYapeQr').addEventListener('click', () => {
  if (!/^9[0-9]{8}$/.test(form.elements.telefonoYape.value.trim())) {
    mensaje.textContent = 'Ingresa un número peruano que empiece por 9.';
    return;
  }
  document.querySelector('#codigoYapeQr').hidden = false;
  form.elements.codigoYape.focus();
  mensaje.textContent = '';
});
document.querySelector('#validarYapeQr').addEventListener('click', () => {
  if (!/^[0-9]{6}$/.test(form.elements.codigoYape.value.trim())) {
    mensaje.textContent = 'Ingresa seis dígitos.';
    return;
  }
  validado = true;
  form.elements.telefonoYape.readOnly = true;
  form.elements.codigoYape.readOnly = true;
  document.querySelector('#confirmarQr').disabled = !form.elements.terminos.checked;
  mensaje.textContent = 'Yape validado.';
});
document.querySelector('#validarTarjetaQr').addEventListener('click', () => {
  const numero = form.elements.numeroTarjeta.value.replace(/\s/g, '');
  const titular = form.elements.titular.value.trim();
  const vencimiento = form.elements.vencimiento.value;
  const cvv = form.elements.cvv.value.trim();
  const ahora = new Date();
  const mesActual = `${ahora.getFullYear()}-${String(ahora.getMonth() + 1).padStart(2, '0')}`;
  if (!/^[0-9]{13,19}$/.test(numero) || !titular || vencimiento <= mesActual || !/^[0-9]{3,4}$/.test(cvv)) {
    mensaje.textContent = 'Revisa el número, titular, vencimiento futuro y CVV.';
    return;
  }
  validado = true;
  for (const nombre of ['numeroTarjeta', 'titular', 'vencimiento', 'cvv']) form.elements[nombre].readOnly = true;
  document.querySelector('#confirmarQr').disabled = !form.elements.terminos.checked;
  mensaje.textContent = 'Tarjeta validada.';
});
form.addEventListener('submit', async evento => {
  evento.preventDefault();
  const seleccion = lineas();
  if (!validado || !form.elements.terminos.checked ||
      !seleccion.some(l => l.cantidad > 0) ||
      seleccion.some(l => !Number.isInteger(l.cantidad) || l.cantidad < 0 || l.cantidad > l.variante.stockDisponible)) {
    mensaje.textContent = 'Revisa el pago y las cantidades.';
    return;
  }
  const medioPago = form.elements.medioPago.value;
  const boton = document.querySelector('#confirmarQr');
  boton.disabled = true;
  try {
    const respuesta = await solicitar(medioPago === 'EFECTIVO'
      ? '/compras/presenciales/efectivo' : '/compras/presenciales', {
      method: 'POST', body: JSON.stringify({ token, compra: {
        ofertaId, medioPago,
        lineas: seleccion.filter(l => l.cantidad > 0).map(l => ({ varianteId: l.varianteId, cantidad: l.cantidad }))
      } })
    });
    form.hidden = true;
    const resultadoQr = document.querySelector('#resultadoQr');
    resultadoQr.className = 'compra-confirmada';
    resultadoQr.replaceChildren(
      el('h2', { textContent: medioPago === 'EFECTIVO' ? 'Entrega el efectivo al trabajador' : 'Compra confirmada' }),
      el('p', { textContent: `Código: ${respuesta.codigoRecojo}` }),
      el('p', { textContent: `Total: ${dinero(respuesta.total)}` }),
      medioPago === 'EFECTIVO' ? el('p', { textContent: `La reserva vence: ${fechaHora(respuesta.venceEn)}` })
        : el('p', { textContent: `Boleta: ${respuesta.numeroBoleta}` }),
      el('a', { href: 'compras.html', textContent: 'Ver mis compras' }));
  } catch (error) {
    mensaje.textContent = error.message;
    boton.disabled = false;
  }
});

iniciar();
