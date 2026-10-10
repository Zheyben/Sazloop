import { solicitar, el, dinero } from './comun.js';
import {
  pintarIconos, crearNavegacion, mensaje, estadoEn, insignia, banda, vacio, cifra, tabla, fecha,
  tiposDocumento, insigniaLocal, vigencias, listaAvisos
} from './panel.js';

const lateral = document.querySelector('.panel-lateral');
const detalle = document.querySelector('[data-panel-vista="locales:detalle"]');
let empresa = null;

pintarIconos();
let vistaActual = ['resumen', null];
const mostrar = crearNavegacion((vista, datos) => {
  vistaActual = [vista, datos];
  const cargas = { resumen: cargarResumen, locales: cargarLocales, 'locales:detalle': () => abrirLocal(datos), avisos: cargarAvisos };
  cargas[vista]?.().catch(error => mensaje(error.message, true));
});

let usuario;
try {
  usuario = await solicitar('/sesion');
} catch {
  location.replace('acceso-locales.html#empresa');
  throw new Error('Sin sesión');
}
document.querySelector('#nombre-usuario').textContent = usuario.nombres.trim().split(/\s+/)[0];
document.querySelector('#salir').addEventListener('click', async () => {
  try { await solicitar('/sesion', { method: 'DELETE' }); } catch { /* la sesión ya no existía */ }
  location.href = 'acceso-locales.html#empresa';
});

empresa = await solicitar('/empresas/actual');
if (empresa) iniciarEmpresa(); else { lateral.hidden = true; mostrar('registro'); }

function iniciarEmpresa() {
  document.querySelector('#titulo-panel').textContent = empresa.nombreComercial;
  document.title = `Sazloop | ${empresa.nombreComercial}`;
  lateral.hidden = false;
  actualizarContador();
  mostrar('resumen');
}

// ---------- Registro de la empresa ----------
document.querySelector('#form-empresa').addEventListener('submit', async evento => {
  evento.preventDefault();
  const formulario = evento.currentTarget;
  const estado = formulario.querySelector('.estado-formulario');
  try {
    empresa = await solicitar('/empresas', { method: 'POST', body: JSON.stringify(Object.fromEntries(new FormData(formulario))) });
    iniciarEmpresa();
    mostrar('locales');
    abrirFormularioLocal();
    mensaje('Empresa registrada. Ahora registra tu primer local.');
  } catch (error) { estadoEn(estado, error.message, true); }
});

// ---------- Resumen ----------
document.querySelector('#actualizar-resumen').addEventListener('click', () => cargarResumen().catch(e => mensaje(e.message, true)));

async function cargarResumen() {
  // Cada consulta es independiente: si una falla, el resto del resumen se sigue mostrando.
  const [resIndicadores, resLocales] = await Promise.allSettled([
    solicitar('/empresas/actual/indicadores'), solicitar('/empresas/actual/establecimientos')]);
  const locales = resLocales.status === 'fulfilled' ? resLocales.value : [];
  pintarAlertas(locales);
  const cifras = document.querySelector('#cifras-empresa');
  const detalle = document.querySelector('#detalle-establecimientos');
  if (resIndicadores.status === 'rejected') {
    const aviso = banda('peligro', 'No se pudieron cargar los indicadores', resIndicadores.reason.message);
    cifras.replaceChildren(aviso);
    detalle.replaceChildren();
    return;
  }
  const { establecimientos, total } = resIndicadores.value;
  cifras.replaceChildren(
    cifra('Ventas', String(total.ventas), `${dinero(total.importeVentas)} vendidos`, true),
    cifra('Ingresos de tus locales', dinero(total.ingresos), 'Después de la comisión'),
    cifra('Comisión de Sazloop', dinero(total.comisiones), '10 % de lo cobrado'),
    cifra('Cancelaciones', String(total.cancelaciones), 'Con reembolso del 50 %'),
    cifra('No recogidas', String(total.noRecogidas), 'Pagadas y sin entrega'),
    cifra('Calificación', total.cantidadResenas ? `${Number(total.calificacionPromedio).toFixed(1)} / 5` : '—',
      `${total.cantidadResenas} ${total.cantidadResenas === 1 ? 'reseña' : 'reseñas'}`),
    cifra('Packs entregados', String(total.packsEntregados), 'Entregados en vez de descartados'));
  if (!establecimientos.length) {
    detalle.replaceChildren(vacio('Aún no tienes locales', 'Registra tu primer establecimiento para empezar.',
      el('button', { type: 'button', textContent: 'Agregar local', onclick: () => { mostrar('locales'); abrirFormularioLocal(); } })));
    return;
  }
  detalle.replaceChildren(...establecimientos.map(i => tarjetaIndicadores(i, locales.find(l => l.id === i.establecimientoId))));
}

