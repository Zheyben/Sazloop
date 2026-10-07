import { solicitar, el } from './comun.js';

const rutas = [
  ['consumidor.html', 'Inicio', 'inicio'],
  ['catalogo.html', 'Productos', 'productos'],
  ['compras.html', 'Mis compras', 'compras']
];
const trazos = {
  inicio: 'M3 10.5 12 3l9 7.5V21h-6v-7H9v7H3z',
  productos: 'M4 8h16v13H4z M3 8l2-5h14l2 5 M12 8v13',
  compras: 'M4 4h2l2 11h11l2-8H7 M9 20h.01 M18 20h.01',
  perfil: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8 M4 21v-2a8 8 0 0 1 16 0v2',
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

function claveCarrito(usuarioId) { return `sazloop-carrito-${usuarioId}`; }

export function leerCarrito(usuarioId) {
  try {
    const dato = JSON.parse(sessionStorage.getItem(claveCarrito(usuarioId)) || 'null');
    if (!dato || !Number.isInteger(dato.ofertaId) || !Array.isArray(dato.lineas)) return null;
    const lineas = dato.lineas.filter(l => Number.isInteger(l.varianteId) && Number.isInteger(l.cantidad) && l.cantidad > 0);
    return lineas.length ? { ofertaId: dato.ofertaId, lineas } : null;
  } catch { return null; }
}

export function guardarCarrito(usuarioId, carrito) {
  if (carrito?.lineas.length) sessionStorage.setItem(claveCarrito(usuarioId), JSON.stringify(carrito));
  else sessionStorage.removeItem(claveCarrito(usuarioId));
  const contador = document.querySelector('#contador-carrito');
  if (contador) {
    const cantidad = carrito?.lineas.reduce((total, l) => total + l.cantidad, 0) || 0;
    contador.textContent = cantidad ? String(cantidad) : '';
    contador.hidden = !cantidad;
  }
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
        const cantidad = leerCarrito(usuario.id)?.lineas.reduce((total, l) => total + l.cantidad, 0) || 0;
        enlace.append(el('span', { id: 'contador-carrito', className: 'contador-carrito', textContent: cantidad ? String(cantidad) : '', hidden: !cantidad }));
      }
      return enlace;
    }));
  const perfil = el('details', { className: 'portal-perfil' },
    el('summary', {}, el('span', { className: 'avatar', textContent: iniciales }), el('span', { className: 'nombre-perfil', textContent: nombre })),
    el('div', { className: 'menu-perfil' },
      el('a', { href: 'perfil.html' }, icono('perfil'), 'Mi perfil'),
      el('button', { type: 'button', id: 'salir' }, icono('salir'), 'Cerrar sesión')));
  nav.replaceChildren(el('div', { className: 'portal-barra' },
    el('a', { href: 'consumidor.html', className: 'portal-marca', ariaLabel: 'Sazloop, ir al inicio' },
      el('span', { className: 'marca-icono', textContent: '✳' }), 'Sazloop', el('span', { className: 'marca-punto', textContent: '.' })),
    enlaces, perfil));
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
