import { solicitar, el, dinero, fechaHora } from './comun.js';

const empresa = document.querySelector('#panelEmpresa');
const trabajador = document.querySelector('#panelTrabajador');
const cuenta = document.querySelector('#cuentaLocal');
const resultado = document.querySelector('#resultadoRecojo');
let codigoConsultado = null;
const formPack = document.querySelector('#formPack');
const formOferta = document.querySelector('#formOferta');

document.querySelector('#agregarAlergeno').addEventListener('click', () => {
  const campo = document.querySelector('#otroAlergenoCampo');
  campo.hidden = !campo.hidden;
  campo.querySelector('input').required = !campo.hidden;
  if (campo.hidden) campo.querySelector('input').value = '';
  else campo.querySelector('input').focus();
});

function estado(zona, mensaje, esError = false) {
  zona.textContent = mensaje;
  zona.className = esError ? 'error' : 'ok';
}

function listaSeparada(valor, maximo) {
  const nombres = valor.split(',').map(nombre => nombre.trim()).filter(Boolean);
  if (nombres.some(nombre => nombre.length > maximo)) {
    throw new Error(`Cada nombre de esta lista debe tener como máximo ${maximo} caracteres.`);
  }
  return nombres;
}

formPack.addEventListener('submit', async evento => {
  evento.preventDefault();
  const boton = formPack.querySelector('button[type="submit"]');
  const resultadoPack = document.querySelector('#resultadoPack');
  boton.disabled = true;
  estado(resultadoPack, '');
  try {
    const datos = new FormData(formPack);
    const nombre = datos.get('nombre').trim();
    const alergenos = [...new Set([
      ...datos.getAll('alergeno'),
      ...listaSeparada(datos.get('otroAlergeno'), 80)
    ])];
    const restricciones = [
      datos.has('aptoVegetarianos') ? 'Apto para vegetarianos' : 'No apto para vegetarianos',
      datos.has('aptoVeganos') ? 'Apto para veganos' : 'No apto para veganos',
      datos.has('aptoCeliacos') ? 'Apto para personas con celiaquía' : 'No apto para personas con celiaquía',
      ...listaSeparada(datos.get('restricciones'), 100)
    ];
    const pack = await solicitar('/local/packs', {
      method: 'POST',
      body: JSON.stringify({
        nombre,
        descripcion: datos.get('descripcion').trim(),
        posiblesProductos: datos.get('posiblesProductos').trim(),
        alergenos,
        restricciones
      })
    });
    formPack.reset();
    document.querySelector('#otroAlergenoCampo').hidden = true;
    document.querySelector('#otroAlergenoCampo input').required = false;
    formOferta.elements.packId.value = pack.id;
    estado(resultadoPack, `Pack «${nombre}» registrado con el número ${pack.id}. Ya puedes publicar su oferta.`);
  } catch (error) { estado(resultadoPack, error.message, true); }
  finally { boton.disabled = false; }
});

for (const grupo of formOferta.querySelectorAll('.variante-oferta')) {
  const incluir = grupo.querySelector('[name="activo"]');
  incluir.addEventListener('change', () => {
    for (const campo of grupo.querySelectorAll('input:not([name="activo"])')) {
      campo.disabled = !incluir.checked;
      campo.required = incluir.checked;
    }
  });
}

formOferta.addEventListener('submit', async evento => {
  evento.preventDefault();
  const boton = formOferta.querySelector('button[type="submit"]');
  const resultadoOferta = document.querySelector('#resultadoOferta');
  estado(resultadoOferta, '');
  const datos = new FormData(formOferta);
  const inicioRecojo = datos.get('inicioRecojo');
  const finRecojo = datos.get('finRecojo');
  const limiteDefinitivo = datos.get('limiteDefinitivo');
  const variantes = [...formOferta.querySelectorAll('.variante-oferta')]
    .filter(grupo => grupo.querySelector('[name="activo"]').checked)
    .map(grupo => ({
      tamanio: grupo.dataset.tamanio,
      precioReferencial: grupo.querySelector('[name="precioReferencial"]').value,
      precioDescuento: grupo.querySelector('[name="precioDescuento"]').value,
      stock: Number(grupo.querySelector('[name="stock"]').value)
    }));
  if (!variantes.length) {
    estado(resultadoOferta, 'Selecciona al menos un tamaño para publicar.', true);
    return;
  }
  if (variantes.some(v => Number(v.precioDescuento) >= Number(v.precioReferencial))) {
    estado(resultadoOferta, 'El precio con descuento debe ser menor que el referencial en cada tamaño.', true);
    return;
  }
  if (!(new Date(inicioRecojo) > new Date() &&
        new Date(finRecojo) > new Date(inicioRecojo) &&
        new Date(limiteDefinitivo) > new Date(finRecojo))) {
    estado(resultadoOferta, 'Revisa el horario: inicio futuro, fin posterior y límite definitivo después del fin.', true);
    return;
  }
  boton.disabled = true;
  try {
    const oferta = await solicitar('/local/ofertas', {
      method: 'POST',
      body: JSON.stringify({
        packId: Number(datos.get('packId')),
        inicioRecojo, finRecojo, limiteDefinitivo, variantes
      })
    });
    resultadoOferta.className = 'ok';
    resultadoOferta.replaceChildren(
      `Oferta ${oferta.id} publicada. `,
      el('a', { href: 'catalogo.html', textContent: 'Ver catálogo para comprar con una cuenta de consumidor' })
    );
  } catch (error) {
    const mensaje = error.status === 409 ? 'Este local ya tiene una oferta activa. Espera a que termine antes de publicar otra.'
      : error.status === 404 ? 'No se encontró ese pack en este establecimiento. Revisa el número.'
      : error.message;
    estado(resultadoOferta, mensaje, true);
  } finally { boton.disabled = false; }
});

