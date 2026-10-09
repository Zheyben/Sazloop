import { solicitar, el } from './comun.js';

const rutas = [
  ['consumidor.html', 'Inicio', 'inicio'],
  ['catalogo.html', 'Productos', 'productos'],
  ['compras.html', 'Mi carrito', 'compras']
];
const trazos = {
  inicio: 'M3 10.5 12 3l9 7.5V21h-6v-7H9v7H3z',
  productos: 'M4 8h16v13H4z M3 8l2-5h14l2 5 M12 8v13',
  compras: 'M3 10h18l-2 10H5L3 10Z M7 10l4-7 M17 10l-4-7 M8 14v3 M12 14v3 M16 14v3',
  perfil: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8 M4 21v-2a8 8 0 0 1 16 0v2',
  campana: 'M6 16V11a6 6 0 1 1 12 0v5l2 2H4z M10 20a2 2 0 0 0 4 0',
  salir: 'M10 4H5v16h5 M14 7l5 5-5 5 M19 12H9',
  flecha: 'M5 12h14m-6-6 6 6-6 6'
};

export function icono(nombre) {
  const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
  svg.setAttribute('viewBox', '0 0 24 24');
  svg.setAttribute('aria-hidden', 'true');
  svg.setAttribute('class', 'icono-portal');
  const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
  path.setAttribute('d', trazos[nombre]);
  svg.append(path);
  return svg;
}

function simboloMarca() {
  const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
  svg.setAttribute('viewBox', '0 0 24 24');
  svg.setAttribute('aria-hidden', 'true');
  const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
  path.setAttribute('d', 'M12 2v20 M2 12h20 M4.9 4.9l14.2 14.2 M19.1 4.9 4.9 19.1');
  svg.append(path);
  return svg;
}

function claveCarrito(usuarioId) { return `sazloop-carrito-${usuarioId}`; }

export function leerCarrito(usuarioId) {
  try {
    const dato = JSON.parse(sessionStorage.getItem(claveCarrito(usuarioId)) || 'null');
    const pedidos = Array.isArray(dato) ? dato : dato?.lineas ? [dato] : [];
    return pedidos.map((pedido, indice) => ({
      clave: typeof pedido.clave === 'string' ? pedido.clave : `anterior-${indice}`,
      ofertaId: pedido.ofertaId,
      lineas: Array.isArray(pedido.lineas) ? pedido.lineas.filter(linea =>
        Number.isInteger(linea.varianteId) && Number.isInteger(linea.cantidad) && linea.cantidad > 0) : []
    })).filter(pedido => Number.isInteger(pedido.ofertaId) && pedido.lineas.length);
  } catch { return []; }
}

export function guardarCarrito(usuarioId, pedidos) {
  if (pedidos?.length) sessionStorage.setItem(claveCarrito(usuarioId), JSON.stringify(pedidos));
  else sessionStorage.removeItem(claveCarrito(usuarioId));
  const contador = document.querySelector('#contador-carrito');
  if (contador) {
    const cantidad = pedidos?.length || 0;
    contador.textContent = cantidad ? String(cantidad) : '';
    contador.hidden = !cantidad;
  }
}

/** Muestra en la campana cuántas notificaciones siguen sin leer. */
export function actualizarCampana(avisos) {
  const contador = document.querySelector('#contador-avisos');
  if (!contador) return;
  const pendientes = avisos.filter(aviso => !aviso.leida).length;
  contador.textContent = pendientes ? String(pendientes) : '';
  contador.hidden = !pendientes;
}

export function actualizarNav(activa) {
  document.querySelectorAll('.portal-enlaces a, .portal-campana').forEach(enlace => {
    if (new URL(enlace.href).pathname.endsWith('/' + activa)) enlace.setAttribute('aria-current', 'page');
    else enlace.removeAttribute('aria-current');
  });
  const perfil = document.querySelector('.portal-perfil');
  if (perfil) perfil.open = false;
}

export async function iniciarPortal(activa) {
  let usuario;
  try { usuario = await solicitar('/sesion'); }
  catch { location.replace('acceso-consumidor.html'); return null; }

  const nav = document.querySelector('#portal-nav');
  const iniciales = [usuario.nombres, usuario.apellidos].map(s => s?.trim().charAt(0).toUpperCase()).join('');
  const nombre = usuario.nombres.trim().split(/\s+/)[0];
  const enlaces = el('nav', { className: 'portal-enlaces', ariaLabel: 'Navegación del consumidor' },
    ...rutas.map(([href, texto, nombreIcono]) => {
      const enlace = el('a', { href }, icono(nombreIcono), el('span', { textContent: texto }));
      if (activa === href) enlace.setAttribute('aria-current', 'page');
      if (nombreIcono === 'compras') {
        const cantidad = leerCarrito(usuario.id).length;
        enlace.append(el('span', { id: 'contador-carrito', className: 'contador-carrito', textContent: cantidad ? String(cantidad) : '', hidden: !cantidad }));
      }
      return enlace;
    }));
  const avatar = usuario.tieneFoto
    ? el('img', { className: 'avatar-foto', src: '/api/perfil/foto', alt: '' })
    : el('span', { className: 'avatar', textContent: iniciales });
  const perfil = el('details', { className: 'portal-perfil' },
    el('summary', {}, avatar, el('span', { className: 'nombre-perfil', textContent: nombre })),
    el('div', { className: 'menu-perfil' },
      el('a', { href: 'perfil.html' }, icono('perfil'), 'Mi perfil'),
      el('button', { type: 'button', id: 'salir' }, icono('salir'), 'Cerrar sesión')));
  const campana = el('a', { href: 'notificaciones.html', className: 'portal-campana', ariaLabel: 'Notificaciones', title: 'Notificaciones' },
    icono('campana'), el('span', { id: 'contador-avisos', className: 'contador-carrito', hidden: true }));
  if (activa === 'notificaciones.html') campana.setAttribute('aria-current', 'page');
  nav.replaceChildren(el('div', { className: 'portal-barra' },
    el('a', { href: 'consumidor.html', className: 'portal-marca', ariaLabel: 'Sazloop, ir al inicio' },
      el('span', { className: 'marca-icono' }, simboloMarca()), 'Sazloop', el('span', { className: 'marca-punto', textContent: '.' })),
    enlaces, el('div', { className: 'portal-derecha' }, campana, perfil)));
  solicitar('/notificaciones').then(actualizarCampana).catch(() => { /* el contador es informativo */ });
  perfil.querySelector('#salir').addEventListener('click', async () => {
    try {
      await solicitar('/sesion', { method: 'DELETE' });
      guardarCarrito(usuario.id, null);
      location.href = 'acceso-consumidor.html';
    } catch {
      perfil.open = false;
    }
  });
  document.addEventListener('click', e => { if (!perfil.contains(e.target)) perfil.open = false; });
  return usuario;
}
