const api = '/api';
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
    if (respuesta.status === 409) throw new Error(
      ruta === '/usuarios' ? 'El correo ya está registrado.' : 'La empresa ya está registrada.'
    );
    if (respuesta.status === 401) throw new Error('Revisa el correo y la contraseña.');
    if (respuesta.status === 403) throw new Error('Registra primero tu empresa.');
    if (respuesta.status === 400) throw new Error('Revisa los datos ingresados.');
    throw new Error('No se pudo completar la operación.');
  }
  return respuesta.status === 204 ? null : respuesta.json();
}

function mostrarSesion(datos) {
  acceso.hidden = Boolean(datos);
  cuenta.hidden = !datos;
  usuario.textContent = datos ? `${datos.nombres} ${datos.apellidos} (${datos.email})` : '';
  if (datos) cargarEmpresa();
}

async function cargarEmpresa() {
  try {
    const empresa = await solicitar('/empresas/actual');
    document.querySelector('#formEmpresa').hidden = Boolean(empresa);
    document.querySelector('#empresaRegistrada').hidden = !empresa;
    if (empresa) {
      document.querySelector('#empresaDatos').textContent =
        `${empresa.nombreComercial} · ${empresa.razonSocial} · RUC ${empresa.ruc}`;
      await cargarLocales();
    }
  } catch (error) {
    mensaje.textContent = error.message;
  }
}

async function cargarLocales() {
  const lista = document.querySelector('#locales');
  const estados = {
    PENDIENTE_APROBACION: 'Pendiente de aprobación',
    ACTIVO: 'Activo',
    SUSPENDIDO: 'Suspendido',
    INACTIVO: 'Inactivo'
  };
  lista.replaceChildren();
  for (const local of await solicitar('/empresas/actual/establecimientos')) {
    const item = document.createElement('li');
    item.textContent = `${local.nombre} · ${local.distrito}, ${local.direccion} · ${estados[local.estado] || local.estado}`;
    lista.append(item);
  }
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

document.querySelector('#formEmpresa').addEventListener('submit', async (evento) => {
  evento.preventDefault();
  const formulario = evento.currentTarget;
  try {
    await solicitar('/empresas', {
      method: 'POST', body: JSON.stringify(Object.fromEntries(new FormData(formulario)))
    });
    formulario.reset();
    await cargarEmpresa();
    mensaje.textContent = 'Empresa registrada.';
  } catch (error) {
    mensaje.textContent = error.message;
  }
});

document.querySelector('#formLocal').addEventListener('submit', async (evento) => {
  evento.preventDefault();
  const formulario = evento.currentTarget;
  const datos = Object.fromEntries(new FormData(formulario));
  const { departamento, provincia, distrito, direccionLinea, numero, referencia, codigoPostal, ...local } = datos;
  local.direccion = { departamento, provincia, distrito, direccionLinea, numero, referencia, codigoPostal };
  try {
    await solicitar('/empresas/actual/establecimientos', {
      method: 'POST', body: JSON.stringify(local)
    });
    formulario.reset();
    await cargarLocales();
    mensaje.textContent = 'Establecimiento registrado; pendiente de aprobación.';
  } catch (error) {
    mensaje.textContent = error.message;
  }
});

solicitar('/sesion')
  .then(mostrarSesion)
  .catch(() => mostrarSesion(null));
