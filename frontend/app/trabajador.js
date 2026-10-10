import { solicitar, el, dinero, tamanios } from './comun.js';
import {
  pintarIconos, crearNavegacion, mensaje, estadoEn, insignia, banda, vacio, cifra, tabla,
  rangoHorario, hora, fecha, filasDieta
} from './panel.js';
import qrcode from './vendor/qrcode.mjs';

const ALERGENOS = ['Trigo (gluten)', 'Huevo', 'Leche', 'Almendras', 'Soya', 'Maní', 'Pescado', 'Mariscos'];
const MEDIOS = { YAPE: 'Yape', TARJETA_DEBITO: 'Tarjeta de débito', EFECTIVO: 'Efectivo' };
const ESTADOS_COMPRA = {
  CONFIRMADA: ['Pagado · listo para entregar', 'ok'],
  PENDIENTE_PAGO: ['Efectivo pendiente de cobro', 'alerta'],
  RECOGIDA: ['Entregado', 'info'],
  CANCELADA: ['Cancelado por el cliente', 'peligro'],
  EXPIRADA: ['No recogido · cerrado', 'peligro']
};

const form = document.querySelector('#form-pack');
const pasos = [...document.querySelectorAll('.paso-formulario')];
let pasoActual = 0;
let resumen = null;
let packs = [];

pintarIconos();
const mostrarVista = crearNavegacion(vista => {
  if (vista === 'validar') document.querySelector('#form-codigo input').focus();
  if (vista === 'vigente' && resumen) cargar();
});

try {
  await solicitar('/local/sesion');
} catch {
  location.replace('acceso-locales.html#trabajador');
  throw new Error('Sin sesión del local');
}

document.querySelector('#salir').addEventListener('click', async () => {
  try { await solicitar('/local/sesion', { method: 'DELETE' }); } catch { /* la sesión ya no existía */ }
  location.href = 'acceso-locales.html#trabajador';
});

prepararFormulario();

async function cargar() {
  try {
    [resumen, packs] = await Promise.all([solicitar('/local/resumen'), solicitar('/local/packs')]);
  } catch (error) {
    mensaje(error.message, true);
    return;
  }
  document.querySelector('#titulo-panel').textContent = resumen.establecimiento;
  document.title = `Sazloop | ${resumen.establecimiento}`;
  pintarSuspension();
  pintarVigente();
  pintarDisponibilidadPublicacion();
  const selector = form.elements.packId;
  selector.replaceChildren(...packs.map(p => el('option', { value: String(p.id), textContent: p.nombre })));
  form.querySelector('[name="origen"][value="existente"]').disabled = !packs.length;
}

const suspendido = () => resumen?.estadoLocal === 'SUSPENDIDO';

function pintarSuspension() {
  document.querySelector('#aviso-suspension').replaceChildren(...(suspendido() ? [banda('peligro',
    'Local suspendido por documentos vencidos',
    `${resumen.motivoSuspension || 'Un documento obligatorio venció.'} Puedes validar códigos y entregar las reservas ya pagadas, `
    + 'pero no publicar packs hasta que tu empresa renueve los documentos y Sazloop apruebe la renovación.')] : []));
}

