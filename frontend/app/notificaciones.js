import { solicitar, el, aviso } from './comun.js';
import { actualizarCampana } from './portal.js';

export function montarNotificaciones() {
  cargar();
}

async function cargar() {
  const zona = document.querySelector('#avisos-consumidor');
  try {
    const avisos = await solicitar('/notificaciones');
    if (!zona.isConnected) return;
    actualizarCampana(avisos);
    if (!avisos.length) {
      zona.replaceChildren(el('div', { className: 'estado-vacio' },
        el('p', { textContent: 'No tienes notificaciones.' }),
        el('p', { textContent: 'Al confirmar una compra recibirás aquí el recordatorio de recojo con tu código.' })));
      return;
    }
    zona.replaceChildren(
      avisos.some(a => !a.leida) ? el('div', { className: 'avisos-acciones' },
        el('button', { type: 'button', className: 'secundario', textContent: 'Marcar todas como leídas',
          onclick: () => marcar('/notificaciones/leer-todas') })) : null,
      el('ul', { className: 'lista-avisos' }, ...avisos.map(a => el('li', { className: `aviso-item${a.leida ? '' : ' no-leida'}` },
        el('strong', { textContent: a.titulo }),
        el('p', { textContent: a.mensaje }),
        el('div', { className: 'pie-aviso' },
          el('span', { textContent: new Date(a.fecha).toLocaleString('es-PE', { dateStyle: 'medium', timeStyle: 'short' }) }),
          a.leida ? null : el('button', { type: 'button', className: 'secundario', textContent: 'Marcar como leída',
            onclick: () => marcar(`/notificaciones/${a.id}/leer`) }))))));
    aviso('');
  } catch (error) { if (zona.isConnected) aviso(error.message, true); }
}

async function marcar(ruta) {
  try {
    await solicitar(ruta, { method: 'POST' });
    await cargar();
  } catch (error) { aviso(error.message, true); }
}
