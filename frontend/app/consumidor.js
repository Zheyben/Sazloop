import { solicitar, aviso } from './comun.js';

const acceso = document.querySelector('#acceso');
const cuenta = document.querySelector('#cuenta');
const usuario = document.querySelector('#usuario');

function mostrar(datos) {
  acceso.hidden = Boolean(datos);
  cuenta.hidden = !datos;
  usuario.textContent = datos ? datos.nombres : '';
}

document.querySelector('#registro').addEventListener('submit', async evento => {
  evento.preventDefault();
  const formulario = evento.currentTarget;
  try {
    await solicitar('/usuarios', { method: 'POST', body: JSON.stringify(Object.fromEntries(new FormData(formulario))) });
    formulario.reset();
    aviso('Cuenta creada. Ya puedes iniciar sesión.');
  } catch (error) { aviso(error.message, true); }
});

document.querySelector('#ingreso').addEventListener('submit', async evento => {
  evento.preventDefault();
  const formulario = evento.currentTarget;
  try {
    mostrar(await solicitar('/sesion', { method: 'POST', body: JSON.stringify(Object.fromEntries(new FormData(formulario))) }));
    formulario.reset();
    aviso('Sesión iniciada.');
  } catch (error) { aviso(error.message, true); }
});

document.querySelector('#salir').addEventListener('click', async () => {
  try {
    await solicitar('/sesion', { method: 'DELETE' });
    mostrar(null);
    aviso('Sesión cerrada.');
  } catch (error) { aviso(error.message, true); }
});

try { mostrar(await solicitar('/sesion')); }
catch { mostrar(null); }