// ---------- Pack sorpresa vigente ----------
function pintarVigente() {
  const oferta = resumen.ofertaVigente;
  const porTamanio = oferta ? oferta.variantes.map(v => `${tamanios[v.tamanio]}: ${v.stockDisponible}`).join(' · ') : 'Sin pack publicado';
  document.querySelector('#cifras-stock').replaceChildren(
    cifra('Stock disponible', `${resumen.stockDisponible} ${resumen.stockDisponible === 1 ? 'unidad' : 'unidades'}`, porTamanio, true),
    cifra('Ganancia neta de hoy', dinero(resumen.dia.ganancia), `${resumen.dia.packsEntregados} ${resumen.dia.packsEntregados === 1 ? 'pack entregado' : 'packs entregados'}`),
    cifra('Sin vender hoy', String(resumen.dia.unidadesNoVendidas), 'Ofertas cerradas hoy'));
  document.querySelector('#periodos').replaceChildren(...[
    ['Hoy', resumen.dia], ['Esta semana', resumen.semana], ['Este mes', resumen.mes]
  ].map(([nombre, periodo]) => el('article', { className: 'periodo' },
    el('h4', { textContent: nombre }),
    el('dl', {},
      fila('Venta de lo entregado', dinero(periodo.ventaBruta)),
      fila('Comisión de Sazloop (10 %)', '− ' + dinero(periodo.comision)),
      fila('Ganancia neta', dinero(periodo.ganancia), true),
      fila('Packs entregados', String(periodo.packsEntregados)),
      fila('Unidades sin vender', String(periodo.unidadesNoVendidas))))));

  const zona = document.querySelector('#pack-vigente');
  if (!oferta) {
    zona.replaceChildren(vacio('No hay un pack sorpresa vigente',
      suspendido() ? 'El local está suspendido; no puede publicar packs por ahora.'
        : 'Cuando publiques un pack, aquí verás su información, sus precios y su código QR.',
      suspendido() ? null : el('button', { type: 'button', textContent: 'Crear nuevo pack', onclick: () => mostrarVista('crear') })));
    return;
  }
  zona.replaceChildren(el('article', { className: 'tarjeta-panel pack-lectura' },
    el('header', { className: 'vista-encabezado', style: 'margin:0' },
      el('div', {}, el('span', { className: 'dato-titulo', textContent: 'Pack publicado' }),
        el('h3', { textContent: oferta.pack }),
        el('p', { className: 'descripcion', textContent: oferta.descripcion })),
      suspendido() ? insignia('Oculto mientras el local esté suspendido', 'peligro') : insignia('Visible para los consumidores', 'ok')),
    el('div', {}, el('span', { className: 'dato-titulo', textContent: 'Podría incluir' }),
      el('p', { className: 'posibles', textContent: oferta.posiblesProductos })),
    el('div', { className: 'columnas-pack' },
      el('div', {}, el('span', { className: 'dato-titulo', textContent: 'Horario de recojo' }),
        el('p', { style: 'margin:0', textContent: rangoHorario(oferta.inicioRecojo, oferta.finRecojo) })),
      el('div', {}, el('span', { className: 'dato-titulo', textContent: 'Alérgenos' }),
        oferta.alergenos.length ? el('div', { className: 'chips' }, ...oferta.alergenos.map(a => el('span', { className: 'chip', textContent: a })))
          : el('p', { style: 'margin:0', className: 'nota', textContent: 'Sin alérgenos declarados' })),
      el('div', {}, el('span', { className: 'dato-titulo', textContent: 'Dieta' }), filasDieta(oferta.restricciones))),
    el('div', {}, el('span', { className: 'dato-titulo', textContent: 'Tamaños, precios y stock' }),
      tabla([['Tamaño'], ['Precio regular', 'numero'], ['Con descuento', 'numero'], ['Disponibles', 'numero']],
        oferta.variantes.map(v => [tamanios[v.tamanio], dinero(v.precioReferencial), dinero(v.precioDescuento), String(v.stockDisponible)]))),
    qrSeguro(oferta.id, resumen.tokenQr)));
}

const fila = (titulo, valor, destacada = false) => el('div', { className: destacada ? 'fila-neta' : '' },
  el('dt', { textContent: titulo }), el('dd', { textContent: valor }));

/** Un fallo al dibujar el QR no debe ocultar la información del pack. */
function qrSeguro(ofertaId, token) {
  try { return qrOferta(ofertaId, token); }
  catch (error) {
    console.error('[Sazloop] No se pudo generar el QR', error);
    return el('p', { className: 'nota', textContent: 'No se pudo generar el código QR de esta oferta.' });
  }
}

