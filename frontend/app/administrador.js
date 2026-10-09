import { solicitar, el, aviso } from './comun.js';

const panel = document.querySelector('#panelAdmin');
const lista = document.querySelector('#solicitudesAdmin');
const detalle = document.querySelector('#detalleSolicitud');

async function cargarSolicitudes() {
  const solicitudes = await solicitar('/administradores/solicitudes');
  lista.replaceChildren(...(solicitudes.length
    ? solicitudes.map(s => el('article', { className: 'tarjeta' },
        el('h4', { textContent: s.establecimiento }),
        el('p', { textContent: `Solicitud ${s.id} · ${s.documentos.length} documento(s)` }),
        el('button', { type: 'button', textContent: 'Revisar', onclick: () => abrirSolicitud(s.id) })))
    : [el('p', { className: 'vacio', textContent: 'No hay solicitudes pendientes.' })]));
}

function tarjetaDocumento(doc, solicitudId) {
  const tarjeta = el('article', { className: 'tarjeta' },
    el('h4', { textContent: doc.tipo.replaceAll('_', ' ') }),
    el('p', { textContent: doc.numero || 'Sin número de documento' }),
    el('p', { textContent: doc.resultado ? `Revisión: ${doc.resultado}` : 'Pendiente de revisión' }),
    el('a', { href: `/api/administradores/documentos/${doc.id}/archivo`, textContent: 'Descargar documento' }));
  if (doc.resultado) {
    if (doc.observaciones) tarjeta.append(el('p', { textContent: doc.observaciones }));
    return tarjeta;
  }
  const select = el('select', { name: 'resultado', required: true },
    el('option', { value: 'APROBADO', textContent: 'Aprobar documento' }),
    el('option', { value: 'RECHAZADO', textContent: 'Rechazar documento' }));
  const observaciones = el('textarea', { name: 'observaciones', maxLength: 500, rows: 3, placeholder: 'Motivo obligatorio al rechazar' });
  const form = el('form', {},
    el('label', {}, 'Resultado', select),
    el('label', {}, 'Observaciones', observaciones),
    el('button', { type: 'submit', textContent: 'Guardar revisión' }));
  form.addEventListener('submit', async evento => {
    evento.preventDefault();
    if (select.value === 'RECHAZADO' && !observaciones.value.trim()) {
      aviso('Indica por qué se rechaza el documento.', true); return;
    }
    try {
      await solicitar(`/administradores/documentos/${doc.id}/revision`, {
        method: 'POST', body: JSON.stringify({ resultado: select.value, observaciones: observaciones.value.trim() || null })
      });
      await abrirSolicitud(solicitudId);
      aviso('Revisión guardada.');
    } catch (error) { aviso(error.message, true); }
  });
  tarjeta.append(form);
  return tarjeta;
}

async function abrirSolicitud(id) {
  try {
    const s = await solicitar(`/administradores/solicitudes/${id}`);
    detalle.hidden = false;
    detalle.replaceChildren(
      el('h3', { textContent: s.establecimiento }),
      el('p', { textContent: `Solicitud ${s.id}` }),
      ...s.documentos.map(doc => tarjetaDocumento(doc, s.id))
    );
    if (s.documentos.length && s.documentos.every(doc => doc.resultado)) {
      const decision = el('select', { name: 'decision' },
        el('option', { value: 'APROBADA', textContent: 'Aprobar solicitud', disabled: s.documentos.some(doc => doc.resultado !== 'APROBADO') }),
        el('option', { value: 'RECHAZADA', textContent: 'Rechazar solicitud' }));
      if (s.documentos.some(doc => doc.resultado !== 'APROBADO')) decision.value = 'RECHAZADA';
      const motivo = el('textarea', { name: 'motivo', required: true, maxLength: 500, rows: 3 });
      const form = el('form', {},
        el('h4', { textContent: 'Decisión final' }),
        el('label', {}, 'Decisión', decision),
        el('label', {}, 'Motivo', motivo),
        el('button', { type: 'submit', textContent: 'Guardar decisión' }));
      form.addEventListener('submit', async evento => {
        evento.preventDefault();
        try {
          await solicitar(`/administradores/solicitudes/${s.id}/decision`, {
            method: 'POST', body: JSON.stringify({ decision: decision.value, motivo: motivo.value.trim() })
          });
          detalle.hidden = true;
          detalle.replaceChildren();
          await cargarSolicitudes();
          aviso('Decisión guardada.');
        } catch (error) { aviso(error.message, true); }
      });
      detalle.append(form);
    }
  } catch (error) { aviso(error.message, true); }
}

async function mostrar(datos) {
  panel.hidden = !datos;
  document.querySelector('#nombreAdmin').textContent = datos?.nombre || '';
  detalle.hidden = true;
  detalle.replaceChildren();
  if (datos) {
    try { await cargarSolicitudes(); }
    catch (error) { aviso(error.message, true); }
  } else lista.replaceChildren();
}

document.querySelector('#salirAdmin').addEventListener('click', async () => {
  try {
    await solicitar('/administradores/sesion', { method: 'DELETE' });
    location.href = 'acceso-administrador.html';
  } catch (error) { aviso(error.message, true); }
});

try { await mostrar(await solicitar('/administradores/sesion')); }
catch { location.replace('acceso-administrador.html'); }