function pintarAlertas(locales) {
  const alertas = [];
  for (const local of locales.filter(l => l.estado === 'SUSPENDIDO')) {
    alertas.push(banda('peligro', `${local.nombre} está suspendido`,
      'Un documento obligatorio venció. Presenta su renovación para reactivarlo.',
      el('button', { type: 'button', className: 'secundario', style: 'margin-top:.6rem', textContent: 'Ver documentos',
        onclick: () => mostrar('locales:detalle', local.id) })));
  }
  const pendientes = locales.filter(l => l.estado === 'PENDIENTE_APROBACION');
  if (pendientes.length) alertas.push(banda('alerta',
    `${pendientes.length} ${pendientes.length === 1 ? 'local espera' : 'locales esperan'} aprobación`,
    'Adjunta sus documentos y envía la solicitud desde Mis locales; el administrador la revisa y te avisa el resultado.'));
  const sinCuenta = locales.filter(l => l.estado === 'ACTIVO' && !l.cuentaTrabajador);
  if (sinCuenta.length) alertas.push(banda('info', 'Crea la cuenta del trabajador',
    `${sinCuenta.map(l => l.nombre).join(', ')} ya ${sinCuenta.length === 1 ? 'fue aprobado' : 'fueron aprobados'}; su personal necesita una cuenta para publicar packs y entregar pedidos.`));
  document.querySelector('#alertas-resumen').replaceChildren(...alertas);
}

function tarjetaIndicadores(i, local) {
  const resenas = i.resenasRecientes || [];
  return el('article', { className: 'tarjeta-panel', style: 'margin-top:1rem' },
    el('div', { className: 'vista-encabezado', style: 'margin-bottom:1rem' },
      el('div', {}, el('h3', { style: 'margin:0', textContent: i.establecimiento }),
        local ? el('p', { className: 'nota', style: 'margin:.2rem 0 0', textContent: `${local.direccion}, ${local.distrito}` }) : null),
      local ? insigniaLocal(local.estado) : null),
    el('div', { className: 'cifras' },
      cifra('Ventas', String(i.ventas), `${dinero(i.importeVentas)} vendidos`),
      cifra('Ingresos del local', dinero(i.ingresos), 'Después de la comisión'),
      cifra('Comisión de Sazloop', dinero(i.comisiones), '10 % de lo cobrado'),
      cifra('Cancelaciones', String(i.cancelaciones), 'Con reembolso del 50 %'),
      cifra('No recogidas', String(i.noRecogidas), 'Pagadas y sin entrega'),
      cifra('Calificación', i.cantidadResenas ? `${Number(i.calificacionPromedio).toFixed(1)} / 5` : '—',
        `${i.cantidadResenas} ${i.cantidadResenas === 1 ? 'reseña' : 'reseñas'}`),
      cifra('Packs entregados', String(i.packsEntregados), 'Impacto de este local')),
    el('h4', { style: 'margin:1.2rem 0 .6rem; color: var(--bosque); font: 400 1.2rem/1.2 var(--serif)', textContent: 'Reseñas recientes' }),
    resenas.length
      ? el('ul', { className: 'lista-avisos' }, ...resenas.map(r => el('li', { className: 'aviso-item', style: 'grid-template-columns:1fr' },
        el('strong', { textContent: '★'.repeat(r.calificacion) + '☆'.repeat(5 - r.calificacion) }),
        el('p', { textContent: r.comentario || 'Sin comentario.' }),
        el('div', { className: 'pie-aviso' }, el('span', { textContent: fecha(r.fecha.slice(0, 10)) })))))
      : el('p', { className: 'nota', style: 'margin:0', textContent: 'Todavía no hay reseñas para este local.' }));
}