function qrOferta(ofertaId, token) {
  const destino = new URL('qr.html', location.href);
  destino.searchParams.set('oferta', ofertaId);
  destino.searchParams.set('token', token);
  const qr = qrcode(0, 'M');
  qr.addData(destino.href);
  qr.make();
  return el('div', { className: 'qr-oferta' },
    el('img', { src: qr.createDataURL(5, 4), alt: 'Código QR para comprar este pack en el local', width: 168, height: 168 }),
    el('div', {},
      el('span', { className: 'dato-titulo', textContent: 'Compra presencial' }),
      el('p', { textContent: 'Imprime este QR y colócalo en el local. Al escanearlo, el cliente puede pagar también en efectivo.' }),
      el('a', { href: destino.href, target: '_blank', rel: 'noopener', textContent: 'Abrir la página del QR' })));
}

// ---------- Crear nuevo pack ----------
function prepararFormulario() {
  construirCampos();
  for (const opcion of form.querySelectorAll('[name="origen"]')) opcion.addEventListener('change', alternarOrigen);
  document.querySelector('#paso-siguiente').addEventListener('click', () => { if (validarPaso(pasoActual)) irAPaso(pasoActual + 1); });
  document.querySelector('#paso-anterior').addEventListener('click', () => irAPaso(pasoActual - 1));
  form.addEventListener('submit', publicar);
}

/** Alérgenos, tamaños y horario sugerido; se reconstruyen después de cada publicación. */
function construirCampos() {
  document.querySelector('#alergenos').replaceChildren(...ALERGENOS.map(nombre =>
    el('label', { className: 'chip-opcion' }, el('input', { type: 'checkbox', name: 'alergeno', value: nombre }), el('span', { textContent: nombre }))));
  document.querySelector('#variantes').replaceChildren(...['PEQUENO', 'MEDIANO', 'GRANDE'].map((tamanio, i) => {
    const incluir = el('input', { type: 'checkbox', name: `incluir-${tamanio}`, checked: i === 0 });
    const campos = ['precioReferencial', 'precioDescuento', 'stock'].map(nombre => el('input', {
      type: 'number', name: `${nombre}-${tamanio}`, min: nombre === 'stock' ? 1 : 0.01, step: nombre === 'stock' ? 1 : 0.01,
      required: i === 0, disabled: i !== 0, inputMode: nombre === 'stock' ? 'numeric' : 'decimal' }));
    const descuento = el('p', { className: 'descuento', ariaLive: 'polite' });
    const actualizar = () => {
      campos.forEach(c => { c.disabled = !incluir.checked; c.required = incluir.checked; });
      const [ref, desc] = campos.map(c => Number(c.value));
      descuento.textContent = incluir.checked && ref > 0 && desc > 0 && desc < ref ? `${Math.round((1 - desc / ref) * 100)} % de descuento` : '';
    };
    incluir.addEventListener('change', actualizar);
    campos.forEach(c => c.addEventListener('input', actualizar));
    return el('fieldset', { className: 'variante' }, el('legend', { textContent: tamanios[tamanio] }),
      el('label', { className: 'incluir' }, incluir, 'Incluir este tamaño'),
      el('label', {}, 'Precio regular (S/)', campos[0]),
      el('label', {}, 'Precio con descuento (S/)', campos[1]),
      el('label', {}, 'Unidades disponibles', campos[2]), descuento);
  }));
  horarioSugerido();
}

function horarioSugerido() {
  const inicio = new Date();
  inicio.setSeconds(0, 0);
  const fin = new Date(inicio.getTime() + 3 * 3600000);
  const local = d => new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 16);
  form.elements.inicioRecojo.value = local(inicio);
  form.elements.finRecojo.value = local(fin);
  form.elements.inicioRecojo.removeAttribute('min');
}

