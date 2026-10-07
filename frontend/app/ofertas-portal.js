import { solicitar, el, dinero, fechaHora, tamanios, aviso } from './comun.js';
import { leerCarrito, guardarCarrito } from './portal.js';

const filtrosPermitidos = ['distrito', 'direccion', 'precioMaximo', 'calificacionMinima'];

export function iniciarOfertas(usuario, limite = null) {
  const formulario = document.querySelector('#filtros');
  const lista = document.querySelector('#lista');
  const detalle = document.querySelector('#detalle');
  const panelFiltros = document.querySelector('#panel-filtros');
  const alternarFiltros = document.querySelector('#alternar-filtros');
  const contadorFiltros = document.querySelector('#contador-filtros');
  let busquedaActual = 0;
  const valoresIniciales = new URLSearchParams(location.search);
  if (formulario) {
    for (const nombre of filtrosPermitidos) {
      if (valoresIniciales.has(nombre)) formulario.elements[nombre].value = valoresIniciales.get(nombre);
    }
    formulario.addEventListener('submit', e => {
      e.preventDefault();
      const params = parametros();
      history.replaceState(null, '', `${location.pathname}${params.size ? '?' + params : ''}`);
      panelFiltros.hidden = true;
      alternarFiltros.setAttribute('aria-expanded', 'false');
      actualizarContador(params);
      buscar(params);
    });
    alternarFiltros.addEventListener('click', () => {
      const abrir = panelFiltros.hidden;
      panelFiltros.hidden = !abrir;
      alternarFiltros.setAttribute('aria-expanded', String(abrir));
      if (abrir) formulario.elements.distrito.focus();
    });
    actualizarContador(parametros());
  }
  buscar(formulario ? parametros() : new URLSearchParams());

  function parametros() {
    const params = new URLSearchParams();
    for (const nombre of filtrosPermitidos) {
      const valor = formulario.elements[nombre].value.trim();
      if (valor) params.set(nombre, valor);
    }
    return params;
  }

  function actualizarContador(params) {
    contadorFiltros.textContent = params.size ? String(params.size) : '';
    contadorFiltros.hidden = !params.size;
  }

  async function buscar(params) {
    const turno = ++busquedaActual;
    lista.replaceChildren(el('p', { className: 'estado-vacio', textContent: 'Buscando packs disponibles…' }));
    try {
      const ofertas = await solicitar(`/ofertas${params.size ? '?' + params : ''}`);
      if (!lista.isConnected || turno !== busquedaActual) return;
      lista.replaceChildren(...(ofertas.length
        ? (limite ? ofertas.slice(0, limite) : ofertas).map(tarjeta)
        : [el('p', { className: 'estado-vacio', textContent: limite ? 'No tenemos productos por el momento.' : 'No hay packs con esos filtros. Prueba cambiando la búsqueda.' })]));
      aviso('');
    } catch (error) {
      if (!lista.isConnected || turno !== busquedaActual) return;
      lista.replaceChildren();
      aviso(error.message, true);
    }
  }

  function tarjeta(o) {
    const disponibles = o.variantes.filter(v => v.stockDisponible > 0);
    const desde = disponibles.length ? Math.min(...disponibles.map(v => Number(v.precioDescuento))) : null;
    const stock = disponibles.reduce((s, v) => s + v.stockDisponible, 0);
    return el('article', { className: 'tarjeta-pack' },
      el('div', { className: 'pack-visual', ariaHidden: 'true' }, el('span', { textContent: '✳' })),
      el('div', { className: 'pack-contenido' },
        el('p', { className: 'pack-local', textContent: `${o.establecimiento} · ${o.distrito}` }),
        el('h3', { textContent: o.pack }),
        el('p', { className: 'pack-calificacion', textContent: o.calificacion != null ? `★ ${Number(o.calificacion).toFixed(1)}` : 'Aún sin calificaciones' }),
        el('div', { className: 'pack-pie' },
          el('div', {}, el('strong', { textContent: desde == null ? 'Sin stock' : `Desde ${dinero(desde)}` }),
            el('small', { textContent: `Recojo hasta ${fechaHora(o.finRecojo)} · ${stock} disponibles` })),
          el('button', { type: 'button', className: 'pack-accion', textContent: 'Ver pack', onclick: () => abrirDetalle(o.id) }))));
  }

  async function abrirDetalle(id) {
    try {
      const o = await solicitar(`/ofertas/${id}`);
      if (!detalle.isConnected) return;
      const avisoDetalle = el('p', { className: 'detalle-aviso', role: 'status', ariaLive: 'polite' });
      const filas = o.variantes.map(v => el('label', { className: 'detalle-variante' },
        el('span', {}, el('strong', { textContent: tamanios[v.tamanio] || v.tamanio }),
          el('small', { textContent: `${dinero(v.precioDescuento)} · antes ${dinero(v.precioReferencial)} · quedan ${v.stockDisponible}` })),
        el('input', { type: 'number', min: 0, max: v.stockDisponible, value: 0, name: 'v' + v.id, disabled: v.stockDisponible === 0, ariaLabel: `Cantidad de ${tamanios[v.tamanio] || v.tamanio}` })));
      const form = el('form', { className: 'detalle-formulario' }, ...filas,
        el('button', { type: 'submit', textContent: 'Añadir al carrito' }), avisoDetalle);
      form.addEventListener('submit', e => {
        e.preventDefault();
        const lineas = o.variantes.map(v => ({ varianteId: v.id, cantidad: Number(form.elements['v' + v.id].value) }))
          .filter(l => l.cantidad > 0);
        if (!lineas.length || lineas.some(l => !Number.isInteger(l.cantidad))) {
          avisoDetalle.textContent = 'Elige al menos una unidad válida.'; return;
        }
        const previo = leerCarrito(usuario.id);
        if (previo && previo.ofertaId !== o.id) {
          avisoDetalle.replaceChildren('Tu carrito ya contiene otro pack. ', el('a', { href: 'compras.html', textContent: 'Revisa Mis compras' }));
          return;
        }
        const acumuladas = new Map(previo?.lineas.map(l => [l.varianteId, l.cantidad]) || []);
        for (const l of lineas) acumuladas.set(l.varianteId, (acumuladas.get(l.varianteId) || 0) + l.cantidad);
        if (o.variantes.some(v => (acumuladas.get(v.id) || 0) > v.stockDisponible)) {
          avisoDetalle.textContent = 'La cantidad supera las unidades disponibles.'; return;
        }
        guardarCarrito(usuario.id, { ofertaId: o.id, lineas: [...acumuladas].map(([varianteId, cantidad]) => ({ varianteId, cantidad })) });
        avisoDetalle.replaceChildren('Pack añadido. ', el('a', { href: 'compras.html', textContent: 'Ir a Mis compras' }));
        form.reset();
      });
      detalle.replaceChildren(
        el('div', { className: 'detalle-cabecera' },
          el('p', { className: 'etiqueta', textContent: 'PACK SORPRESA' }),
          el('button', { type: 'button', className: 'detalle-cerrar', ariaLabel: 'Cerrar detalle', textContent: '×', onclick: () => detalle.close() })),
        el('h2', { textContent: o.pack }),
        el('p', { className: 'pack-local', textContent: `${o.establecimiento} · ${o.direccion}, ${o.distrito}` }),
        el('p', { textContent: o.descripcion }),
        el('p', { textContent: `Podría incluir: ${o.posiblesProductos}` }),
        o.alergenos.length ? el('p', { className: 'alerta', textContent: `Alérgenos: ${o.alergenos.join(', ')}` }) : null,
        o.restricciones.length ? el('p', { textContent: `Restricciones: ${o.restricciones.join(', ')}` }) : null,
        el('p', { className: 'detalle-horario', textContent: `Recojo: ${fechaHora(o.inicioRecojo)} a ${fechaHora(o.finRecojo)}` }),
        form);
      detalle.showModal();
    } catch (error) { if (detalle.isConnected) aviso(error.message, true); }
  }
}
