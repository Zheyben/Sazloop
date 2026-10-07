import { solicitar, el, dinero, fechaHora, tamanios, aviso, pintarNav } from './comun.js';

const lista = document.querySelector('#lista');
const detalle = document.querySelector('#detalle');

pintarNav('catalogo.html');
buscar();

document.querySelector('#filtros').addEventListener('submit', e => {
  e.preventDefault();
  buscar(new FormData(e.currentTarget));
});

// 1) Buscar ofertas con los filtros que el backend acepta.
async function buscar(datos = new FormData()) {
  const params = new URLSearchParams();
  for (const [k, v] of datos) if (v) params.set(k, v);   // solo manda los filtros llenos
  try {
    const ofertas = await solicitar('/ofertas?' + params);
    lista.replaceChildren(...(ofertas.length
      ? ofertas.map(tarjeta)
      : [el('p', { className: 'vacio', textContent: 'No hay ofertas con esos filtros. Prueba quitando alguno.' })]));
    aviso('');
  } catch (err) { aviso(err.message, true); }
}

// 2) Tarjeta resumen de cada oferta.
function tarjeta(o) {
  const desde = Math.min(...o.variantes.filter(v => v.stockDisponible > 0).map(v => Number(v.precioDescuento)));
  const stock = o.variantes.reduce((s, v) => s + v.stockDisponible, 0);
  return el('article', { className: 'tarjeta' },
    el('h2', { textContent: o.pack }),
    el('p', { className: 'local', textContent: `${o.establecimiento} · ${o.distrito}` }),
    el('p', { textContent: o.calificacion ? `★ ${o.calificacion.toFixed(1)}` : 'Aún sin calificaciones' }),
    el('p', { className: 'precio', textContent: `Desde ${dinero(desde)}` }),
    el('p', { className: 'chico', textContent: `Recojo hasta ${fechaHora(o.finRecojo)} · quedan ${stock}` }),
    el('button', { type: 'button', textContent: 'Ver oferta', onclick: () => abrirDetalle(o.id) })
  );
}

// 3) Ficha completa + formulario de compra dentro de un <dialog>.
async function abrirDetalle(id) {
  try {
    const o = await solicitar(`/ofertas/${id}`);
    const filas = o.variantes.map(v => el('label', { className: 'fila' },
      el('span', { textContent: `${tamanios[v.tamanio]} — ${dinero(v.precioDescuento)} (antes ${dinero(v.precioReferencial)}, ahorras ${dinero(v.ahorro)}) · quedan ${v.stockDisponible}` }),
      el('input', { type: 'number', min: 0, max: v.stockDisponible, value: 0, name: 'v' + v.id, disabled: v.stockDisponible === 0 })
    ));
    const pago = el('select', { name: 'medioPago' },
      el('option', { value: 'YAPE', textContent: 'Yape (simulado)' }),
      el('option', { value: 'TARJETA_DEBITO', textContent: 'Tarjeta de débito (simulada)' }));
    const form = el('form', {}, ...filas, el('label', {}, 'Medio de pago', pago),
      el('button', { type: 'submit', textContent: 'Comprar' }));
    const resultado = el('div', { role: 'status' });

    form.addEventListener('submit', async e => {
      e.preventDefault();
      // Convierte los inputs con cantidad > 0 en el formato { varianteId, cantidad } que pide el backend.
      const lineas = o.variantes
        .map(v => ({ varianteId: v.id, cantidad: Number(form.elements['v' + v.id].value) }))
        .filter(l => l.cantidad > 0);
      if (!lineas.length) { resultado.textContent = 'Elige al menos una unidad.'; return; }
      try {
        const c = await solicitar('/compras', { method: 'POST',
          body: JSON.stringify({ ofertaId: o.id, medioPago: pago.value, lineas }) });
        form.hidden = true;
        resultado.replaceChildren(
          el('h3', { textContent: '¡Compra confirmada!' }),
          el('p', { textContent: `Total: ${dinero(c.total)} · Boleta ${c.numeroBoleta}` }),
          el('p', { className: 'codigo', textContent: c.codigoRecojo }),
          el('p', { textContent: `Muestra este código en el local entre ${fechaHora(c.inicioRecojo)} y ${fechaHora(c.finRecojo)}.` }),
          el('p', { className: 'chico', textContent: c.avisoBoleta || '' }));
        buscar();   // refresca el stock en el listado
      } catch (err) {
        resultado.textContent = err.status === 401
          ? 'Inicia sesión desde "Mi cuenta" para comprar.' : err.message;
      }
    });

    detalle.replaceChildren(
      el('h2', { textContent: o.pack }),
      el('p', { className: 'local', textContent: `${o.establecimiento} · ${o.direccion}, ${o.distrito}` }),
      el('p', { textContent: o.descripcion }),
      el('p', { textContent: 'Podría incluir: ' + o.posiblesProductos }),
      o.alergenos.length ? el('p', { className: 'alerta', textContent: 'Alérgenos: ' + o.alergenos.join(', ') }) : null,
      o.restricciones.length ? el('p', { textContent: 'Restricciones: ' + o.restricciones.join(', ') }) : null,
      el('p', { className: 'chico', textContent: `Recojo: ${fechaHora(o.inicioRecojo)} a ${fechaHora(o.finRecojo)}` }),
      form, resultado,
      el('button', { type: 'button', className: 'secundario', textContent: 'Cerrar', onclick: () => detalle.close() }));
    detalle.showModal();
  } catch (err) { aviso(err.message, true); }
}