function modo(esTrabajador) {
  document.body.classList.toggle('vista-trabajador', esTrabajador);
  empresa.hidden = esTrabajador;
  trabajador.hidden = !esTrabajador;
  document.querySelector('#modoEmpresa').setAttribute('aria-pressed', String(!esTrabajador));
  document.querySelector('#modoTrabajador').setAttribute('aria-pressed', String(esTrabajador));
  if (esTrabajador) history.replaceState(null, '', '#trabajador');
  else history.replaceState(null, '', location.pathname);
}

function mostrarLocal(datos) {
  cuenta.hidden = !datos;
  document.querySelector('#datosLocal').textContent = datos
    ? `Cuenta ${datos.nombreUsuario} · Establecimiento ${datos.establecimientoId}` : '';
  if (!datos) { codigoConsultado = null; resultado.replaceChildren(); }
}

document.querySelector('#modoEmpresa').addEventListener('click', () => {
  if (!cuenta.hidden) location.href = 'acceso-locales.html#empresa';
  else modo(false);
});
document.querySelector('#modoTrabajador').addEventListener('click', () => {
  if (cuenta.hidden) location.href = 'acceso-locales.html#trabajador';
  else modo(true);
});

document.querySelector('#salirLocal').addEventListener('click', async () => {
  try {
    await solicitar('/local/sesion', { method: 'DELETE' });
    location.href = 'acceso-locales.html#trabajador';
  } catch (error) { resultado.textContent = error.message; }
});

document.querySelector('#consultarRecojo').addEventListener('submit', async evento => {
  evento.preventDefault();
  const codigo = new FormData(evento.currentTarget).get('codigo').trim();
  codigoConsultado = null;
  resultado.replaceChildren();
  try {
    const compra = await solicitar('/local/recojos/' + encodeURIComponent(codigo));
    codigoConsultado = codigo;
    resultado.append(
      el('h4', { textContent: 'Compra encontrada' }),
      el('p', { textContent: `Estado: ${compra.estado} · Total: ${dinero(compra.total)}` }),
      el('p', { textContent: `Recojo: ${fechaHora(compra.inicioRecojo)} a ${fechaHora(compra.finRecojo)}` }),
      ...(compra.estado === 'CONFIRMADA'
        ? [el('button', { type: 'button', textContent: 'Confirmar recojo', onclick: confirmarRecojo })]
        : [el('p', { textContent: 'Esta compra no tiene un recojo pendiente.' })])
    );
  } catch (error) { resultado.textContent = error.message; }
});

async function confirmarRecojo() {
  if (!codigoConsultado) return;
  const boton = resultado.querySelector('button');
  boton.disabled = true;
  try {
    await solicitar('/local/recojos/' + encodeURIComponent(codigoConsultado) + '/confirmar', { method: 'POST' });
    codigoConsultado = null;
    resultado.textContent = 'Recojo confirmado.';
  } catch (error) {
    resultado.textContent = error.message;
  }
}

modo(location.hash === '#trabajador');
try {
  const datos = await solicitar('/local/sesion');
  mostrarLocal(datos);
  modo(true);
} catch {
  mostrarLocal(null);
  try {
    await solicitar('/sesion');
    modo(false);
  } catch {
    location.replace('acceso-locales.html#' + (location.hash === '#trabajador' ? 'trabajador' : 'empresa'));
  }
}
