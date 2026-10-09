const tipoAcceso = document.body.dataset.acceso;
const mensaje = document.querySelector('#mensaje-acceso');
const ingresoPersonal = document.querySelector('#ingreso-personal');
const registroPersonal = document.querySelector('#registro-personal');
const ingresoTrabajador = document.querySelector('#ingreso-trabajador');
const ingresoAdmin = document.querySelector('#ingreso-administrador');
const esLocales = tipoAcceso === 'locales';
let rol = esLocales ? 'trabajador' : tipoAcceso;

function aviso(texto, error = false) {
  mensaje.textContent = texto;
  mensaje.className = error ? 'error' : 'ok';
}

function vistaRegistro(registro) {
  if (!registroPersonal) return;
  ingresoPersonal.hidden = registro || rol === 'trabajador';
  registroPersonal.hidden = !registro || rol === 'trabajador';
  document.querySelector('#ir-registro').hidden = registro || rol === 'trabajador';
  document.querySelector('#ir-ingreso').hidden = !registro || rol === 'trabajador';
  document.querySelector('#titulo-formulario').textContent =
    registro ? 'Crear cuenta' : rol === 'empresa' ? 'Ingresar como empresa' : 'Iniciar sesión';
}

function cambiarRol(nuevoRol) {
  rol = nuevoRol;
  const marco = document.querySelector('#marco-local');
  marco.classList.toggle('modo-empresa', rol === 'empresa');
  marco.classList.toggle('modo-trabajador', rol === 'trabajador');
  document.querySelectorAll('[data-rol]').forEach(boton => {
    boton.setAttribute('aria-pressed', String(boton.dataset.rol === rol));
  });
  document.querySelector('#etiqueta-historia').textContent =
    rol === 'empresa' ? 'PARA LAS EMPRESAS' : 'PARA EL TRABAJADOR';
  document.querySelector('#titulo-historia').replaceChildren(
    document.createTextNode(rol === 'empresa' ? 'Tu excedente todavía tiene ' : 'Tu local, '),
    Object.assign(document.createElement('em'), { textContent: rol === 'empresa' ? 'valor.' : 'en marcha.' })
  );
  document.querySelector('#descripcion-historia').textContent = rol === 'empresa'
    ? 'Gestiona tu empresa, registra establecimientos y presenta sus documentos para revisión.'
    : 'Ingresa con la cuenta de tu establecimiento para consultar y confirmar los recojos.';
  ingresoTrabajador.hidden = rol !== 'trabajador';
  document.querySelector('#ayuda-trabajador').hidden = rol !== 'trabajador';
  vistaRegistro(false);
  document.querySelector('#titulo-formulario').textContent =
    rol === 'trabajador' ? 'Ingresar como trabajador' : 'Ingresar como empresa';
  aviso('');
}

async function enviar(formulario, ruta, destino) {
  const boton = formulario.querySelector('button[type="submit"]');
  boton.disabled = true;
  aviso('');
  try {
    const respuesta = await fetch(ruta, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(Object.fromEntries(new FormData(formulario)))
    });
    if (!respuesta.ok) {
      const textos = {
        400: 'Revisa los datos ingresados.',
        401: 'Revisa tus credenciales e intenta de nuevo.',
        409: 'La cuenta ya existe o no se puede completar la operación.'
      };
      throw new Error(textos[respuesta.status] || 'No se pudo completar la operación.');
    }
    if (destino) {
      location.href = destino;
    } else {
      const correo = formulario.elements.email.value;
      formulario.reset();
      vistaRegistro(false);
      ingresoPersonal.elements.email.value = correo;
      aviso('Cuenta creada. Ya puedes iniciar sesión.');
    }
  } catch (error) { aviso(error.message, true); }
  finally { boton.disabled = false; }
}

ingresoPersonal?.addEventListener('submit', evento => {
  evento.preventDefault();
  const continuar = new URLSearchParams(location.search).get('continuar');
  let destino = 'consumidor.html';
  if (continuar) {
    const url = new URL(continuar, location.origin);
    if (url.origin === location.origin && url.pathname === '/app/qr.html') destino = url.pathname + url.search;
  }
  enviar(ingresoPersonal, '/api/sesion', rol === 'empresa' ? 'negocio.html#empresa' : destino);
});
registroPersonal?.addEventListener('submit', evento => {
  evento.preventDefault();
  enviar(registroPersonal, '/api/usuarios', null);
});
ingresoTrabajador?.addEventListener('submit', evento => {
  evento.preventDefault();
  enviar(ingresoTrabajador, '/api/local/sesion', 'negocio.html#trabajador');
});
ingresoAdmin?.addEventListener('submit', evento => {
  evento.preventDefault();
  enviar(ingresoAdmin, '/api/administradores/sesion', 'administrador.html');
});

document.querySelector('#ir-registro a')?.addEventListener('click', evento => {
  evento.preventDefault();
  aviso('');
  vistaRegistro(true);
});
document.querySelector('#ir-ingreso a')?.addEventListener('click', evento => {
  evento.preventDefault();
  aviso('');
  vistaRegistro(false);
});
document.querySelectorAll('[data-rol]').forEach(boton => {
  boton.addEventListener('click', () => cambiarRol(boton.dataset.rol));
});

if (esLocales) {
  cambiarRol(location.hash === '#empresa' || location.hash === '#registro' ? 'empresa' : 'trabajador');
  if (location.hash === '#registro') vistaRegistro(true);
} else if (location.hash === '#registro' && registroPersonal) vistaRegistro(true);