function alternarOrigen() {
  const existente = form.elements.origen.value === 'existente';
  document.querySelector('#pack-existente').hidden = !existente;
  document.querySelector('#pack-nuevo').hidden = existente;
  for (const campo of document.querySelectorAll('#pack-nuevo [required]')) campo.disabled = existente;
  form.elements.packId.required = existente;
}

function irAPaso(indice) {
  pasoActual = Math.max(0, Math.min(2, indice));
  pasos.forEach((paso, i) => { paso.hidden = i !== pasoActual; });
  document.querySelectorAll('.pasos li').forEach((li, i) => {
    if (i === pasoActual) li.setAttribute('aria-current', 'step'); else li.removeAttribute('aria-current');
    li.classList.toggle('completo', i < pasoActual);
  });
  document.querySelector('#paso-anterior').hidden = pasoActual === 0;
  document.querySelector('#paso-siguiente').hidden = pasoActual === 2;
  document.querySelector('#publicar').hidden = pasoActual !== 2;
  estadoEn(document.querySelector('#estado-pack'), '');
  if (pasoActual === 2) pintarRevision();
}

function variantesElegidas() {
  return ['PEQUENO', 'MEDIANO', 'GRANDE'].filter(t => form.elements[`incluir-${t}`].checked).map(t => ({
    tamanio: t,
    precioReferencial: form.elements[`precioReferencial-${t}`].value,
    precioDescuento: form.elements[`precioDescuento-${t}`].value,
    stock: Number(form.elements[`stock-${t}`].value)
  }));
}

function validarPaso(indice) {
  const estado = document.querySelector('#estado-pack');
  for (const campo of pasos[indice].querySelectorAll('input, textarea, select')) {
    if (!campo.disabled && !campo.checkValidity()) {
      if (indice !== pasoActual) irAPaso(indice);
      campo.reportValidity();
      return false;
    }
  }
  if (indice === 1) {
    const elegidas = variantesElegidas();
    if (!elegidas.length) { estadoEn(estado, 'Incluye al menos un tamaño.', true); return false; }
    if (elegidas.some(v => Number(v.precioDescuento) >= Number(v.precioReferencial))) {
      estadoEn(estado, 'El precio con descuento debe ser menor que el precio regular.', true); return false;
    }
  }
  if (indice === 2) {
    const inicio = new Date(form.elements.inicioRecojo.value);
    const fin = new Date(form.elements.finRecojo.value);
    if (inicio < new Date(Date.now() - 5 * 60000)) { estadoEn(estado, 'El inicio del recojo no puede estar en el pasado.', true); return false; }
    if (!(fin > inicio)) { estadoEn(estado, 'El fin del recojo debe ser posterior al inicio.', true); return false; }
  }
  return true;
}

function pintarRevision() {
  const nuevo = form.elements.origen.value === 'nuevo';
  const nombre = nuevo ? form.elements.nombre.value.trim() : form.elements.packId.selectedOptions[0]?.textContent;
  const inicio = form.elements.inicioRecojo.value;
  const fin = form.elements.finRecojo.value;
  document.querySelector('#resumen-publicacion').replaceChildren(
    el('span', { className: 'dato-titulo', textContent: 'Así se publicará' }),
    el('h3', { style: 'margin:0 0 .6rem', textContent: nombre || 'Pack sin nombre' }),
    tabla([['Tamaño'], ['Precio regular', 'numero'], ['Con descuento', 'numero'], ['Unidades', 'numero']],
      variantesElegidas().map(v => [tamanios[v.tamanio], dinero(v.precioReferencial), dinero(v.precioDescuento), String(v.stock)])),
    el('p', { className: 'nota', style: 'margin:.8rem 0 0', textContent: inicio && fin && new Date(fin) > new Date(inicio)
      ? `Recojo: ${rangoHorario(inicio, fin)}` : 'Indica el horario de recojo.' }));
  for (const campo of [form.elements.inicioRecojo, form.elements.finRecojo]) campo.oninput = pintarRevision;
}

