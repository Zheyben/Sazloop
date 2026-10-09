// panel.js — piezas compartidas por los portales de trabajador, empresa y administración.
import { el } from './comun.js';

const trazos = {
  marca: 'M12 2v20 M2 12h20 M4.9 4.9l14.2 14.2 M19.1 4.9 4.9 19.1',
  pack: 'M4 8h16v13H4z M3 8l2-5h14l2 5 M12 8v13',
  crear: 'M12 5v14 M5 12h14',
  validar: 'M4 7V4h3 M17 4h3v3 M20 17v3h-3 M7 20H4v-3 M8 12l3 3 5-6',
  resumen: 'M4 20V10 M10 20V4 M16 20v-7 M22 20H2',
  locales: 'M3 10l1.5-6h15L21 10 M4 10v10h16V10 M9 20v-6h6v6 M3 10h18',
  campana: 'M6 16V11a6 6 0 1 1 12 0v5l2 2H4z M10 20a2 2 0 0 0 4 0',
  solicitudes: 'M7 3h7l5 5v13H7z M14 3v5h5 M10 13h6 M10 17h6',
  edificio: 'M4 21V5l8-3v19 M12 8h8v13 M8 9v.01 M8 13v.01 M8 17v.01 M16 12v.01 M16 16v.01',
  salir: 'M10 4H5v16h5 M14 7l5 5-5 5 M19 12H9',
  descarga: 'M12 4v11 M7 10l5 5 5-5 M5 20h14'
};

export function icono(nombre) {
  const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
  svg.setAttribute('viewBox', '0 0 24 24');
  svg.setAttribute('aria-hidden', 'true');
  const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
  path.setAttribute('d', trazos[nombre]);
  svg.append(path);
  return svg;
}

/** Coloca el símbolo de la marca y los iconos declarados con data-icono en el HTML. */
export function pintarIconos() {
  for (const zona of document.querySelectorAll('[data-icono]')) zona.prepend(icono(zona.dataset.icono));
}

/** Navegación del menú lateral: muestra una vista y marca su botón. */
export function crearNavegacion(alMostrar) {
  const botones = [...document.querySelectorAll('.panel-lateral [data-vista]')];
  function mostrar(vista, datos) {
    const raiz = vista.split(':')[0];
    for (const seccion of document.querySelectorAll('[data-panel-vista]')) {
      seccion.hidden = seccion.dataset.panelVista !== vista;
    }
    for (const boton of botones) {
      if (boton.dataset.vista === raiz) boton.setAttribute('aria-current', 'page');
      else boton.removeAttribute('aria-current');
    }
    mensaje('');
    scrollTo({ top: 0 });
    alMostrar?.(vista, datos);
  }
  for (const boton of botones) boton.addEventListener('click', () => mostrar(boton.dataset.vista));
  return mostrar;
}

/** Mensaje general de la vista (verde para confirmaciones, rojo para errores). */
export function mensaje(texto, esError = false) {
  const zona = document.querySelector('#mensaje');
  zona.textContent = texto;
  zona.className = texto ? (esError ? 'error' : 'ok') : '';
}

export function estadoEn(zona, texto, esError = false) {
  zona.textContent = texto;
  zona.className = `estado-formulario ${esError ? 'error' : 'ok'}`;
}

export function insignia(texto, tono = '') {
  return el('span', { className: `insignia ${tono}`.trim(), textContent: texto });
}

export function banda(tono, titulo, texto, ...extra) {
  return el('div', { className: `banda ${tono}`, role: tono === 'peligro' ? 'alert' : 'status' },
    el('span', { className: 'banda-icono', ariaHidden: 'true', textContent: tono === 'info' ? 'i' : '!' }),
    el('div', {}, el('strong', { textContent: titulo }), texto ? el('p', { textContent: texto }) : null, ...extra));
}

export function vacio(titulo, texto, ...acciones) {
  return el('div', { className: 'vacio-panel' },
    el('strong', { textContent: titulo }), texto ? el('p', { textContent: texto }) : null,
    acciones.length ? el('p', {}, ...acciones) : null);
}

export function cifra(etiqueta, valor, detalle = '', destacada = false) {
  return el('div', { className: `cifra${destacada ? ' destacada' : ''}` },
    el('span', { textContent: etiqueta }), el('strong', { textContent: valor }),
    detalle ? el('small', { textContent: detalle }) : null);
}

