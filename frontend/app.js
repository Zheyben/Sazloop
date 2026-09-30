const api = '/api';
const mensaje = document.querySelector('#mensaje');
const acceso = document.querySelector('#acceso');
const cuenta = document.querySelector('#cuenta');
const usuario = document.querySelector('#usuario');
let localActivoId = null;

async function solicitar(ruta, opciones = {}) {
  const respuesta = await fetch(`${api}${ruta}`, {
    ...opciones,
    credentials: 'include',
    headers: { ...(opciones.body instanceof FormData ? {} : { 'Content-Type': 'application/json' }),
      ...opciones.headers }
  });
  if (!respuesta.ok) {
    if (respuesta.status === 409) throw new Error(ruta === '/usuarios'
      ? 'El correo ya está registrado.'
      : ruta === '/empresas' ? 'La empresa ya está registrada.' : 'Esta solicitud no admite cambios.');
    if (respuesta.status === 401) throw new Error('Revisa el correo y la contraseña.');
    if (respuesta.status === 403) throw new Error('Registra primero tu empresa.');
    if (respuesta.status === 400) throw new Error(ruta.includes('/documentos')
      ? 'Revisa el documento: debe ser PDF, PNG o JPG y pesar hasta 10 MB.'
      : 'Revisa los datos ingresados.');
    throw new Error('No se pudo completar la operación.');
  }
  return respuesta.status === 204 ? null : respuesta.json();
}

function mostrarSesion(datos) {
  acceso.hidden = Boolean(datos);
  cuenta.hidden = !datos;
  usuario.textContent = datos ? `${datos.nombres} ${datos.apellidos} (${datos.email})` : '';
  if (!datos) {
    localActivoId = null;
    document.querySelector('#solicitudPanel').hidden = true;
  }
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
    const boton = document.createElement('button');
    boton.type = 'button';
    boton.textContent = 'Documentos';
    boton.addEventListener('click', () => abrirSolicitud(local));
    item.append(' ', boton);
    lista.append(item);
  }
}

async function abrirSolicitud(local) {
  localActivoId = local.id;
  document.querySelector('#solicitudPanel').hidden = false;
  document.querySelector('#solicitudTitulo').textContent = `Documentos de ${local.nombre}`;
  await cargarSolicitud();
}

async function cargarSolicitud() {
  if (localActivoId === null) return;
  try {
    const solicitud = await solicitar(`/establecimientos/${localActivoId}/solicitud`);
    const estado = solicitud?.estado;
    const editable = !estado || estado === 'PENDIENTE' || estado === 'RECHAZADA';
    document.querySelector('#formDocumento').hidden = !editable;
    document.querySelector('#solicitudEstado').textContent = ({
      PENDIENTE: 'Solicitud en preparación.',
      EN_REVISION: 'Solicitud enviada para revisión.',
      APROBADA: 'Solicitud aprobada.',
      RECHAZADA: 'Solicitud rechazada. Adjunta documentos para presentar una nueva.'
    })[estado] || 'Adjunta los documentos del establecimiento.';
    if (solicitud?.motivoDecision) {
      document.querySelector('#solicitudEstado').textContent += ` Motivo: ${solicitud.motivoDecision}`;
    }
    const lista = document.querySelector('#documentos');
    lista.replaceChildren();
    for (const doc of solicitud?.documentos || []) {
      const item = document.createElement('li');
      const enlace = document.createElement('a');
      enlace.href = `/api/establecimientos/${localActivoId}/documentos/${doc.id}/archivo`;
      enlace.textContent = doc.tipo.replaceAll('_', ' ');
      item.append(enlace);
      lista.append(item);
    }
    const enviar = document.querySelector('#enviarSolicitud');
    enviar.hidden = estado !== 'PENDIENTE';
    enviar.disabled = !solicitud?.documentos?.length;
  } catch (error) {
    mensaje.textContent = error.message;
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

document.querySelector('#formDocumento').addEventListener('submit', async (evento) => {
  evento.preventDefault();
  const formulario = evento.currentTarget;
  const datos = new FormData(formulario);
  for (const campo of ['numero', 'emision', 'vencimiento']) {
    if (!datos.get(campo)) datos.delete(campo);
  }
  try {
    await solicitar(`/establecimientos/${localActivoId}/documentos`, {
      method: 'POST', body: datos
    });
    formulario.reset();
    await cargarSolicitud();
    mensaje.textContent = 'Documento adjuntado. Puedes añadir otro o enviar la solicitud.';
  } catch (error) {
    mensaje.textContent = error.message;
  }
});

document.querySelector('#enviarSolicitud').addEventListener('click', async () => {
  try {
    await solicitar(`/establecimientos/${localActivoId}/solicitud/enviar`, { method: 'POST' });
    await cargarSolicitud();
    mensaje.textContent = 'Solicitud enviada para revisión.';
  } catch (error) {
    mensaje.textContent = error.message;
  }
});

solicitar('/sesion')
  .then(mostrarSesion)
  .catch(() => mostrarSesion(null));
