import { solicitar, el, dinero, fechaHora, aviso, tamanios } from './comun.js';
import { leerCarrito, guardarCarrito } from './portal.js';

const estados = {
  PENDIENTE_PAGO: 'Pendiente de pago', CONFIRMADA: 'Lista para recoger',
  RECOGIDA: 'Recogida', CANCELADA: 'Cancelada', EXPIRADA: 'Expirada'
};
let usuario;
export function montarCompras(usuarioActual) {
  usuario = usuarioActual;
  mostrarCarrito();
  mostrarHistorial();
}

async function mostrarCarrito() {
  const zona = document.querySelector('#carrito');
  const carrito = leerCarrito(usuario.id);
  if (!carrito) {
    zona.replaceChildren(el('div', { className: 'estado-vacio' },
      el('p', { textContent: 'Tu carrito está vacío.' }),
      el('a', { href: 'catalogo.html', textContent: 'Explorar productos' })));
    return;
  }

  zona.replaceChildren(el('p', { className: 'estado-vacio', textContent: 'Revisando disponibilidad…' }));
  try {
    const oferta = await solicitar(`/ofertas/${carrito.ofertaId}`);
    if (!zona.isConnected) return;
    const variantes = carrito.lineas.map(linea => ({
      ...oferta.variantes.find(v => v.id === linea.varianteId), cantidad: linea.cantidad
    })).filter(v => v.id);
    if (!variantes.length) {
      zona.replaceChildren(el('div', { className: 'estado-vacio' },
        el('p', { textContent: 'Este pack ya no tiene las variantes que seleccionaste.' }),
        el('button', { type: 'button', className: 'secundario', textContent: 'Quitar del carrito', onclick: () => { guardarCarrito(usuario.id, null); mostrarCarrito(); } })));
      return;
    }
    const total = el('strong', { className: 'carrito-total' });
    const mensajeCarrito = el('p', { className: 'detalle-aviso', role: 'status', ariaLive: 'polite' });
    const filas = variantes.map(v => el('label', { className: 'detalle-variante' },
      el('span', {}, el('strong', { textContent: tamanios[v.tamanio] || v.tamanio }),
        el('small', { textContent: `${dinero(v.precioDescuento)} cada uno · quedan ${v.stockDisponible}` })),
      el('input', { name: 'v' + v.id, type: 'number', min: 0, max: v.stockDisponible, value: v.cantidad,
        ariaLabel: `Cantidad de ${tamanios[v.tamanio] || v.tamanio}` })));
    const pago = el('select', { name: 'medioPago' },
      el('option', { value: 'YAPE', textContent: 'Yape' }),
      el('option', { value: 'TARJETA_DEBITO', textContent: 'Tarjeta de débito' }));
    const quitar = el('button', { type: 'button', className: 'secundario', textContent: 'Quitar pack', onclick: () => {
      guardarCarrito(usuario.id, null); mostrarCarrito();
    } });
    const confirmar = el('button', { type: 'submit', textContent: 'Confirmar compra' });
    const form = el('form', { className: 'carrito-formulario' },
      ...filas, el('label', { className: 'carrito-pago' }, 'Medio de pago', pago),
      el('div', { className: 'carrito-resumen' }, el('span', { textContent: 'Total' }), total),
      el('div', { className: 'carrito-acciones' }, confirmar, quitar), mensajeCarrito);

    function lineasElegidas() {
      return variantes.map(v => ({ varianteId: v.id, cantidad: Number(form.elements['v' + v.id].value) }))
        .filter(l => l.cantidad > 0);
    }
    function actualizar() {
      if (!form.reportValidity()) {
        mensajeCarrito.textContent = 'Revisa las cantidades; puede haber cambiado el stock disponible.';
        return false;
      }
      const lineas = lineasElegidas();
      if (lineas.some(l => !Number.isInteger(l.cantidad) || l.cantidad > oferta.variantes.find(v => v.id === l.varianteId).stockDisponible)) {
        mensajeCarrito.textContent = 'Revisa las cantidades disponibles.';
        return false;
      }
      if (!lineas.length) {
        guardarCarrito(usuario.id, null);
        mostrarCarrito();
        return false;
      }
      guardarCarrito(usuario.id, { ofertaId: oferta.id, lineas });
      total.textContent = dinero(lineas.reduce((s, l) => s + l.cantidad * Number(oferta.variantes.find(v => v.id === l.varianteId).precioDescuento), 0));
      mensajeCarrito.textContent = '';
      return true;
    }
    form.addEventListener('change', e => { if (e.target.type === 'number') actualizar(); });
    form.addEventListener('submit', async e => {
      e.preventDefault();
      if (!actualizar()) return;
      confirmar.disabled = true;
      try {
        const compra = await solicitar('/compras', { method: 'POST', body: JSON.stringify({
          ofertaId: oferta.id, medioPago: pago.value, lineas: lineasElegidas()
        }) });
        guardarCarrito(usuario.id, null);
        if (!zona.isConnected) return;
        zona.replaceChildren(el('div', { className: 'compra-confirmada' },
          el('p', { className: 'etiqueta', textContent: 'COMPRA CONFIRMADA' }),
          el('h3', { textContent: 'Tu pack ya está reservado.' }),
          el('p', { textContent: `Total: ${dinero(compra.total)} · Boleta ${compra.numeroBoleta}` }),
          el('p', { className: 'codigo', textContent: compra.codigoRecojo }),
          el('p', { textContent: `Muestra este código en el local entre ${fechaHora(compra.inicioRecojo)} y ${fechaHora(compra.finRecojo)}.` }),
          el('p', { className: 'chico', textContent: compra.avisoBoleta || '' })));
        mostrarHistorial();
      } catch (error) {
        if (!zona.isConnected) return;
        mensajeCarrito.textContent = error.message;
        confirmar.disabled = false;
      }
    });
    zona.replaceChildren(el('div', { className: 'carrito-panel' },
      el('div', { className: 'carrito-titulo' },
        el('h3', { textContent: oferta.pack }),
        el('p', { textContent: `${oferta.establecimiento} · ${oferta.distrito}` }),
        el('p', { className: 'chico', textContent: `Recojo: ${fechaHora(oferta.inicioRecojo)} a ${fechaHora(oferta.finRecojo)}` })),
      form));
    actualizar();
  } catch (error) {
    if (!zona.isConnected) return;
    zona.replaceChildren(el('div', { className: 'estado-vacio' },
      el('p', { textContent: 'No se pudo consultar el pack de tu carrito.' }),
      el('button', { type: 'button', className: 'secundario', textContent: 'Quitar del carrito', onclick: () => { guardarCarrito(usuario.id, null); mostrarCarrito(); } })));
  }
}

async function mostrarHistorial() {
  const lista = document.querySelector('#lista');
  try {
    const compras = await solicitar('/compras/mias');
    if (!lista.isConnected) return;
    lista.replaceChildren(...(compras.length
      ? compras.map(c => el('article', { className: 'tarjeta-compra' },
        el('p', { className: 'etiqueta', textContent: estados[c.estado] || c.estado }),
        el('h3', { textContent: dinero(c.total) }),
        el('p', { textContent: `Medio de pago: ${c.medioPago === 'TARJETA_DEBITO' ? 'Tarjeta de débito' : c.medioPago}` }),
        el('p', { className: 'codigo', textContent: c.codigoRecojo }),
        el('p', { className: 'chico', textContent: `Boleta ${c.numeroBoleta} · Recojo ${fechaHora(c.inicioRecojo)} a ${fechaHora(c.finRecojo)}` })))
      : [el('div', { className: 'estado-vacio', textContent: 'Aún no tienes compras realizadas.' })]));
    aviso('');
  } catch (error) { if (lista.isConnected) aviso(error.message, true); }
}