// ---------- Locales ----------
const formLocal = document.querySelector('#form-local');
function abrirFormularioLocal() {
  formLocal.hidden = false;
  formLocal.elements.nombre.focus();
}
document.querySelector('#nuevo-local').addEventListener('click', abrirFormularioLocal);
document.querySelector('#cancelar-local').addEventListener('click', () => { formLocal.reset(); formLocal.hidden = true; });
formLocal.addEventListener('submit', async evento => {
  evento.preventDefault();
  const { departamento, provincia, distrito, direccionLinea, numero, referencia, codigoPostal, ...local } =
    Object.fromEntries(new FormData(formLocal));
  local.direccion = { departamento, provincia, distrito, direccionLinea, numero, referencia, codigoPostal };
  try {
    const creado = await solicitar('/empresas/actual/establecimientos', { method: 'POST', body: JSON.stringify(local) });
    formLocal.reset();
    formLocal.hidden = true;
    mostrar('locales:detalle', creado.id);
    mensaje('Local registrado. Ahora adjunta sus documentos y envía la solicitud.');
  } catch (error) { estadoEn(formLocal.querySelector('.estado-formulario'), error.message, true); }
});

async function cargarLocales() {
  const locales = await solicitar('/empresas/actual/establecimientos');
  const lista = document.querySelector('#lista-locales');
  if (!locales.length) {
    lista.replaceChildren(vacio('Aún no registras locales', 'Agrega tu primer establecimiento con su dirección.'));
    abrirFormularioLocal();
    return;
  }
  lista.replaceChildren(...locales.map(local => el('article', { className: 'item-panel' },
    insigniaLocal(local.estado),
    el('h3', { textContent: local.nombre }),
    el('p', { textContent: `${local.direccion}, ${local.distrito}` }),
    el('p', { textContent: local.estado === 'PENDIENTE_APROBACION' ? 'Presenta sus documentos para que Sazloop lo apruebe.'
      : local.cuentaTrabajador ? `Cuenta del trabajador: ${local.cuentaTrabajador}` : 'Aún no tiene cuenta de trabajador.' }),
    el('div', { className: 'pie-item' },
      el('span', { className: 'nota', textContent: 'Documentos y cuenta' }),
      el('button', { type: 'button', textContent: 'Gestionar', onclick: () => mostrar('locales:detalle', local.id) })))));
}

