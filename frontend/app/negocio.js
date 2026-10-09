import { solicitar, el, dinero, fechaHora, tamanios } from './comun.js';
import qrcode from './vendor/qrcode.mjs';

const empresa = document.querySelector('#panelEmpresa');
const trabajador = document.querySelector('#panelTrabajador');
const cuenta = document.querySelector('#cuentaLocal');
const resultado = document.querySelector('#resultadoRecojo');
let codigoConsultado = null;
const formPublicacion = document.querySelector('#formPublicacion');
const selectorPack = formPublicacion.elements.packId;

const vistasLocal = { vigente: '#vistaVigente', crear: '#vistaCrear', validar: '#vistaValidar' };
for (const boton of document.querySelectorAll('[data-vista-local]')) {
  boton.addEventListener('click', () => mostrarVistaLocal(boton.dataset.vistaLocal));
}
function mostrarVistaLocal(vista) {
  for (const [nombre, selector] of Object.entries(vistasLocal)) {
    document.querySelector(selector).hidden = nombre !== vista;
  }
  for (const boton of document.querySelectorAll('[data-vista-local]')) {
    if (boton.dataset.vistaLocal === vista) boton.setAttribute('aria-current', 'page');
    else boton.removeAttribute('aria-current');
  }
}

async function cargarResumenLocal() {
  const resumen = await solicitar('/local/resumen');
  document.querySelector('#datosLocal').textContent = resumen.establecimiento;
  const actual = document.querySelector('#packVigente');
  const oferta = resumen.ofertaVigente;
  if (!oferta) actual.replaceChildren(el('p', { textContent: 'No hay una oferta vigente.' }));
  else actual.replaceChildren(
    el('h4', { textContent: oferta.pack }),
    el('p', { textContent: oferta.descripcion }),
    el('p', { textContent: `Podría incluir: ${oferta.posiblesProductos}` }),
    el('p', { textContent: `Alérgenos: ${oferta.alergenos.join(', ') || 'Ninguno declarado'}` }),
    el('p', { textContent: `Restricciones: ${oferta.restricciones.join(', ')}` }),
    el('p', { textContent: `Recojo: ${fechaHora(oferta.inicioRecojo)} a ${fechaHora(oferta.finRecojo)}` }),
    el('ul', {}, ...oferta.variantes.map(variante =>
      el('li', { textContent: `${tamanios[variante.tamanio]} · ${dinero(variante.precioDescuento)} · ${variante.stockDisponible} uds.` }))),
    qrOferta(oferta.id, resumen.tokenQr)
  );
  document.querySelector('#stockVigente').textContent = `Stock disponible: ${resumen.stockDisponible} unidades`;
  document.querySelector('#resumenPeriodos').replaceChildren(
    ...[['Hoy', resumen.dia], ['Semana', resumen.semana], ['Mes', resumen.mes]].map(([nombre, periodo]) =>
      el('tr', {}, el('th', { textContent: nombre }),
        el('td', { textContent: dinero(periodo.ganancia) }),
        el('td', { textContent: String(periodo.unidadesNoVendidas) })))
  );
}


function qrOferta(ofertaId, token) {
  const destino = new URL('qr.html', location.href);
  destino.searchParams.set('oferta', ofertaId);
  destino.searchParams.set('token', token);
  const qr = qrcode(0, 'M');
  qr.addData(destino.href);
  qr.make();
  return el('section', {},
    el('h4', { textContent: 'QR de esta oferta' }),
    el('img', { src: qr.createDataURL(5, 4), alt: 'Código QR de la oferta', width: 260, height: 260 }),
    el('p', {}, el('a', { href: destino.href, textContent: 'Abrir enlace de la oferta presencial' })),
    el('p', { textContent: 'Imprime este QR para mostrarlo en el establecimiento.' }));
}

