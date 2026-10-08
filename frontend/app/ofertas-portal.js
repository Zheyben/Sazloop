import { solicitar, el, dinero, tamanios, aviso } from './comun.js';
import { leerCarrito, guardarCarrito } from './portal.js';

const filtrosPermitidos = ['distrito', 'direccion', 'precioMaximo', 'calificacionMinima'];

function hora(s) {
  return new Date(s).toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit', hour12: false });
}

function fechaBreve(s) {
  const fecha = new Date(s);
  const hoy = new Date();
  return fecha.toDateString() === hoy.toDateString()
    ? 'hoy'
    : fecha.toLocaleDateString('es-PE', { day: 'numeric', month: 'short' });
}

function horarioRecojo(o) {
  const inicio = new Date(o.inicioRecojo);
  const fin = new Date(o.finRecojo);
  const otroDia = inicio.toDateString() !== fin.toDateString();
  return `Recojo ${fechaBreve(o.inicioRecojo)} · ${hora(o.inicioRecojo)} – ${otroDia ? `${fechaBreve(o.finRecojo)} · ` : ''}${hora(o.finRecojo)}`;
}

function horarioRecojoDestacado(o) {
  const texto = horarioRecojo(o);
  const separador = texto.indexOf(' · ');
  return [el('strong', { textContent: texto.slice(0, separador) }), texto.slice(separador)];
}

function nombreDestacado(nombre) {
  const espacio = nombre.indexOf(' ');
  return espacio < 0 ? [nombre] : [nombre.slice(0, espacio), ' ', el('em', { textContent: nombre.slice(espacio + 1) })];
}

function iconoDato(tipo) {
  const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
  svg.setAttribute('viewBox', '0 0 24 24');
  svg.setAttribute('aria-hidden', 'true');
  svg.setAttribute('class', 'pack-icono-dato');
  const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
  path.setAttribute('d', tipo === 'ubicacion'
    ? 'M20 10c0 5-8 11-8 11S4 15 4 10a8 8 0 1 1 16 0Zm-8 3a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z'
    : 'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20Zm0-16v6l4 2');
  svg.append(path);
  return svg;
}