async function abrirLocal(id, aviso = '') {
  detalle.replaceChildren(el('p', { className: 'nota', textContent: 'Cargando el local…' }));
  const [doc, locales] = await Promise.all([
    solicitar(`/establecimientos/${id}/documentacion`), solicitar('/empresas/actual/establecimientos')]);
  const local = locales.find(l => l.id === id);
  const aprobado = local.estado === 'ACTIVO' || local.estado === 'SUSPENDIDO';
  detalle.replaceChildren(
    el('button', { type: 'button', className: 'secundario volver', textContent: '← Mis locales', onclick: () => mostrar('locales') }),
    el('div', { className: 'vista-encabezado' },
      el('div', {}, el('p', { className: 'etiqueta', textContent: 'ESTABLECIMIENTO' }),
        el('h2', { textContent: local.nombre }),
        el('p', { textContent: `${local.direccion}, ${local.distrito}` })),
      insigniaLocal(local.estado)),
    local.estado === 'SUSPENDIDO' ? banda('peligro', 'Local suspendido',
      `${doc.motivoSuspension || 'Un documento obligatorio venció.'}${doc.suspendidoDesde ? ` Suspendido desde el ${fecha(doc.suspendidoDesde)}.` : ''} `
      + 'No aparece en la búsqueda ni puede publicar packs; el trabajador sí puede entregar las reservas ya pagadas. '
      + 'Presenta la renovación de los documentos vencidos para reactivarlo.') : null,
    el('section', { className: 'tarjeta-panel' },
      el('h3', { textContent: aprobado ? 'Documentos del local' : 'Solicitud de afiliación' }),
      ...(aprobado ? seccionVigentes(doc, id) : seccionAfiliacion(doc, id))),
    el('section', { className: 'tarjeta-panel', style: 'margin-top:1rem' },
      el('h3', { textContent: 'Cuenta del trabajador' }), ...seccionCuenta(local)));
  if (aviso) mensaje(aviso);
}

function tablaDocumentos(localId, documentos) {
  return tabla([['Documento'], ['Número'], ['Emisión'], ['Vencimiento'], ['Archivo']],
    documentos.map(d => [tiposDocumento[d.tipo] || d.tipo, d.numero || '—', fecha(d.fechaEmision), fecha(d.fechaVencimiento),
      el('a', { href: `/api/establecimientos/${localId}/documentos/${d.id}/archivo`, className: 'enlace-archivo', textContent: 'Descargar' })]));
}

function seccionAfiliacion(doc, localId) {
  const s = doc.solicitud;
  const partes = [];
  if (!s || s.estado === 'PENDIENTE') {
    partes.push(el('p', { className: 'nota', textContent: '1. Adjunta los documentos del local (licencia, certificado sanitario u otros). 2. Envía la solicitud. 3. Un administrador revisa cada documento y aprueba o rechaza con un motivo.' }));
  } else if (s.estado === 'EN_REVISION') {
    partes.push(banda('info', 'Solicitud en revisión', 'Un administrador revisa manualmente cada documento. Verás el resultado en Notificaciones.'));
  } else if (s.estado === 'RECHAZADA') {
    partes.push(banda('alerta', 'Solicitud rechazada', `Motivo: ${s.motivoDecision}. Adjunta nuevos documentos para presentar otra solicitud.`));
  }
  if (s && s.estado !== 'RECHAZADA' && s.documentos.length) partes.push(tablaDocumentos(localId, s.documentos));
  if (!s || s.estado === 'PENDIENTE' || s.estado === 'RECHAZADA') {
    partes.push(formularioDocumento(localId, Object.keys(tiposDocumento), false));
  }
  if (s?.estado === 'PENDIENTE' && s.documentos.length) partes.push(botonEnviar(localId, 'Enviar solicitud a revisión'));
  return partes;
}