const pasos = ['#pasoPack', '#pasoVariantes', '#pasoHorario'];
let pasoActual = 0;
function estado(zona, mensaje, esError = false) {
  zona.textContent = mensaje;
  zona.className = esError ? 'error' : 'ok';
}
function mostrarPaso(indice) {
  pasoActual = indice;
  pasos.forEach((selector, numero) => { document.querySelector(selector).hidden = numero !== indice; });
  document.querySelector('#progresoPublicacion').textContent =
    `Paso ${indice + 1} de 3: ${['pack', 'tamaños y precios', 'horario'][indice]}`;
  estado(document.querySelector('#resultadoPublicacion'), '');
}
function pasoValido(selector) {
  for (const campo of document.querySelectorAll(`${selector} input, ${selector} textarea, ${selector} select`)) {
    if (!campo.disabled && !campo.checkValidity()) { campo.reportValidity(); return false; }
  }
  return true;
}
function tipoPack() {
  const existente = formPublicacion.elements.tipoPack.value === 'existente';
  document.querySelector('#datosPackNuevo').hidden = existente;
  document.querySelector('#datosPackNuevo').disabled = existente;
  document.querySelector('#datosPackExistente').hidden = !existente;
  document.querySelector('#datosPackExistente').disabled = !existente;
}
for (const opcion of formPublicacion.querySelectorAll('[name="tipoPack"]')) {
  opcion.addEventListener('change', tipoPack);
}
document.querySelector('#agregarAlergeno').addEventListener('click', () => {
  const campo = document.querySelector('#otroAlergenoCampo');
  campo.hidden = !campo.hidden;
  campo.querySelector('input').required = !campo.hidden;
  if (campo.hidden) campo.querySelector('input').value = '';
  else campo.querySelector('input').focus();
});
for (const grupo of formPublicacion.querySelectorAll('.variante-oferta')) {
  const incluir = grupo.querySelector('[name="activo"]');
  incluir.addEventListener('change', () => {
    for (const campo of grupo.querySelectorAll('input:not([name="activo"])')) {
      campo.disabled = !incluir.checked;
      campo.required = incluir.checked;
    }
  });
}
document.querySelector('#siguientePack').addEventListener('click', () => {
  if (pasoValido('#pasoPack')) mostrarPaso(1);
});
document.querySelector('#siguienteVariantes').addEventListener('click', () => {
  const seleccionadas = [...formPublicacion.querySelectorAll('.variante-oferta')]
    .filter(grupo => grupo.querySelector('[name="activo"]').checked);
  const salida = document.querySelector('#resultadoPublicacion');
  if (!seleccionadas.length) return estado(salida, 'Selecciona al menos un tamaño.', true);
  if (!pasoValido('#pasoVariantes')) return;
  if (seleccionadas.some(grupo =>
    Number(grupo.querySelector('[name="precioDescuento"]').value) >=
    Number(grupo.querySelector('[name="precioReferencial"]').value))) {
    return estado(salida, 'El precio con descuento debe ser menor que el referencial.', true);
  }
  mostrarPaso(2);
});
for (const boton of document.querySelectorAll('[data-paso-anterior]')) {
  boton.addEventListener('click', () => mostrarPaso(Number(boton.dataset.pasoAnterior)));
}
formPublicacion.addEventListener('submit', async evento => {
  evento.preventDefault();
  const salida = document.querySelector('#resultadoPublicacion');
  if (!pasoValido('#pasoHorario')) return;
  const datos = new FormData(formPublicacion);
  if (!(new Date(datos.get('inicioRecojo')) > new Date() &&
        new Date(datos.get('finRecojo')) > new Date(datos.get('inicioRecojo')))) {
    return estado(salida, 'Revisa el horario de recojo.', true);
  }
  const variantes = [...formPublicacion.querySelectorAll('.variante-oferta')]
    .filter(grupo => grupo.querySelector('[name="activo"]').checked)
    .map(grupo => ({
      tamanio: grupo.dataset.tamanio,
      precioReferencial: grupo.querySelector('[name="precioReferencial"]').value,
      precioDescuento: grupo.querySelector('[name="precioDescuento"]').value,
      stock: Number(grupo.querySelector('[name="stock"]').value)
    }));
  const oferta = { inicioRecojo: datos.get('inicioRecojo'), finRecojo: datos.get('finRecojo'), variantes };
  const nuevo = datos.get('tipoPack') === 'nuevo';
  if (nuevo) {
    const otro = (datos.get('otroAlergeno') || '').trim();
    const alergenos = [...new Set([...datos.getAll('alergeno'), ...(otro ? [otro] : [])])];
    oferta.pack = {
      nombre: datos.get('nombre').trim(),
      descripcion: datos.get('descripcion').trim(),
      posiblesProductos: datos.get('posiblesProductos').trim(),
      alergenos,
      restricciones: [
        datos.has('aptoVegetarianos') ? 'Apto para vegetarianos' : 'No apto para vegetarianos',
        datos.has('aptoVeganos') ? 'Apto para veganos' : 'No apto para veganos',
        datos.has('aptoCeliacos') ? 'Apto para personas con celiaquía' : 'No apto para personas con celiaquía'
      ]
    };
  } else oferta.packId = Number(datos.get('packId'));
  const boton = formPublicacion.querySelector('button[type="submit"]');
  boton.disabled = true;
  estado(salida, '');
  try {
    await solicitar(nuevo ? '/local/publicaciones' : '/local/ofertas',
      { method: 'POST', body: JSON.stringify(oferta) });
    formPublicacion.reset();
    document.querySelector('#otroAlergenoCampo').hidden = true;
    document.querySelector('#otroAlergenoCampo input').required = false;
    for (const grupo of formPublicacion.querySelectorAll('.variante-oferta')) {
      grupo.querySelector('[name="activo"]').dispatchEvent(new Event('change'));
    }
    tipoPack();
    await cargarPacks();
    await cargarResumenLocal();
    mostrarPaso(0);
    mostrarVistaLocal('vigente');
    estado(document.querySelector('#mensajeLocal'), 'Oferta publicada.');
  } catch (error) {
    estado(salida, error.status === 409
      ? 'Este local ya tiene una oferta activa. Espera a que termine antes de publicar otra.'
      : error.message, true);
  } finally { boton.disabled = false; }
});