function pintarDisponibilidadPublicacion() {
  const aviso = document.querySelector('#aviso-publicacion');
  const bloqueado = suspendido() || Boolean(resumen.ofertaVigente);
  aviso.replaceChildren(...(suspendido()
    ? [banda('peligro', 'No puedes publicar mientras el local esté suspendido', 'Tu empresa debe renovar los documentos vencidos.')]
    : resumen.ofertaVigente
      ? [banda('info', 'Ya tienes un pack publicado',
        `Está vigente hasta ${fecha(resumen.ofertaVigente.finRecojo)} a las ${hora(resumen.ofertaVigente.finRecojo)}. Cada local mantiene una sola oferta activa; podrás publicar otra cuando termine su horario.`)]
      : []));
  form.querySelectorAll('fieldset.paso-formulario').forEach(f => { f.disabled = bloqueado; });
  for (const boton of form.querySelectorAll('.acciones-formulario button')) boton.disabled = bloqueado;
}

async function publicar(evento) {
  evento.preventDefault();
  if (![0, 1, 2].every(validarPaso)) return;
  const nuevo = form.elements.origen.value === 'nuevo';
  const datos = new FormData(form);
  const cuerpo = { inicioRecojo: datos.get('inicioRecojo'), finRecojo: datos.get('finRecojo'), variantes: variantesElegidas() };
  if (nuevo) {
    const otro = (datos.get('otroAlergeno') || '').trim();
    cuerpo.pack = {
      nombre: datos.get('nombre').trim(),
      descripcion: datos.get('descripcion').trim(),
      posiblesProductos: datos.get('posiblesProductos').trim(),
      alergenos: [...new Set([...datos.getAll('alergeno'), ...(otro ? [otro] : [])])],
      restricciones: [
        datos.has('aptoVegetarianos') ? 'Apto para vegetarianos' : 'No apto para vegetarianos',
        datos.has('aptoVeganos') ? 'Apto para veganos' : 'No apto para veganos',
        datos.has('aptoCeliacos') ? 'Apto para personas con celiaquía' : 'No apto para personas con celiaquía'
      ]
    };
  } else cuerpo.packId = Number(datos.get('packId'));
  const boton = document.querySelector('#publicar');
  boton.disabled = true;
  try {
    await solicitar(nuevo ? '/local/publicaciones' : '/local/ofertas', { method: 'POST', body: JSON.stringify(cuerpo) });
    form.reset();
    construirCampos();
    alternarOrigen();
    irAPaso(0);
    await cargar();
    mostrarVista('vigente');
    mensaje('Pack publicado. Ya aparece en la búsqueda de los consumidores.');
  } catch (error) {
    estadoEn(document.querySelector('#estado-pack'), error.status === 409 && /oferta activa/.test(error.message)
      ? 'Este local ya tiene una oferta activa. Podrás publicar otra cuando termine su horario.' : error.message, true);
  } finally { boton.disabled = false; }
}

// ---------- Validar código ----------
const resultado = document.querySelector('#resultado-codigo');
document.querySelector('#form-codigo').addEventListener('submit', async evento => {
  evento.preventDefault();
  const codigo = evento.currentTarget.elements.codigo.value.trim().toUpperCase();
  if (!codigo) return;
  resultado.replaceChildren(el('p', { className: 'nota', textContent: 'Buscando el pedido…' }));
  try {
    pintarCompra(await solicitar('/local/recojos/' + encodeURIComponent(codigo)), codigo);
  } catch (error) {
    resultado.replaceChildren(error.status === 404
      ? banda('alerta', 'No encontramos ese código en este local', 'Revisa que el cliente lo haya dictado completo.')
      : banda('peligro', 'No se pudo consultar el código', error.message));
  }
});

