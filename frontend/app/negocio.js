import { solicitar, el, dinero } from './comun.js';

const empresa = document.querySelector('#panelEmpresa');
const trabajador = document.querySelector('#panelTrabajador');
const ingreso = document.querySelector('#ingresoLocal');
const cuenta = document.querySelector('#cuentaLocal');
const resultado = document.querySelector('#resultadoRecojo');
let codigoConsultado = null;

function modo(esTrabajador) {
  empresa.hidden = esTrabajador;
  trabajador.hidden = !esTrabajador;
  document.querySelector('#modoEmpresa').setAttribute('aria-pressed', String(!esTrabajador));
  document.querySelector('#modoTrabajador').setAttribute('aria-pressed', String(esTrabajador));
  if (esTrabajador) history.replaceState(null, '', '#trabajador');
  else history.replaceState(null, '', location.pathname);
}

function mostrarLocal(datos) {
  ingreso.hidden = Boolean(datos);
  cuenta.hidden = !datos;
  document.querySelector('#datosLocal').textContent = datos
    ? `Cuenta ${datos.nombreUsuario} · Establecimiento ${datos.establecimientoId}` : '';
  if (!datos) { codigoConsultado = null; resultado.replaceChildren(); }
}

function mensajeLocal(texto, error = false) {
  const zona = document.querySelector('#mensajeLocal');
  zona.textContent = texto;
  zona.className = error ? 'error' : 'ok';
}

document.querySelector('#modoEmpresa').addEventListener('click', () => {
  if (!cuenta.hidden) location.href = 'negocio.html';
  else modo(false);
});
document.querySelector('#modoTrabajador').addEventListener('click', () => modo(true));

ingreso.addEventListener('submit', async evento => {
  evento.preventDefault();
  try {
    const datos = await solicitar('/local/sesion', { method: 'POST', body: JSON.stringify(Object.fromEntries(new FormData(ingreso))) });
    ingreso.reset();
    location.href = 'negocio.html#trabajador';
  } catch (error) { mensajeLocal(error.message, true); }
});

document.querySelector('#salirLocal').addEventListener('click', async () => {
  try {
    await solicitar('/local/sesion', { method: 'DELETE' });
    mostrarLocal(null);
    mensajeLocal('Sesión cerrada.');
  } catch (error) { mensajeLocal(error.message, true); }
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
      el('button', { type: 'button', textContent: 'Confirmar recojo', onclick: confirmarRecojo })
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
} catch { mostrarLocal(null); }