function descuento(v) {
  return Math.round((1 - Number(v.precioDescuento) / Number(v.precioReferencial)) * 100);
}

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
    const masBarata = disponibles.reduce((menor, v) => !menor || Number(v.precioDescuento) < Number(menor.precioDescuento) ? v : menor, null);
    const stock = disponibles.reduce((s, v) => s + v.stockDisponible, 0);
    return el('article', { className: 'tarjeta-pack' },
      el('div', { className: 'pack-visual' },
        el('span', { className: 'pack-visual-marca', ariaHidden: 'true', textContent: '✳' }),
        el('span', { className: 'pack-visual-stock', textContent: `${stock} uds.` }),
        o.calificacion != null ? el('span', { className: 'pack-visual-calificacion', textContent: `★  ${Number(o.calificacion).toFixed(1)}` }) : null),
      el('div', { className: 'pack-contenido' },
        el('p', { className: 'pack-local' }, iconoDato('ubicacion'), `${o.establecimiento} · ${o.distrito}`),
        el('h3', {}, ...nombreDestacado(o.pack)),
        el('p', { className: 'pack-horario' }, iconoDato('reloj'), horarioRecojo(o)),
        el('div', { className: 'pack-posibles' },
          el('span', { className: 'pack-dato-etiqueta', textContent: 'PODRÍA INCLUIR' }),
          el('p', { textContent: o.posiblesProductos })),
        el('div', { className: 'pack-pie' },
          el('div', { className: 'pack-precio' },
            el('div', { className: 'pack-precio-encabezado' },
              el('span', { className: 'pack-dato-etiqueta', textContent: 'DESDE' }),
              masBarata ? el('span', { className: 'pack-descuento', textContent: `−${descuento(masBarata)} %` }) : null),
            el('div', { className: 'pack-precio-cifras' },
              el('strong', { textContent: masBarata ? dinero(masBarata.precioDescuento) : 'Sin stock' }),
              masBarata ? el('s', { textContent: dinero(masBarata.precioReferencial) }) : null)),
          el('button', { type: 'button', className: 'pack-accion', textContent: 'Ver pack', onclick: () => abrirDetalle(o.id) }))));
  }

  async function abrirDetalle(id) {
    try {
      const o = await solicitar(`/ofertas/${id}`);
      if (!detalle.isConnected) return;
      const avisoDetalle = el('p', { className: 'detalle-aviso', role: 'status', ariaLive: 'polite' });
      const total = el('strong', { className: 'detalle-total', ariaLive: 'polite' });
      const primerDisponible = o.variantes.find(v => v.stockDisponible > 0);
      const filas = o.variantes.map(v => {
        const campo = el('input', { type: 'number', min: 0, max: v.stockDisponible,
          value: v.id === primerDisponible?.id ? 1 : 0, name: 'v' + v.id,
          disabled: v.stockDisponible === 0, ariaLabel: `Cantidad de ${tamanios[v.tamanio] || v.tamanio}` });
        const fila = el('div', { className: 'detalle-variante' },
          el('div', { className: 'detalle-variante-info' },
            el('strong', { textContent: tamanios[v.tamanio] || v.tamanio }),
            el('span', {}, el('span', { className: 'detalle-variante-precio', textContent: dinero(v.precioDescuento) }),
              ' ', el('s', { textContent: dinero(v.precioReferencial) })),
            el('small', { textContent: `${v.stockDisponible} disponibles` })),
          el('div', { className: 'detalle-cantidad' },
            el('button', { type: 'button', className: 'detalle-cantidad-boton detalle-menos', ariaLabel: `Quitar una unidad de ${tamanios[v.tamanio] || v.tamanio}`, disabled: v.stockDisponible === 0,
              onclick: () => { campo.value = Math.max(0, Math.min(v.stockDisponible, Math.trunc(Number(campo.value) || 0) - 1)); actualizarTotal(); } }),
            campo,
            el('button', { type: 'button', className: 'detalle-cantidad-boton detalle-mas', ariaLabel: `Añadir una unidad de ${tamanios[v.tamanio] || v.tamanio}`, disabled: v.stockDisponible === 0,
              onclick: () => { campo.value = Math.min(v.stockDisponible, Math.max(0, Math.trunc(Number(campo.value) || 0)) + 1); actualizarTotal(); } })));
        return fila;
      });
      const form = el('form', { id: 'formulario-detalle-pack', className: 'detalle-formulario' }, ...filas);
      function actualizarTotal() {
        const lineas = o.variantes.map(v => ({ variante: v, cantidad: Number(form.elements['v' + v.id].value) }));
        const valido = lineas.every(l => Number.isInteger(l.cantidad) && l.cantidad >= 0 && l.cantidad <= l.variante.stockDisponible);
        total.textContent = valido ? dinero(lineas.reduce((s, l) => s + l.cantidad * Number(l.variante.precioDescuento), 0)) : 'Revisa las cantidades';
        filas.forEach((fila, i) => fila.classList.toggle('seleccionada', lineas[i].cantidad > 0));
        return valido;
      }
      form.addEventListener('input', actualizarTotal);
      actualizarTotal();
      form.addEventListener('submit', e => {
        e.preventDefault();
        if (!actualizarTotal()) { avisoDetalle.textContent = 'Revisa las cantidades disponibles.'; return; }
        const lineas = o.variantes.map(v => ({ varianteId: v.id, cantidad: Number(form.elements['v' + v.id].value) }))
          .filter(l => l.cantidad > 0);
        if (!lineas.length || lineas.some(l => !Number.isInteger(l.cantidad))) {
          avisoDetalle.textContent = 'Elige al menos una unidad válida.'; return;
        }
        const previo = leerCarrito(usuario.id);
        if (previo && previo.ofertaId !== o.id) {
          avisoDetalle.replaceChildren('Tu carrito ya contiene otro pack. ', el('a', { href: 'compras.html', textContent: 'Revisar mi carrito' }));
          return;
        }
        const acumuladas = new Map(previo?.lineas.map(l => [l.varianteId, l.cantidad]) || []);
        for (const l of lineas) acumuladas.set(l.varianteId, (acumuladas.get(l.varianteId) || 0) + l.cantidad);
        if (o.variantes.some(v => (acumuladas.get(v.id) || 0) > v.stockDisponible)) {
          avisoDetalle.textContent = 'La cantidad supera las unidades disponibles.'; return;
        }
        guardarCarrito(usuario.id, { ofertaId: o.id, lineas: [...acumuladas].map(([varianteId, cantidad]) => ({ varianteId, cantidad })) });
        avisoDetalle.replaceChildren('Pack añadido. ', el('a', { href: 'compras.html', textContent: 'Ir a mi carrito' }));
        form.reset(); actualizarTotal();
      });
      const restricciones = o.restricciones || [];
      const opcionesDieta = [
        ['vegetarianos', 'Apto para vegetarianos', 'No apto para vegetarianos'],
        ['veganos', 'Apto para veganos', 'No apto para veganos'],
        ['personas con celiaquía', 'Apto para personas con celiaquía', 'No apto para personas con celiaquía']
      ];
      const estadosDieta = opcionesDieta.map(([nombre, positivo, negativo]) => restricciones.includes(positivo)
        ? [true, positivo] : restricciones.includes(negativo) ? [false, negativo] : null).filter(Boolean);
      const otrasRestricciones = restricciones.filter(r => !opcionesDieta.some(([, positivo, negativo]) => r === positivo || r === negativo));
      const botonAgregar = el('button', { type: 'submit', className: 'detalle-agregar', textContent: 'Añadir al carrito' });
      botonAgregar.setAttribute('form', form.id);
      detalle.replaceChildren(
        el('div', { className: 'detalle-visual' },
          el('span', { className: 'detalle-etiqueta', textContent: o.calificacion != null ? `★  ${Number(o.calificacion).toFixed(1)}` : 'Sin calificaciones' }),
          el('span', { className: 'detalle-visual-marca', ariaHidden: 'true', textContent: '✳' }),
          el('button', { type: 'button', className: 'detalle-cerrar', ariaLabel: 'Cerrar detalle', onclick: () => detalle.close() })),
        el('div', { className: 'detalle-cuerpo' },
          el('h2', { id: 'detalle-titulo' }, ...nombreDestacado(o.pack)),
          o.descripcion ? el('p', { className: 'detalle-descripcion', textContent: o.descripcion }) : null,
          el('p', { className: 'detalle-meta' }, iconoDato('ubicacion'), el('strong', { textContent: o.establecimiento }), ` · ${o.direccion}, ${o.distrito}`),
          el('p', { className: 'detalle-meta' }, iconoDato('reloj'), ...horarioRecojoDestacado(o)),
          el('section', { className: 'detalle-posibles' },
            el('span', { className: 'pack-dato-etiqueta', textContent: 'LA SORPRESA DEL DÍA · PODRÍA INCLUIR' }),
            el('p', { textContent: o.posiblesProductos })),
          o.alergenos.length ? el('section', { className: 'detalle-alergenos' },
            el('h3', { textContent: 'ALÉRGENOS' }),
            el('div', { className: 'detalle-chips' }, ...o.alergenos.map(nombre => el('span', { className: 'detalle-chip', textContent: nombre }))),
            el('p', { className: 'detalle-aviso-alergenos' },
              el('span', { className: 'detalle-info-icono', ariaHidden: 'true', textContent: 'i' }),
              'Si tienes alergia o intolerancia a alguno de estos alérgenos, este pack no es para ti.')) : null,
          estadosDieta.length || otrasRestricciones.length ? el('section', { className: 'detalle-dieta' },
            el('h3', { textContent: 'DIETA' }),
            ...estadosDieta.map(([apto, texto]) => el('p', {},
              el('span', { className: `detalle-estado ${apto ? 'apto' : 'no-apto'}`, ariaHidden: 'true', textContent: apto ? '✓' : '×' }), texto)),
            otrasRestricciones.length ? el('p', { textContent: otrasRestricciones.join(' · ') }) : null) : null,
          el('section', { className: 'detalle-seleccion' },
            el('h3', { textContent: 'Elige tu tamaño' }), form)),
        el('div', { className: 'detalle-pie' },
          el('div', { className: 'detalle-pie-total' }, el('span', { textContent: 'Total' }), total),
          el('div', { className: 'detalle-pie-accion' }, botonAgregar, avisoDetalle)));
      detalle.setAttribute('aria-labelledby', 'detalle-titulo');
      detalle.showModal();
    } catch (error) { if (detalle.isConnected) aviso(error.message, true); }
  }
}
