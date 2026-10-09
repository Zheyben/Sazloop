import { solicitar, el } from './comun.js';
import {
  pintarIconos, crearNavegacion, mensaje, estadoEn, insignia, banda, vacio, tabla, fecha,
  tiposDocumento, insigniaLocal
} from './panel.js';

const detalle = document.querySelector('[data-panel-vista="solicitudes:detalle"]');
let establecimientos = [];

pintarIconos();
const mostrar = crearNavegacion((vista, datos) => {
  const cargas = { solicitudes: cargarSolicitudes, 'solicitudes:detalle': () => abrirSolicitud(datos), establecimientos: cargarEstablecimientos };
  cargas[vista]?.().catch(error => mensaje(error.message, true));
});

let admin;
try {
  admin = await solicitar('/administradores/sesion');
} catch {
  location.replace('acceso-administrador.html');
  throw new Error('Sin sesión de administración');
}
document.querySelector('#nombre-admin').textContent = admin.nombre;
document.querySelector('#salir').addEventListener('click', async () => {
  try { await solicitar('/administradores/sesion', { method: 'DELETE' }); } catch { /* la sesión ya no existía */ }
  location.href = 'acceso-administrador.html';
});
document.querySelector('#filtro-estado').addEventListener('change', pintarEstablecimientos);
mostrar('solicitudes');

// ---------- Solicitudes ----------
async function cargarSolicitudes() {
  const solicitudes = await solicitar('/administradores/solicitudes');
  document.querySelector('#contador-solicitudes').textContent = solicitudes.length ? String(solicitudes.length) : '';
  const lista = document.querySelector('#lista-solicitudes');
  if (!solicitudes.length) {
    lista.replaceChildren(vacio('No hay solicitudes pendientes', 'Cuando un local envíe sus documentos o una renovación, aparecerá aquí.'));
    return;
  }
  lista.replaceChildren(...solicitudes.map(s => {
    const revisados = s.documentos.filter(d => d.resultado).length;
    return el('article', { className: 'item-panel' },
      s.renovacion ? insignia('Renovación de documentos', 'alerta') : insignia('Afiliación de local nuevo', 'info'),
      el('h3', { textContent: s.establecimiento }),
      el('p', { textContent: `${s.empresa} · RUC ${s.ruc}` }),
      el('p', { textContent: `${s.direccion}, ${s.distrito}` }),
      el('p', { textContent: `Enviada el ${fecha(s.fechaSolicitud)} · ${revisados} de ${s.documentos.length} documentos revisados` }),
      el('div', { className: 'pie-item' }, insigniaLocal(s.estadoLocal),
        el('button', { type: 'button', textContent: 'Revisar', onclick: () => mostrar('solicitudes:detalle', s.id) })));
  }));
}

async function abrirSolicitud(id, aviso = '') {
  detalle.replaceChildren(el('p', { className: 'nota', textContent: 'Cargando la solicitud…' }));
  const s = await solicitar(`/administradores/solicitudes/${id}`);
  const todosRevisados = s.documentos.length && s.documentos.every(d => d.resultado);
  detalle.replaceChildren(
    el('button', { type: 'button', className: 'secundario volver', textContent: '← Solicitudes', onclick: () => mostrar('solicitudes') }),
    el('div', { className: 'vista-encabezado' },
      el('div', {},
        el('p', { className: 'etiqueta', textContent: s.renovacion ? 'RENOVACIÓN DE DOCUMENTOS' : 'SOLICITUD DE AFILIACIÓN' }),
        el('h2', { textContent: s.establecimiento }),
        el('p', { textContent: `${s.empresa} · RUC ${s.ruc} · ${s.direccion}, ${s.distrito}` })),
      insigniaLocal(s.estadoLocal)),
    s.renovacion ? banda('info', 'Renovación',
      'Al aprobarla, estos documentos reemplazan a los anteriores del mismo tipo. Si el local está suspendido y ya no le quedan documentos vencidos, se reactiva automáticamente.') : null,
    el('div', { style: 'display:grid; gap:1rem' }, ...s.documentos.map(doc => tarjetaDocumento(doc, s))),
    s.estado === 'EN_REVISION'
      ? (todosRevisados ? formularioDecision(s) : el('p', { className: 'nota', style: 'margin-top:1.2rem', textContent: 'Revisa todos los documentos para registrar la decisión final.' }))
      : banda('info', 'Solicitud resuelta', s.motivoDecision ? `Motivo: ${s.motivoDecision}` : ''));
  if (aviso) mensaje(aviso);
}

