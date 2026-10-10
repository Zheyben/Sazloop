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

async function abrirSolicitud(id) {
  detalle.replaceChildren(el('p', { className: 'nota', textContent: 'Cargando la solicitud…' }));
  const s = await solicitar(`/administradores/solicitudes/${id}`);
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
    s.estado === 'EN_REVISION' ? formularioResolucion(s)
      : el('div', {}, ...s.documentos.map(tarjetaLectura),
        banda('info', 'Solicitud resuelta', s.motivoDecision ? `Motivo: ${s.motivoDecision}` : '')));
}

const hoyLocal = () => {
  const d = new Date();
  return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
};
const dato = (titulo, valor) => el('div', {}, el('dt', { textContent: titulo }), el('dd', { textContent: valor || '—' }));

/** Datos del documento tal como los presentó el propietario (solo lectura). */
function datosDocumento(doc) {
  const vencido = Boolean(doc.vencimiento) && doc.vencimiento < hoyLocal();
  return {
    vencido,
    lista: el('dl', { className: 'datos-lista' },
      dato('Número', doc.numero), dato('Emisión', fecha(doc.emision)),
      dato('Vencimiento', vencido ? `${fecha(doc.vencimiento)} (vencido)` : fecha(doc.vencimiento)))
  };
}

/** Documento ya revisado: solo se muestra. */
function tarjetaLectura(doc) {
  const resultado = { APROBADO: ['Aprobado', 'ok'], RECHAZADO: ['Rechazado', 'peligro'] }[doc.resultado] || ['Sin revisar', 'alerta'];
  return el('article', { className: 'tarjeta-panel documento-revision', style: 'margin-bottom:1rem' },
    el('header', {}, el('h4', { textContent: tiposDocumento[doc.tipo] || doc.tipo }), insignia(...resultado)),
    datosDocumento(doc).lista,
    doc.observaciones ? el('p', { className: 'nota', style: 'margin:0', textContent: `Observaciones: ${doc.observaciones}` }) : null,
    el('a', { className: 'enlace-archivo', href: `/api/administradores/documentos/${doc.id}/archivo`, textContent: 'Descargar el documento' }));
}

/** Una sola pantalla: se marca cada documento y se guarda una única decisión. */
function formularioResolucion(s) {
  const formulario = el('form', { className: 'formulario-plano', style: 'display:grid; gap:1rem' });
  const filas = s.documentos.map(doc => {
    if (doc.resultado) return { doc, tarjeta: tarjetaLectura(doc), previa: true };
    const { vencido, lista } = datosDocumento(doc);
    const nombre = `resultado-${doc.id}`;
    const aprobar = el('input', { type: 'radio', name: nombre, value: 'APROBADO', checked: !vencido, disabled: vencido });
    const rechazar = el('input', { type: 'radio', name: nombre, value: 'RECHAZADO', checked: vencido });
    const observaciones = el('textarea', { rows: 2, maxLength: 500, placeholder: 'Obligatorias si rechazas el documento' });
    const tarjeta = el('article', { className: 'tarjeta-panel documento-revision' },
      el('header', {}, el('h4', { textContent: tiposDocumento[doc.tipo] || doc.tipo }),
        insignia(vencido ? 'Vencido' : 'Por revisar', vencido ? 'peligro' : 'alerta')),
      lista,
      el('a', { className: 'enlace-archivo', href: `/api/administradores/documentos/${doc.id}/archivo`, textContent: 'Descargar el documento para revisarlo' }),
      vencido ? el('p', { className: 'nota', style: 'margin:0', textContent: 'Un documento vencido solo puede rechazarse.' }) : null,
      el('fieldset', { className: 'opciones-radio' }, el('legend', { textContent: 'Resultado' }),
        el('label', { className: 'opcion' }, aprobar, 'Aprobar'),
        el('label', { className: 'opcion' }, rechazar, 'Rechazar')),
      el('label', {}, 'Observaciones', observaciones));
    return { doc, tarjeta, previa: false, aprobar, rechazar, observaciones };
  });
  const comentario = el('textarea', { rows: 2, maxLength: 500, placeholder: 'Opcional: se enviará al propietario junto con la decisión' });
  const resultadoFinal = el('p', { className: 'nota', style: 'margin:0', role: 'status' });
  const estado = el('p', { className: 'estado-formulario', role: 'status' });
  const boton = el('button', { type: 'submit', textContent: 'Guardar decisión' });
  const aprobado = fila => fila.previa ? fila.doc.resultado === 'APROBADO' : fila.aprobar.checked;
  const actualizar = () => {
    resultadoFinal.textContent = filas.every(aprobado)
      ? (s.renovacion ? 'Se aprobará la renovación del local.' : 'Se aprobará el local.')
      : (s.renovacion ? 'Se rechazará la renovación porque hay documentos rechazados.'
        : 'Se rechazará la solicitud porque hay documentos rechazados.');
  };
  formulario.addEventListener('change', actualizar);
  actualizar();
  formulario.append(...filas.map(f => f.tarjeta),
    el('label', { style: 'margin:0' }, 'Comentario general (opcional)', comentario), resultadoFinal, estado,
    el('div', { className: 'acciones-formulario', style: 'margin-top:0' }, boton));
  formulario.addEventListener('submit', async evento => {
    evento.preventDefault();
    const pendientes = filas.filter(f => !f.previa);
    const sinMotivo = pendientes.find(f => f.rechazar.checked && !f.observaciones.value.trim());
    if (sinMotivo) {
      estadoEn(estado, `Indica por qué se rechaza: ${tiposDocumento[sinMotivo.doc.tipo] || sinMotivo.doc.tipo}.`, true);
      sinMotivo.observaciones.focus();
      return;
    }
    boton.disabled = true;
    try {
      await solicitar(`/administradores/solicitudes/${s.id}/resolucion`, { method: 'POST', body: JSON.stringify({
        documentos: pendientes.map(f => ({ id: f.doc.id, resultado: f.aprobar.checked ? 'APROBADO' : 'RECHAZADO',
          observaciones: f.observaciones.value.trim() || null })),
        comentario: comentario.value.trim() || null
      }) });
      const aprobo = filas.every(aprobado);
      mostrar('solicitudes');
      mensaje(aprobo
        ? (s.renovacion ? 'Renovación aprobada. El propietario fue notificado.'
          : 'Local aprobado. El propietario ya puede crear la cuenta del trabajador.')
        : 'Solicitud rechazada. El propietario recibirá el motivo.');
    } catch (error) {
      estadoEn(estado, error.message, true);
      boton.disabled = false;
    }
  });
  return formulario;
}

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
