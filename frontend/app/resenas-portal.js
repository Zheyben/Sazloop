import { solicitar, el } from './comun.js';

function dialogoResena() {
  const existente = document.querySelector('#dialogo-resena');
  if (existente) return existente;
  const cerrar = el('button', { type: 'button', ariaLabel: 'Cerrar sin calificar esta compra', textContent: '×' });
  const titulo = el('h2', { id: 'titulo-resena' });
  const calificacion = el('select', { name: 'calificacion', required: true },
    el('option', { value: '', textContent: 'Selecciona una calificación' }),
    ...[1, 2, 3, 4, 5].map(n => el('option', { value: String(n), textContent: `${n} ${n === 1 ? 'estrella' : 'estrellas'}` })));
  const form = el('form', {},
    el('label', {}, 'Calificación', calificacion),
    el('label', {}, 'Comentario (opcional)', el('textarea', { name: 'comentario', maxLength: 1000 })),
    el('p', { id: 'mensaje-resena', role: 'status', ariaLive: 'polite' }),
    el('button', { type: 'submit', textContent: 'Enviar reseña' }));
  const dialogo = el('dialog', { id: 'dialogo-resena', ariaLabelledBy: 'titulo-resena' }, cerrar, titulo, form);
  document.body.append(dialogo);
  return dialogo;
}

export function abrirResena(compra, alTerminar = () => {}) {
  const dialogo = dialogoResena();
  if (dialogo.open) return;
  const form = dialogo.querySelector('form');
  const mensaje = dialogo.querySelector('#mensaje-resena');
  const cerrar = dialogo.querySelector('button[type="button"]');
  const enviar = form.querySelector('button[type="submit"]');
  dialogo.querySelector('#titulo-resena').textContent = `¿Cómo calificarías el producto de ${compra.establecimiento}?`;
  form.reset();
  mensaje.textContent = '';
  let enviando = false;

  async function terminar(ruta, datos) {
    if (enviando) return;
    enviando = true;
    cerrar.disabled = true;
    enviar.disabled = true;
    try {
      await solicitar(ruta, { method: 'POST', ...(datos ? { body: JSON.stringify(datos) } : {}) });
      dialogo.close();
      await alTerminar();
    } catch (error) {
      mensaje.textContent = error.message;
    } finally {
      enviando = false;
      cerrar.disabled = false;
      enviar.disabled = false;
    }
  }
  cerrar.onclick = () => terminar(`/compras/${compra.reservaId}/resena/omitir`);
  dialogo.oncancel = evento => { evento.preventDefault(); cerrar.click(); };
  form.onsubmit = evento => {
    evento.preventDefault();
    terminar(`/compras/${compra.reservaId}/resena`, {
      calificacion: Number(form.elements.calificacion.value),
      comentario: form.elements.comentario.value.trim()
    });
  };
  dialogo.showModal();
}

export async function avisarResenasPendientes() {
  try {
    const compras = await solicitar('/compras/mias');
    const vistas = new Set();
    const pendientes = compras.filter(compra => {
      if (!compra.resenaPendiente || vistas.has(compra.establecimientoId)) return false;
      vistas.add(compra.establecimientoId);
      return true;
    });
    function siguiente() {
      const compra = pendientes.shift();
      if (compra) abrirResena(compra, siguiente);
    }
    siguiente();
  } catch { /* El historial conserva el acceso manual si la consulta falla. */ }
}