function tarjetaDocumento(doc, solicitud) {
  const resultado = { APROBADO: ['Aprobado', 'ok'], RECHAZADO: ['Rechazado', 'peligro'] }[doc.resultado] || ['Pendiente de revisión', 'alerta'];
  const tarjeta = el('article', { className: 'tarjeta-panel documento-revision' },
    el('header', {}, el('h4', { textContent: tiposDocumento[doc.tipo] || doc.tipo }), insignia(...resultado)),
    el('dl', { className: 'datos-lista' },
      dato('Número', doc.numero), dato('Emisión', fecha(doc.emision)), dato('Vencimiento', fecha(doc.vencimiento))),
    el('a', { className: 'enlace-archivo', href: `/api/administradores/documentos/${doc.id}/archivo`, textContent: 'Descargar el documento para revisarlo' }));
  if (doc.resultado) {
    if (doc.observaciones) tarjeta.append(el('p', { className: 'nota', style: 'margin:0', textContent: `Observaciones: ${doc.observaciones}` }));
    return tarjeta;
  }
  const formulario = el('form', { className: 'formulario-plano' },
    el('fieldset', { className: 'opciones-radio' }, el('legend', { textContent: 'Resultado de la revisión' }),
      el('label', { className: 'opcion' }, el('input', { type: 'radio', name: 'resultado', value: 'APROBADO', checked: true }), 'Aprobar documento'),
      el('label', { className: 'opcion' }, el('input', { type: 'radio', name: 'resultado', value: 'RECHAZADO' }), 'Rechazar documento')),
    el('fieldset', { className: 'grupo' }, el('legend', { textContent: 'Datos del documento' }),
      el('p', { className: 'ayuda', textContent: 'Corrige lo que no coincida con el archivo. La fecha de vencimiento activa el aviso previo y la suspensión automática.' }),
      el('div', { className: 'campos' },
        el('label', {}, 'Tipo', el('select', { name: 'tipo' }, ...Object.entries(tiposDocumento).map(([valor, texto]) =>
          el('option', { value: valor, textContent: texto, selected: valor === doc.tipo })))),
        el('label', {}, 'Número', el('input', { name: 'numero', maxLength: 80, value: doc.numero || '' })),
        el('label', {}, 'Emisión', el('input', { name: 'emision', type: 'date', value: doc.emision || '' })),
        el('label', {}, 'Vencimiento', el('input', { name: 'vencimiento', type: 'date', value: doc.vencimiento || '' })))),
    el('label', { style: 'margin-top:1rem' }, 'Observaciones',
      el('textarea', { name: 'observaciones', rows: 2, maxLength: 500, placeholder: 'Obligatorias si rechazas el documento' })),
    el('p', { className: 'estado-formulario', role: 'status' }),
    el('div', { className: 'acciones-formulario' }, el('button', { type: 'submit', textContent: 'Guardar revisión' })));
  formulario.addEventListener('submit', async evento => {
    evento.preventDefault();
    const datos = Object.fromEntries(new FormData(formulario));
    const estado = formulario.querySelector('.estado-formulario');
    if (datos.resultado === 'RECHAZADO' && !datos.observaciones.trim()) {
      estadoEn(estado, 'Indica por qué se rechaza el documento.', true);
      return;
    }
    const cuerpo = { resultado: datos.resultado, observaciones: datos.observaciones.trim() || null, tipo: datos.tipo,
      numero: datos.numero.trim() || null, emision: datos.emision || null, vencimiento: datos.vencimiento || null };
    try {
      await solicitar(`/administradores/documentos/${doc.id}/revision`, { method: 'POST', body: JSON.stringify(cuerpo) });
      await abrirSolicitud(solicitud.id, 'Revisión guardada.');
    } catch (error) { estadoEn(estado, error.message, true); }
  });
  tarjeta.append(formulario);
  return tarjeta;
}