export function tabla(columnas, filas, pie = null) {
  const encabezado = el('tr', {}, ...columnas.map(([texto, clase]) =>
    el('th', { scope: 'col', textContent: texto, className: clase || '' })));
  const cuerpo = filas.map(fila => el('tr', {}, ...fila.map((valor, i) => {
    const celda = el('td', { className: columnas[i][1] || '' });
    if (valor instanceof Node) celda.append(valor); else celda.textContent = valor ?? '—';
    return celda;
  })));
  const piePagina = pie ? el('tfoot', {}, el('tr', {}, ...pie.map((valor, i) =>
    el('td', { className: columnas[i][1] || '', textContent: valor ?? '' })))) : null;
  return el('div', { className: 'tabla-contenedor' },
    el('table', { className: 'tabla-panel' }, el('thead', {}, encabezado), el('tbody', {}, ...cuerpo), piePagina));
}

export const fecha = s => s ? new Date(s + (s.length === 10 ? 'T00:00:00' : '')).toLocaleDateString('es-PE',
  { day: 'numeric', month: 'short', year: 'numeric' }) : '—';
export const hora = s => new Date(s).toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit', hour12: false });

export function rangoHorario(inicio, fin) {
  const a = new Date(inicio);
  const b = new Date(fin);
  return a.toDateString() === b.toDateString()
    ? `${fecha(inicio)} · ${hora(inicio)} – ${hora(fin)}`
    : `${fecha(inicio)} ${hora(inicio)} – ${fecha(fin)} ${hora(fin)}`;
}

export const tiposDocumento = {
  LICENCIA_FUNCIONAMIENTO: 'Licencia de funcionamiento',
  CERTIFICADO_SANITARIO: 'Certificado sanitario',
  INSPECCION_SEGURIDAD: 'Inspección de seguridad',
  OTRO: 'Otro documento'
};

export const estadosLocal = {
  PENDIENTE_APROBACION: ['Pendiente de aprobación', 'alerta'],
  ACTIVO: ['Activo', 'ok'],
  SUSPENDIDO: ['Suspendido', 'peligro'],
  INACTIVO: ['Inactivo', '']
};

export function insigniaLocal(estado) {
  const [texto, tono] = estadosLocal[estado] || [estado, ''];
  return insignia(texto, tono);
}

export const vigencias = {
  VIGENTE: ['Vigente', 'ok'],
  POR_VENCER: ['Por vencer', 'alerta'],
  VENCIDO: ['Vencido', 'peligro'],
  SIN_VENCIMIENTO: ['Sin vencimiento', 'info']
};

/** Filas de dieta con el mismo criterio que la ficha del consumidor. */
export function filasDieta(restricciones = []) {
  return el('div', { className: 'dieta' }, ...[
    ['Apto para vegetarianos', 'No apto para vegetarianos'],
    ['Apto para veganos', 'No apto para veganos'],
    ['Apto para personas con celiaquía', 'No apto para personas con celiaquía']
  ].map(([si, no]) => {
    const apto = restricciones.includes(si);
    return el('p', { className: apto ? 'apto' : 'no-apto' },
      el('span', { className: 'estado-dieta', ariaHidden: 'true', textContent: apto ? '✓' : '✕' }), apto ? si : no);
  }));
}

/** Lista de avisos con acciones de lectura; devuelve la cantidad pendiente al terminar. */
export function listaAvisos(avisos, { alLeer, alLeerTodas }) {
  if (!avisos.length) return vacio('No tienes notificaciones', 'Aquí verás los avisos sobre tus solicitudes, documentos y pedidos.');
  const pendientes = avisos.filter(a => !a.leida).length;
  const lista = el('ul', { className: 'lista-avisos' }, ...avisos.map(aviso => {
    const boton = aviso.leida ? null : el('button', { type: 'button', className: 'secundario', textContent: 'Marcar como leída',
      onclick: () => alLeer(aviso) });
    return el('li', { className: `aviso-item${aviso.leida ? '' : ' no-leida'}` },
      el('span', { className: 'punto', ariaHidden: 'true' }),
      el('strong', { textContent: aviso.titulo }),
      el('p', { textContent: aviso.mensaje }),
      el('div', { className: 'pie-aviso' },
        el('span', { textContent: new Date(aviso.fecha).toLocaleString('es-PE', { dateStyle: 'medium', timeStyle: 'short' }) }),
        boton));
  }));
  return el('div', {},
    pendientes ? el('div', { className: 'acciones-formulario', style: 'margin: 0 0 1rem' },
      el('button', { type: 'button', className: 'secundario', textContent: 'Marcar todas como leídas', onclick: alLeerTodas })) : null,
    lista);
}
