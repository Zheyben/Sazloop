// comun.js — piezas compartidas por todas las páginas (módulo ES).
const API = '/api';

// Hace la llamada al backend. Siempre manda la cookie de sesión (credentials: 'include').
export async function solicitar(ruta, opciones = {}) {
  const esFormData = opciones.body instanceof FormData;
  const r = await fetch(API + ruta, {
    ...opciones,
    credentials: 'include',
    headers: { ...(esFormData || !opciones.body ? {} : { 'Content-Type': 'application/json' }), ...opciones.headers }
  });
  if (!r.ok) {
    // El backend no envía mensaje de error, así que traducimos por código HTTP.
    const textos = {
      400: 'Revisa los datos ingresados.',
      401: 'Necesitas iniciar sesión para hacer esto.',
      403: 'No tienes permiso para esta acción.',
      404: 'No se encontró lo que buscabas.',
      409: 'La operación no se pudo completar (por ejemplo, sin stock o ya existe).'
    };
    const error = new Error(textos[r.status] || 'Ocurrió un error inesperado. Intenta de nuevo.');
    error.status = r.status;
    throw error;
  }
  return r.status === 204 ? null : r.json();
}

// Crea elementos sin usar innerHTML (así evitamos inyección de HTML con datos del servidor).
export function el(etiqueta, props = {}, ...hijos) {
  const nodo = Object.assign(document.createElement(etiqueta), props);
  nodo.append(...hijos.filter(h => h !== null && h !== undefined));
  return nodo;
}

export const dinero = n => `S/ ${Number(n).toFixed(2)}`;
export const fechaHora = s => new Date(s).toLocaleString('es-PE', { dateStyle: 'medium', timeStyle: 'short' });
export const tamanios = { PEQUENO: 'Pequeño', MEDIANO: 'Mediano', GRANDE: 'Grande' };

// Aviso arriba de la página (verde = ok, rojo = error).
export function aviso(texto, esError = false) {
  const zona = document.querySelector('#mensaje');
  zona.textContent = texto;
  zona.className = esError ? 'error' : 'ok';
}

// Barra de navegación: muestra al usuario si hay sesión.
export async function pintarNav(activa) {
  const nav = document.querySelector('#nav');
  const enlaces = [['catalogo.html', 'Explorar ofertas'], ['compras.html', 'Mis compras'], ['consumidor.html', 'Mi cuenta']];
  nav.replaceChildren(
    el('a', { href: '../index.html', className: 'marca', textContent: '✳ Sazloop.' }),
    ...enlaces.map(([href, texto]) => el('a', { href, textContent: texto, ariaCurrent: href === activa ? 'page' : null }))
  );
  try {
    const u = await solicitar('/sesion');
    nav.append(el('span', { className: 'quien', textContent: u.nombres }));
  } catch {
    nav.append(el('a', { href: 'acceso-consumidor.html', className: 'quien', textContent: 'Ingresar' }));
  }
}
