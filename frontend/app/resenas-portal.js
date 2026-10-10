import { solicitar, el } from './comun.js';

const TEXTOS = ['Muy malo', 'Malo', 'Regular', 'Bueno', 'Excelente'];

function dialogoResena() {
  const existente = document.querySelector('#dialogo-resena');
  if (existente) return existente;
  const cerrar = el('button', { type: 'button', className: 'resena-cerrar', ariaLabel: 'Cerrar sin calificar esta compra' });
  const titulo = el('h2', { id: 'titulo-resena' });
  // Estrellas: grupo de opciones de 1 a 5 (el valor elegido se guarda en el campo oculto «calificacion»).
  const campo = el('input', { type: 'hidden', name: 'calificacion', value: '' });
  const leyenda = el('p', { className: 'resena-leyenda', id: 'leyenda-resena', ariaLive: 'polite', textContent: 'Toca una estrella' });
  const estrellas = el('div', { className: 'resena-estrellas', role: 'radiogroup', ariaLabel: 'Calificación de 1 a 5 estrellas' },
    ...[1, 2, 3, 4, 5].map(n => {
      const boton = el('button', { type: 'button', className: 'resena-estrella', role: 'radio', ariaChecked: 'false',
        ariaLabel: `${n} ${n === 1 ? 'estrella' : 'estrellas'}: ${TEXTOS[n - 1]}`, textContent: '★' });
      boton.dataset.valor = String(n); // dataset es de solo lectura: no puede asignarse con Object.assign
      return boton;
    }));
  const form = el('form', {},
    campo, estrellas, leyenda,
    el('label', {}, 'Comentario (opcional)', el('textarea', { name: 'comentario', maxLength: 1000, rows: 3, placeholder: 'Cuéntanos cómo estuvo tu pack' })),
    el('p', { id: 'mensaje-resena', role: 'status', ariaLive: 'polite' }),
    el('button', { type: 'submit', textContent: 'Enviar reseña' }));
  const dialogo = el('dialog', { id: 'dialogo-resena', className: 'dialogo-resena', ariaLabelledBy: 'titulo-resena' }, cerrar, titulo, form);

  function elegir(valor) {
    campo.value = valor ? String(valor) : '';
    estrellas.querySelectorAll('.resena-estrella').forEach(boton => {
      const activa = Number(boton.dataset.valor) <= valor;
      boton.classList.toggle('activa', activa);
      boton.setAttribute('aria-checked', String(Number(boton.dataset.valor) === valor));
    });
    leyenda.textContent = valor ? `${valor} de 5 · ${TEXTOS[valor - 1]}` : 'Toca una estrella';
  }
  estrellas.addEventListener('click', evento => {
    const boton = evento.target.closest('.resena-estrella');
    if (boton) elegir(Number(boton.dataset.valor));
  });
  dialogo.elegirEstrellas = elegir;
  document.body.append(dialogo);
  return dialogo;
}

export function abrirResena(compra, alTerminar = () => {}) {
  const dialogo = dialogoResena();
  if (dialogo.open) return;
  const form = dialogo.querySelector('form');
  const mensaje = dialogo.querySelector('#mensaje-resena');
  const cerrar = dialogo.querySelector('.resena-cerrar');
  const enviar = form.querySelector('button[type="submit"]');
  dialogo.querySelector('#titulo-resena').textContent = `¿Cómo calificarías el producto de ${compra.establecimiento}?`;
  form.reset();
  dialogo.elegirEstrellas(0);
  mensaje.textContent = '';
  let enviando = false;

  async function terminar(ruta, datos) {
    if (enviando) return;
    enviando = true;
    cerrar.disabled = true;
    enviar.disabled = true;
    try {
      await solicitar(ruta, { method: 'POST', ...(datos ? { body: JSON.stringify(datos) } : {}) });
      dialogo.close();
      await alTerminar();
    } catch (error) {
      mensaje.textContent = error.message;
    } finally {
      enviando = false;
      cerrar.disabled = false;
      enviar.disabled = false;
    }
  }
  cerrar.onclick = () => terminar(`/compras/${compra.reservaId}/resena/omitir`);
  dialogo.oncancel = evento => { evento.preventDefault(); cerrar.click(); };
  form.onsubmit = evento => {
    evento.preventDefault();
    const calificacion = Number(form.elements.calificacion.value);
    if (!calificacion) {
      mensaje.textContent = 'Elige de 1 a 5 estrellas para enviar tu reseña.';
      return;
    }
    terminar(`/compras/${compra.reservaId}/resena`, { calificacion, comentario: form.elements.comentario.value.trim() });
  };
  dialogo.showModal();
}

export async function avisarResenasPendientes() {
  try {
    const compras = await solicitar('/compras/mias');
    const vistas = new Set();
    const pendientes = compras.filter(compra => {
      if (!compra.resenaPendiente || vistas.has(compra.establecimientoId)) return false;
      vistas.add(compra.establecimientoId);
      return true;
    });
    function siguiente() {
      const compra = pendientes.shift();
      if (compra) abrirResena(compra, siguiente);
    }
    siguiente();
  } catch { /* El historial conserva el acceso manual si la consulta falla. */ }
}
