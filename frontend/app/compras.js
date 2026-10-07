import { solicitar, el, dinero, fechaHora, aviso, pintarNav } from './comun.js';

const estados = { PENDIENTE_PAGO: 'Pendiente de pago', CONFIRMADA: 'Lista para recoger',
  RECOGIDA: 'Recogida', CANCELADA: 'Cancelada', EXPIRADA: 'Expirada' };

pintarNav('compras.html');

try {
  const compras = await solicitar('/compras/mias');
  document.querySelector('#lista').replaceChildren(...(compras.length
    ? compras.map(c => el('article', { className: 'tarjeta' },
        el('h2', { textContent: estados[c.estado] || c.estado }),
        el('p', { className: 'codigo', textContent: c.codigoRecojo }),
        el('p', { textContent: `${dinero(c.total)} · ${c.medioPago}` }),
        el('p', { className: 'chico', textContent: `Boleta ${c.numeroBoleta} · Recojo ${fechaHora(c.inicioRecojo)} a ${fechaHora(c.finRecojo)}` })))
    : [el('p', { className: 'vacio' }, 'Aún no compras nada. ', el('a', { href: 'catalogo.html', textContent: 'Ver ofertas' }))]));
} catch (err) {
  aviso(err.status === 401 ? 'Inicia sesión desde "Mi cuenta" para ver tus compras.' : err.message, true);
}
