const api = 'http://localhost:8080/api';
const mensaje = document.querySelector('#mensaje');
const acceso = document.querySelector('#acceso');
const cuenta = document.querySelector('#cuenta');
const usuario = document.querySelector('#usuario');

async function solicitar(ruta, opciones = {}) {
  const respuesta = await fetch(`${api}${ruta}`, {
    ...opciones,
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...opciones.headers }
  });
  if (!respuesta.ok) {
    if (respuesta.status === 409) throw new Error('El correo ya está registrado.');
    if (respuesta.status === 401) throw new Error('Revisa el correo y la contraseña.');
    if (respuesta.status === 400) throw new Error('Revisa los datos ingresados.');
    throw new Error('No se pudo completar la operación.');
  }
  return respuesta.status === 204 ? null : respuesta.json();
}

function mostrarSesion(datos) {
  acceso.hidden = Boolean(datos);
  cuenta.hidden = !datos;
  usuario.textContent = datos ? `${datos.nombres} ${datos.apellidos} (${datos.email})` : '';
}

document.querySelector('#registro').addEventListener('submit', async (evento) => {
  evento.preventDefault();
  const formulario = evento.currentTarget;
  const datos = Object.fromEntries(new FormData(formulario));
  try {
    await solicitar('/usuarios', { method: 'POST', body: JSON.stringify(datos) });
    formulario.reset();
    mensaje.textContent = 'Cuenta creada. Ya puedes iniciar sesión.';
  } catch (error) {
    mensaje.textContent = error.message;
  }
});

document.querySelector('#ingreso').addEventListener('submit', async (evento) => {
  evento.preventDefault();
  const formulario = evento.currentTarget;
  const datos = Object.fromEntries(new FormData(formulario));
  try {
    mostrarSesion(await solicitar('/sesion', { method: 'POST', body: JSON.stringify(datos) }));
    formulario.reset();
    mensaje.textContent = 'Sesión iniciada.';
  } catch (error) {
    mensaje.textContent = error.message;
  }
});

document.querySelector('#salir').addEventListener('click', async () => {
  try {
    await solicitar('/sesion', { method: 'DELETE' });
    mostrarSesion(null);
    mensaje.textContent = 'Sesión cerrada.';
  } catch (error) {
    mensaje.textContent = error.message;
  }
});

solicitar('/sesion')
  .then(mostrarSesion)
  .catch(() => mostrarSesion(null));