function modo(esTrabajador) {
  document.body.classList.toggle('vista-trabajador', esTrabajador);
  empresa.hidden = esTrabajador;
  trabajador.hidden = !esTrabajador;
}

async function cargarPacks() {
  const packs = await solicitar('/local/packs');
  selectorPack.replaceChildren(...packs.map(pack =>
    el('option', { value: String(pack.id), textContent: pack.nombre })));
  document.querySelector('#usarPackExistente').disabled = packs.length === 0;
  if (!packs.length && formPublicacion.elements.tipoPack.value === 'existente') {
    formPublicacion.querySelector('[value="nuevo"]').checked = true;
    tipoPack();
  }
}

function mostrarLocal(datos) {
  cuenta.hidden = !datos;
  document.querySelector('#datosLocal').textContent = datos ? datos.nombreUsuario : '';
  if (!datos) { codigoConsultado = null; resultado.replaceChildren(); }
}

document.querySelector('#salirLocal').addEventListener('click', async () => {
  try {
    await solicitar('/local/sesion', { method: 'DELETE' });
    location.href = 'acceso-locales.html#trabajador';
  } catch (error) { resultado.textContent = error.message; }
});

document.querySelector('#consultarRecojo').addEventListener('submit', async evento => {
  evento.preventDefault();
  const codigo = new FormData(evento.currentTarget).get('codigo').trim();
  codigoConsultado = null;
  resultado.replaceChildren();
  try {
    const compra = await solicitar('/local/recojos/' + encodeURIComponent(codigo));
    codigoConsultado = codigo;
    resultado.append(
      el('h4', { textContent: 'Compra encontrada' }),
      el('p', { textContent: `Estado: ${compra.estado} · Total: ${dinero(compra.total)}` }),
      el('p', { textContent: `Recojo: ${fechaHora(compra.inicioRecojo)} a ${fechaHora(compra.finRecojo)}` }),
      ...(compra.estado === 'CONFIRMADA'
        ? [el('button', { type: 'button', textContent: 'Confirmar recojo', onclick: confirmarRecojo })]
        : compra.estado === 'PENDIENTE_PAGO' && compra.medioPago === 'EFECTIVO'
          ? [el('button', { type: 'button', textContent: 'Confirmar efectivo y entrega', onclick: confirmarEfectivo })]
          : [el('p', { textContent: 'Esta compra no tiene un recojo pendiente.' })])
    );
  } catch (error) { resultado.textContent = error.message; }
});

async function confirmarEfectivo() {
  if (!codigoConsultado) return;
  const boton = resultado.querySelector('button');
  boton.disabled = true;
  try {
    await solicitar('/local/efectivo/' + encodeURIComponent(codigoConsultado) + '/confirmar', { method: 'POST' });
    codigoConsultado = null;
    resultado.textContent = 'Efectivo y entrega confirmados.';
    await cargarResumenLocal();
  } catch (error) {
    estado(resultado, error.message, true);
  } finally { boton.disabled = false; }
}

async function confirmarRecojo() {
  if (!codigoConsultado) return;
  const boton = resultado.querySelector('button');
  boton.disabled = true;
  try {
    await solicitar('/local/recojos/' + encodeURIComponent(codigoConsultado) + '/confirmar', { method: 'POST' });
    codigoConsultado = null;
    resultado.textContent = 'Recojo confirmado.';
    await cargarResumenLocal();
  } catch (error) {
    resultado.textContent = error.message;
  }
}

const esTrabajador = location.hash === '#trabajador';
modo(esTrabajador);
if (esTrabajador) {
  try {
    const datos = await solicitar('/local/sesion');
    mostrarLocal(datos);
    await cargarPacks();
    await cargarResumenLocal();
  } catch {
    mostrarLocal(null);
    location.replace('acceso-locales.html#trabajador');
  }
} else {
  try {
    await solicitar('/sesion');
  } catch {
    location.replace('acceso-locales.html#empresa');
  }
}
