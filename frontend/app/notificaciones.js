import { solicitar, el } from './comun.js';

const volver = document.querySelector('#volver');
const params = new URLSearchParams(location.search);
if (params.get('origen') === 'empresa') volver.href = 'negocio.html#empresa';
const lista = document.querySelector('#avisos');
const mensaje = document.querySelector('#mensaje');
try {
  const avisos = await solicitar('/notificaciones');
  if (!avisos.length) mensaje.textContent = 'No tienes notificaciones.';
  for (const aviso of avisos) {
    const boton = el('button', { type: 'button', textContent: 'Marcar como leída' });
    boton.hidden = aviso.leida;
    boton.addEventListener('click', async () => {
      try {
        await solicitar(`/notificaciones/${aviso.id}/leer`, { method: 'POST' });
        boton.hidden = true;
      } catch (error) { mensaje.textContent = error.message; }
    });
    lista.append(el('li', {},
      el('strong', { textContent: aviso.titulo }),
      el('p', { textContent: aviso.mensaje }),
      el('small', { textContent: new Date(aviso.fecha).toLocaleString('es-PE') }), boton));
  }
} catch { location.replace('acceso.html'); }