function formularioDecision(s) {
  const ahora = new Date();
  const hoy = new Date(ahora.getTime() - ahora.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
  const rechazados = s.documentos.some(d => d.resultado !== 'APROBADO');
  const vencidos = s.documentos.some(d => d.vencimiento && d.vencimiento < hoy);
  const noAprobable = rechazados || vencidos;
  const formulario = el('form', { className: 'formulario-panel', style: 'margin-top:1.4rem' },
    el('h3', { style: 'margin-top:0', textContent: 'Decisión final' }),
    noAprobable ? banda('alerta', rechazados ? 'Hay documentos rechazados' : 'Hay documentos vencidos', 'Solo puedes rechazar esta solicitud e indicar el motivo.') : null,
    el('fieldset', { className: 'opciones-radio' }, el('legend', { textContent: 'Decisión' }),
      el('label', { className: 'opcion' }, el('input', { type: 'radio', name: 'decision', value: 'APROBADA', checked: !noAprobable, disabled: noAprobable }),
        s.renovacion ? 'Aprobar renovación' : 'Aprobar local'),
      el('label', { className: 'opcion' }, el('input', { type: 'radio', name: 'decision', value: 'RECHAZADA', checked: noAprobable }),
        s.renovacion ? 'Rechazar renovación' : 'Rechazar solicitud')),
    el('label', { style: 'margin-top:1rem' }, 'Motivo', el('textarea', { name: 'motivo', rows: 3, maxLength: 500, required: true,
      placeholder: 'Se enviará al propietario junto con la decisión' })),
    el('p', { className: 'estado-formulario', role: 'status' }),
    el('div', { className: 'acciones-formulario' }, el('button', { type: 'submit', textContent: 'Guardar decisión' })));
  formulario.addEventListener('submit', async evento => {
    evento.preventDefault();
    const datos = Object.fromEntries(new FormData(formulario));
    try {
      await solicitar(`/administradores/solicitudes/${s.id}/decision`, { method: 'POST',
        body: JSON.stringify({ decision: datos.decision, motivo: datos.motivo.trim() }) });
      mostrar('solicitudes');
      mensaje(datos.decision === 'APROBADA'
        ? (s.renovacion ? 'Renovación aprobada. El propietario fue notificado.' : 'Local aprobado. El propietario ya puede crear la cuenta del trabajador.')
        : 'Solicitud rechazada. El propietario recibirá el motivo.');
    } catch (error) { estadoEn(formulario.querySelector('.estado-formulario'), error.message, true); }
  });
  return formulario;
}

const dato = (titulo, valor) => el('div', {}, el('dt', { textContent: titulo }), el('dd', { textContent: valor || '—' }));

// ---------- Establecimientos ----------
async function cargarEstablecimientos() {
  establecimientos = await solicitar('/administradores/establecimientos');
  pintarEstablecimientos();
}

function pintarEstablecimientos() {
  const filtro = document.querySelector('#filtro-estado input:checked').value;
  const lista = establecimientos.filter(l => !filtro || l.estado === filtro);
  const zona = document.querySelector('#tabla-establecimientos');
  if (!lista.length) {
    zona.replaceChildren(vacio('No hay locales con ese estado', ''));
    return;
  }
  zona.replaceChildren(tabla([['Local'], ['Empresa'], ['Distrito'], ['Estado'], ['Detalle']], lista.map(l => [
    l.nombre, `${l.empresa} · RUC ${l.ruc}`, l.distrito, insigniaLocal(l.estado),
    el('div', { style: 'display:grid; gap:.35rem' },
      l.estado === 'SUSPENDIDO' ? el('span', { textContent:
        `${l.motivoSuspension || 'Suspendido'} · desde el ${fecha(l.suspendidoDesde)}${l.suspensionAutomatica ? ' (suspensión automática)' : ''}` }) : null,
      l.solicitudEnRevision ? insignia('Solicitud en revisión', 'info') : null,
      l.estado !== 'SUSPENDIDO' && !l.solicitudEnRevision ? el('span', { className: 'nota', textContent: '—' }) : null)
  ])));
}