function seccionVigentes(doc, localId) {
  const partes = [];
  partes.push(doc.documentos.length ? tabla([['Documento'], ['Número'], ['Vence'], ['Estado']],
    doc.documentos.map(d => {
      const [texto, tono] = vigencias[d.vigencia] || [d.vigencia, ''];
      return [tiposDocumento[d.tipo] || d.tipo, d.numero || '—', fecha(d.vencimiento), insignia(texto, tono)];
    })) : el('p', { className: 'nota', textContent: 'No hay documentos aprobados registrados.' }));
  const s = doc.solicitud;
  const renovacion = s?.renovacion ? s : null;
  partes.push(el('h3', { style: 'margin-top:1.6rem', textContent: 'Renovación' }));
  if (renovacion?.estado === 'EN_REVISION') {
    partes.push(banda('info', 'Renovación en revisión', 'Un administrador revisa los documentos renovados. Te avisaremos el resultado.'),
      tablaDocumentos(localId, renovacion.documentos));
    return partes;
  }
  if (renovacion?.estado === 'RECHAZADA') partes.push(banda('alerta', 'La última renovación fue rechazada', `Motivo: ${renovacion.motivoDecision}`));
  if (renovacion?.estado === 'APROBADA') partes.push(el('p', { className: 'nota', textContent: `Última renovación aprobada: ${renovacion.motivoDecision}` }));
  if (!doc.tiposRenovables.length) {
    partes.push(el('p', { className: 'nota', textContent: 'No hay documentos por renovar. Te avisaremos siete días antes de cada vencimiento.' }));
    return partes;
  }
  const nombres = doc.tiposRenovables.map(t => tiposDocumento[t] || t).join(', ');
  partes.push(el('p', { textContent: doc.estado === 'SUSPENDIDO'
    ? `Presenta solo los documentos vencidos: ${doc.tiposVencidos.map(t => tiposDocumento[t]).join(', ') || nombres}.`
    : `Vencen en los próximos días: ${nombres}. Renuévalos antes del vencimiento para evitar la suspensión.` }));
  if (renovacion?.estado === 'PENDIENTE' && renovacion.documentos.length) partes.push(tablaDocumentos(localId, renovacion.documentos));
  partes.push(formularioDocumento(localId, doc.tiposRenovables, true));
  if (renovacion?.estado === 'PENDIENTE' && renovacion.documentos.length) partes.push(botonEnviar(localId, 'Enviar renovación a revisión'));
  return partes;
}

function formularioDocumento(localId, tipos, renovacion) {
  const formulario = el('form', { className: 'formulario-plano', style: 'margin-top:1.2rem' },
    el('h4', { style: 'margin:0 0 .8rem', textContent: renovacion ? 'Adjuntar documento renovado' : 'Adjuntar documento' }),
    el('div', { className: 'campos' },
      el('label', {}, 'Tipo de documento', el('select', { name: 'tipo', required: true },
        ...tipos.map(t => el('option', { value: t, textContent: tiposDocumento[t] || t })))),
      el('label', {}, 'Número del documento', el('input', { name: 'numero', maxLength: 80 })),
      el('label', {}, 'Fecha de emisión', el('input', { name: 'emision', type: 'date' })),
      el('label', {}, 'Fecha de vencimiento', el('input', { name: 'vencimiento', type: 'date', required: renovacion }),
        el('span', { className: 'ayuda', textContent: 'Si el documento vence, indica la fecha: Sazloop te avisará siete días antes.' })),
      el('label', { className: 'ancho-completo' }, 'Archivo PDF, PNG o JPG (hasta 10 MB)',
        el('input', { name: 'archivo', type: 'file', accept: '.pdf,.png,.jpg,.jpeg', required: true }))),
    el('p', { className: 'estado-formulario', role: 'status' }),
    el('div', { className: 'acciones-formulario' }, el('button', { type: 'submit', className: 'secundario', textContent: 'Adjuntar' })));
  formulario.addEventListener('submit', async evento => {
    evento.preventDefault();
    const datos = new FormData(formulario);
    for (const campo of ['numero', 'emision', 'vencimiento']) if (!datos.get(campo)) datos.delete(campo);
    const boton = formulario.querySelector('button[type="submit"]');
    boton.disabled = true;
    try {
      await solicitar(`/establecimientos/${localId}/documentos`, { method: 'POST', body: datos });
      await abrirLocal(localId, 'Documento adjuntado. Puedes añadir otro o enviar la solicitud.');
    } catch (error) {
      estadoEn(formulario.querySelector('.estado-formulario'), error.message, true);
      boton.disabled = false;
    }
  });
  return formulario;
}

