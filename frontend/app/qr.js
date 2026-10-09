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
      el('h2', { textContent: `${oferta.pack} · ${oferta.establecimiento}` }),
      el('p', { textContent: oferta.descripcion }),
      el('p', { textContent: `Podría incluir: ${oferta.posiblesProductos}` }),
      el('p', { textContent: `Recojo: ${fechaHora(oferta.inicioRecojo)} a ${fechaHora(oferta.finRecojo)}` }));
    const variantes = document.querySelector('#variantesQr');
    for (const v of oferta.variantes) {
      variantes.append(el('label', {},
        `${tamanios[v.tamanio] || v.tamanio} · ${dinero(v.precioDescuento)} · ${v.stockDisponible} disponibles`,
        el('input', { type: 'number', name: `v${v.id}`, min: 0, max: v.stockDisponible,
          step: 1, value: 0, disabled: v.stockDisponible === 0 })));
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
  document.querySelector('#totalQr').textContent = `Total: ${dinero(lineas().reduce((s, l) =>
    s + l.cantidad * Number(l.variante.precioDescuento), 0))}`;
}

function actualizarPago() {
  const medio = form.elements.medioPago.value;
  validado = medio === 'EFECTIVO';
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
    document.querySelector('#resultadoQr').replaceChildren(
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
