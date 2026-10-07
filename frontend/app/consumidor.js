import { solicitar, aviso } from './comun.js';

const cuenta = document.querySelector('#cuenta');

document.querySelector('#salir').addEventListener('click', async () => {
  try {
    await solicitar('/sesion', { method: 'DELETE' });
    location.href = 'acceso-consumidor.html';
  } catch (error) { aviso(error.message, true); }
});

try {
  const datos = await solicitar('/sesion');
  document.querySelector('#usuario').textContent = datos.nombres;
  cuenta.hidden = false;
} catch {
  location.replace('acceso-consumidor.html');
}