function botonEnviar(localId, texto) {
  const estado = el('p', { className: 'estado-formulario', role: 'status' });
  const boton = el('button', { type: 'button', textContent: texto, onclick: async () => {
    boton.disabled = true;
    try {
      await solicitar(`/establecimientos/${localId}/solicitud/enviar`, { method: 'POST' });
      await abrirLocal(localId, 'Enviado a revisión. Te avisaremos el resultado en Notificaciones.');
    } catch (error) { estadoEn(estado, error.message, true); boton.disabled = false; }
  } });
  return el('div', {}, el('div', { className: 'acciones-formulario' }, boton), estado);
}

function seccionCuenta(local) {
  if (local.estado === 'PENDIENTE_APROBACION' || local.estado === 'INACTIVO') {
    return [el('p', { className: 'nota', textContent: 'Podrás crear la cuenta del trabajador cuando Sazloop apruebe el local.' })];
  }
  if (local.cuentaTrabajador) {
    return [el('p', {}, 'Usuario del local: ', el('strong', { textContent: local.cuentaTrabajador })),
      el('p', { className: 'nota', textContent: 'El personal ingresa desde Iniciar sesión → Empresa o trabajador → Trabajador. Por seguridad, la contraseña no se puede consultar.' })];
  }
  const formulario = el('form', { className: 'formulario-plano' },
    el('p', { className: 'nota', textContent: 'Es una cuenta compartida por el personal de este local. Anota las credenciales antes de crearla: luego no podrás consultar la contraseña.' }),
    el('div', { className: 'campos' },
      el('label', {}, 'Usuario', el('input', { name: 'nombreUsuario', maxLength: 80, required: true, autocomplete: 'off' })),
      el('label', {}, 'Contraseña', el('input', { name: 'contrasena', type: 'password', minLength: 8, maxLength: 128, required: true, autocomplete: 'new-password' }),
        el('span', { className: 'ayuda', textContent: 'Mínimo 8 caracteres.' }))),
    el('p', { className: 'estado-formulario', role: 'status' }),
    el('div', { className: 'acciones-formulario' }, el('button', { type: 'submit', textContent: 'Crear cuenta del trabajador' })));
  formulario.addEventListener('submit', async evento => {
    evento.preventDefault();
    try {
      await solicitar(`/establecimientos/${local.id}/cuenta`, { method: 'POST', body: JSON.stringify(Object.fromEntries(new FormData(formulario))) });
      await abrirLocal(local.id, 'Cuenta creada. Comparte el usuario y la contraseña con el personal del local.');
    } catch (error) { estadoEn(formulario.querySelector('.estado-formulario'), error.message, true); }
  });
  return [formulario];
}

// ---------- Notificaciones ----------
async function cargarAvisos() {
  const avisos = await solicitar('/notificaciones');
  pintarContador(avisos);
  document.querySelector('#lista-avisos').replaceChildren(listaAvisos(avisos, {
    alLeer: async aviso => { await solicitar(`/notificaciones/${aviso.id}/leer`, { method: 'POST' }); await cargarAvisos(); },
    alLeerTodas: async () => { await solicitar('/notificaciones/leer-todas', { method: 'POST' }); await cargarAvisos(); }
  }));
}

async function actualizarContador() {
  try { pintarContador(await solicitar('/notificaciones')); } catch { /* el contador es informativo */ }
}

function pintarContador(avisos) {
  const pendientes = avisos.filter(a => !a.leida).length;
  document.querySelector('#contador-avisos').textContent = pendientes ? String(pendientes) : '';
}

// Si el administrador aprueba o rechaza mientras esta pestaña estaba en segundo plano, se actualiza al volver.
document.addEventListener('visibilitychange', () => {
  if (document.visibilityState !== 'visible' || !empresa || vistaActual[0] === 'registro') return;
  if (document.activeElement && ['INPUT', 'TEXTAREA', 'SELECT'].includes(document.activeElement.tagName)) return;
  actualizarContador();
  if (vistaActual[0] !== 'locales:detalle' || !detalle.querySelector('form')) mostrar(vistaActual[0], vistaActual[1]);
});