function pintarCompra(compra, codigo, recienEntregada = false) {
  const [texto, tono] = ESTADOS_COMPRA[compra.estado] || [compra.estado, ''];
  const zonaAccion = el('div', { className: 'acciones-formulario' });
  const estado = el('p', { className: 'estado-formulario', role: 'status' });
  if (compra.estado === 'CONFIRMADA') {
    zonaAccion.append(el('button', { type: 'button', textContent: 'Confirmar entrega',
      onclick: e => confirmar(e.currentTarget, `/local/recojos/${encodeURIComponent(codigo)}/confirmar`, estado, codigo) }));
    estadoEn(estado, `Entrega el pedido al cliente y luego confirma. Puedes entregarlo hasta las ${hora(compra.finRecojo)}.`);
  } else if (compra.estado === 'PENDIENTE_PAGO' && compra.medioPago === 'EFECTIVO') {
    zonaAccion.append(el('button', { type: 'button', textContent: 'Recibí el efectivo y entregué el pedido',
      onclick: e => confirmar(e.currentTarget, `/local/efectivo/${encodeURIComponent(codigo)}/confirmar`, estado, codigo) }));
    estadoEn(estado, 'Cobra el total en efectivo antes de confirmar. La reserva se libera a los 10 minutos si no se confirma.');
  } else if (!recienEntregada) {
    estadoEn(estado, compra.estado === 'RECOGIDA' ? 'Este pedido ya fue entregado; el código no puede volver a usarse.'
      : 'Este pedido no tiene una entrega pendiente.', compra.estado !== 'RECOGIDA');
  }
  const comision = Math.round(Number(compra.total) * 10) / 100;
  resultado.replaceChildren(
    recienEntregada ? el('div', { className: 'confirmado', role: 'status' }, el('span', { ariaHidden: 'true', textContent: '✓' }),
      el('div', {}, el('strong', { textContent: 'Entrega confirmada correctamente' }),
        el('p', { style: 'margin:.2rem 0 0; font-weight:500', textContent:
          `El código quedó utilizado. Venta ${dinero(compra.total)} − comisión de Sazloop ${dinero(comision)} = ganancia neta ${dinero(Number(compra.total) - comision)}; ya figura en «Pack sorpresa».` }))) : null,
    el('article', { className: 'tarjeta-panel', style: recienEntregada ? 'margin-top:1rem' : '' },
      el('header', {}, el('span', { className: 'codigo-grande', textContent: compra.codigoRecojo }), insignia(texto, tono)),
      el('span', { className: 'dato-titulo', textContent: 'Entregar' }),
      el('ul', { className: 'entrega' }, ...(compra.lineas || []).map(l =>
        el('li', {}, el('span', { textContent: `${l.cantidad} × ${tamanios[l.tamanio] || l.tamanio}` }),
          el('span', { textContent: dinero(l.precioUnitario * l.cantidad) })))),
      el('dl', { className: 'datos-lista' },
        dato('Cliente', compra.cliente),
        dato('Total', dinero(compra.total)),
        dato('Medio de pago', MEDIOS[compra.medioPago] || compra.medioPago),
        dato('Horario de recojo', rangoHorario(compra.inicioRecojo, compra.finRecojo))),
      zonaAccion, estado));
}

const dato = (titulo, valor) => el('div', {}, el('dt', { textContent: titulo }), el('dd', { textContent: valor || '—' }));

async function confirmar(boton, ruta, estado, codigo) {
  boton.disabled = true;
  try {
    const compra = await solicitar(ruta, { method: 'POST' });
    pintarCompra(compra, codigo, true);
    document.querySelector('#form-codigo').reset();
    mensaje('Entrega confirmada. La ganancia del día se actualizó en «Pack sorpresa».');
    await cargar();
  } catch (error) {
    estadoEn(estado, error.message, true);
    boton.disabled = false;
  }
}

// La primera carga va al final: las constantes y los manejadores de arriba deben estar definidos antes de usarse.
await cargar();
