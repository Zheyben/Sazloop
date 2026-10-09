import { iniciarPortal, actualizarNav } from './portal.js';
import { montarInicio } from './consumidor.js';
import { montarCatalogo } from './catalogo.js';
import { montarCompras } from './compras.js';
import { avisarResenasPendientes } from './resenas-portal.js';
import { montarPerfil } from './perfil.js';

const vistas = new Map([
  ['consumidor.html', montarInicio],
  ['catalogo.html', montarCatalogo],
  ['compras.html', montarCompras],
  ['perfil.html', montarPerfil]
]);
const paginaInicial = location.pathname.split('/').pop();
const usuario = await iniciarPortal(paginaInicial);
let navegacion = 0;

if (usuario) {
  vistas.get(paginaInicial)?.(usuario);
  avisarResenasPendientes();

  document.addEventListener('click', evento => {
    if (evento.defaultPrevented || evento.button !== 0 || evento.metaKey || evento.ctrlKey || evento.shiftKey || evento.altKey) return;
    const enlace = evento.target.closest('a[href]');
    if (!enlace || enlace.target || enlace.hasAttribute('download')) return;
    const destino = new URL(enlace.href);
    const pagina = destino.pathname.split('/').pop();
    if (destino.origin !== location.origin || destino.pathname.slice(0, destino.pathname.lastIndexOf('/')) !== location.pathname.slice(0, location.pathname.lastIndexOf('/')) || !vistas.has(pagina)) return;
    evento.preventDefault();
    if (destino.href !== location.href) navegar(destino, true);
  });

  addEventListener('popstate', () => navegar(new URL(location.href), false));
}

async function navegar(destino, guardarHistorial) {
  const turno = ++navegacion;
  try {
    const respuesta = await fetch(destino.href, { credentials: 'same-origin' });
    if (!respuesta.ok) throw new Error('No se pudo abrir la vista.');
    const pagina = new DOMParser().parseFromString(await respuesta.text(), 'text/html');
    const principal = pagina.querySelector('main.portal-principal');
    if (!principal) throw new Error('Falta el contenido de la vista.');
    if (turno !== navegacion) return;

    document.querySelector('main.portal-principal').replaceWith(document.importNode(principal, true));
    document.querySelector('#detalle')?.remove();
    const detalle = pagina.querySelector('#detalle');
    if (detalle) document.body.append(document.importNode(detalle, true));
    document.title = pagina.title;
    if (guardarHistorial) history.pushState(null, '', destino.href);
    const nombre = destino.pathname.split('/').pop();
    actualizarNav(nombre);
    scrollTo(0, 0);
    vistas.get(nombre)(usuario);
  } catch {
    if (turno === navegacion) location.assign(destino.href);
  }
}
