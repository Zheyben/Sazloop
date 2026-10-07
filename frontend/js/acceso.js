const selector = document.querySelector('#modal-acceso');
const autenticacion = document.querySelector('#modal-autenticacion');
const menuMovil = document.querySelector('.menu-movil');
const mensaje = document.querySelector('#mensaje-autenticacion');
const accesoPersonal = document.querySelector('#acceso-personal');
const ingresoPersonal = document.querySelector('#ingreso-personal');
const registroPersonal = document.querySelector('#registro-personal');
const ingresoTrabajador = document.querySelector('#ingreso-trabajador');
const ingresoAdmin = document.querySelector('#ingreso-administrador');
const rolesLocal = document.querySelector('#roles-local');
const rutas = {
  consumidor: ['app/consumidor.html', '/api/sesion'],
  empresa: ['app/negocio.html', '/api/sesion'],
  trabajador: ['app/negocio.html#trabajador', '/api/local/sesion'],
  administrador: ['app/administrador.html', '/api/administradores/sesion']
};
let modoActual = 'consumidor';

function abrir(dialogo) {
  dialogo.showModal();
  document.body.classList.add('modal-abierto');
}

function cerrar(dialogo) { if (dialogo.open) dialogo.close(); }

function aviso(texto, error = false) {
  mensaje.textContent = texto;
  mensaje.className = error ? 'error' : 'ok';
}

function vistaPersonal(registro) {
  ingresoPersonal.hidden = registro;
  registroPersonal.hidden = !registro;
  document.querySelectorAll('[data-vista]').forEach(boton => {
    boton.setAttribute('aria-pressed', String(boton.dataset.vista === (registro ? 'registro' : 'ingreso')));
  });
}

function mostrarModo(modo) {
  modoActual = modo;
  aviso('');
  rolesLocal.hidden = modo !== 'empresa' && modo !== 'trabajador';
  accesoPersonal.hidden = modo === 'trabajador' || modo === 'administrador';
  ingresoTrabajador.hidden = modo !== 'trabajador';
  ingresoAdmin.hidden = modo !== 'administrador';
  document.querySelectorAll('[data-rol]').forEach(boton => {
    boton.setAttribute('aria-pressed', String(boton.dataset.rol === modo));
  });
  const nombres = {
    consumidor: ['PARA QUIENES COMPRAN', 'Acceso del consumidor'],
    empresa: ['PARA LOS ESTABLECIMIENTOS', 'Acceso de la empresa'],
    trabajador: ['PARA LOS ESTABLECIMIENTOS', 'Acceso del trabajador'],
    administrador: ['ADMINISTRACIÓN', 'Acceso de administrador']
  };
  document.querySelector('#etiqueta-autenticacion').textContent = nombres[modo][0];
  document.querySelector('#titulo-autenticacion').textContent = nombres[modo][1];
  vistaPersonal(false);
}

async function sesionActiva(modo) {
  try {
    const respuesta = await fetch(rutas[modo][1], { credentials: 'include' });
    return respuesta.ok;
  } catch { return false; }
}

async function elegirModo(modo) {
  if (!(modo in rutas)) return;
  if (await sesionActiva(modo)) {
    location.href = rutas[modo][0];
    return;
  }
  mostrarModo(modo);
  cerrar(selector);
  abrir(autenticacion);
}

async function enviar(ruta, datos) {
  const respuesta = await fetch(ruta, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(datos)
  });
  if (!respuesta.ok) {
    const textos = {
      400: 'Revisa los datos ingresados.',
      401: 'Revisa tus credenciales e intenta de nuevo.',
      409: 'Esta cuenta ya existe o no se puede completar el registro.'
    };
    throw new Error(textos[respuesta.status] || 'No se pudo completar la operación.');
  }
  return respuesta;
}

async function gestionarFormulario(formulario, ruta, destino) {
  const boton = formulario.querySelector('button[type="submit"]');
  boton.disabled = true;
  aviso('');
  try {
    await enviar(ruta, Object.fromEntries(new FormData(formulario)));
    location.href = destino;
  } catch (error) {
    aviso(error.message, true);
    boton.disabled = false;
  }
}

document.querySelectorAll('[data-abrir-acceso]').forEach(enlace => {
  enlace.addEventListener('click', evento => {
    if (typeof selector.showModal !== 'function') return;
    evento.preventDefault();
    if (menuMovil) menuMovil.open = false;
    abrir(selector);
  });
});

document.querySelectorAll('[data-modo]').forEach(enlace => {
  enlace.addEventListener('click', evento => {
    if (typeof autenticacion.showModal !== 'function') return;
    evento.preventDefault();
    elegirModo(enlace.dataset.modo);
  });
});

document.querySelectorAll('[data-rol]').forEach(boton => {
  boton.addEventListener('click', () => mostrarModo(boton.dataset.rol));
});
document.querySelectorAll('[data-vista]').forEach(boton => {
  boton.addEventListener('click', () => { aviso(''); vistaPersonal(boton.dataset.vista === 'registro'); });
});

ingresoPersonal.addEventListener('submit', evento => {
  evento.preventDefault();
  gestionarFormulario(ingresoPersonal, '/api/sesion', rutas[modoActual][0]);
});

registroPersonal.addEventListener('submit', async evento => {
  evento.preventDefault();
  const boton = registroPersonal.querySelector('button[type="submit"]');
  boton.disabled = true;
  const correo = registroPersonal.elements.email.value;
  try {
    await enviar('/api/usuarios', Object.fromEntries(new FormData(registroPersonal)));
    registroPersonal.reset();
    vistaPersonal(false);
    ingresoPersonal.elements.email.value = correo;
    aviso('Cuenta creada. Ya puedes iniciar sesión.');
  } catch (error) { aviso(error.message, true); }
  finally { boton.disabled = false; }
});

ingresoTrabajador.addEventListener('submit', evento => {
  evento.preventDefault();
  gestionarFormulario(ingresoTrabajador, '/api/local/sesion', rutas.trabajador[0]);
});
ingresoAdmin.addEventListener('submit', evento => {
  evento.preventDefault();
  gestionarFormulario(ingresoAdmin, '/api/administradores/sesion', rutas.administrador[0]);
});

document.querySelectorAll('.modal-cerrar').forEach(boton => {
  boton.addEventListener('click', () => cerrar(boton.closest('dialog')));
});
document.querySelector('#volver-accesos').addEventListener('click', () => {
  cerrar(autenticacion);
  abrir(selector);
});

for (const dialogo of [selector, autenticacion]) {
  dialogo.addEventListener('click', evento => {
    if (evento.target !== dialogo) return;
    const area = dialogo.getBoundingClientRect();
    if (evento.clientX < area.left || evento.clientX > area.right ||
        evento.clientY < area.top || evento.clientY > area.bottom) cerrar(dialogo);
  });
  dialogo.addEventListener('close', () => {
    if (!selector.open && !autenticacion.open) document.body.classList.remove('modal-abierto');
  });
}

const modoInicial = new URLSearchParams(location.search).get('acceso');
if (modoInicial in rutas && typeof autenticacion.showModal === 'function') elegirModo(modoInicial);
