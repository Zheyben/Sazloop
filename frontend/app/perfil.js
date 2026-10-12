import { solicitar } from './comun.js';

export function montarPerfil(usuario) {
  const formulario = document.querySelector('#form-perfil');
  const foto = document.querySelector('#perfil-foto');
  const mensaje = document.querySelector('#mensaje-perfil');
  function pintarDatos() {
    formulario.elements.nombres.value = usuario.nombres;
    formulario.elements.apellidos.value = usuario.apellidos;
    formulario.elements.email.value = usuario.email;
    formulario.elements.telefono.value = usuario.telefono || '';
    document.querySelector('#perfil-nombres').textContent = usuario.nombres;
    document.querySelector('#perfil-apellidos').textContent = usuario.apellidos;
    document.querySelector('#perfil-email').textContent = usuario.email;
    document.querySelector('#perfil-telefono').textContent = usuario.telefono || 'No registrado';
    document.querySelector('#perfil-nombre-completo').textContent = `${usuario.nombres} ${usuario.apellidos}`.trim();
    document.querySelector('#perfil-correo-corto').textContent = usuario.email;
    document.querySelector('#perfil-iniciales').textContent = [usuario.nombres, usuario.apellidos]
      .map(valor => valor.trim().charAt(0).toUpperCase()).join('');
  }
  pintarDatos();
  document.querySelector('#editar-perfil').addEventListener('click', () => {
    formulario.hidden = false;
    document.querySelector('#datos-perfil').hidden = true;
    formulario.elements.nombres.focus();
  });
  document.querySelector('#cancelar-perfil').addEventListener('click', () => {
    pintarDatos();
    formulario.hidden = true;
    document.querySelector('#datos-perfil').hidden = false;
  });
  document.querySelector('#editar-foto').addEventListener('click', () => {
    document.querySelector('#form-foto').hidden = false;
  });
  document.querySelector('#editar-contrasena').addEventListener('click', () => {
    document.querySelector('#form-contrasena').hidden = false;
  });
  if (usuario.tieneFoto) mostrarFoto();

  function estado(texto, error = false) {
    // Si detecta falla de red, lanza el SweetAlert y detiene la función
    if (texto === 'Failed to fetch' || texto.includes('Network') || texto.includes('NetworkError')) {
      Swal.fire({
        title: '¡Ups! Sin conexión',
        text: 'Parece que no tienes internet. Revisa tu red y vuelve a intentarlo.',
        icon: 'error',
        confirmButtonColor: '#183a2b',
        confirmButtonText: 'Entendido'
      });
      return;
    }

    // Si es un error normal (ej. contraseña corta), lo pinta normal en el HTML
    mensaje.textContent = texto;
    mensaje.className = error ? 'error' : 'ok';
  }

  function mostrarFoto() {
    foto.src = `/api/perfil/foto?t=${Date.now()}`;
    foto.hidden = false;
    document.querySelector('#perfil-iniciales').hidden = true;
  }

  formulario.addEventListener('submit', async evento => {
    evento.preventDefault();
    const boton = formulario.querySelector('button[type="submit"]');
    boton.disabled = true;
    try {
      const datos = await solicitar('/perfil', {
        method: 'PUT', body: JSON.stringify(Object.fromEntries(new FormData(formulario)))
      });
      Object.assign(usuario, datos);
      document.querySelector('.nombre-perfil').textContent = datos.nombres.trim().split(/\s+/)[0];
      const avatar = document.querySelector('.avatar');
      if (avatar) avatar.textContent = [datos.nombres, datos.apellidos]
        .map(valor => valor.trim().charAt(0).toUpperCase()).join('');
      pintarDatos();
      formulario.hidden = true;
      document.querySelector('#datos-perfil').hidden = false;
      estado('Datos actualizados.');
    } catch (error) { estado(error.message, true); }
    finally { boton.disabled = false; }
  });

  document.querySelector('#form-contrasena').addEventListener('submit', async evento => {
    evento.preventDefault();
    const form = evento.currentTarget;
    const boton = form.querySelector('button[type="submit"]');
    boton.disabled = true;
    try {
      await solicitar('/perfil/contrasena', {
        method: 'PUT', body: JSON.stringify(Object.fromEntries(new FormData(form)))
      });
      form.reset();
      form.hidden = true;
      estado('Contraseña actualizada.');
    } catch (error) { estado(error.message, true); }
    finally { boton.disabled = false; }
  });

  document.querySelector('#form-foto').addEventListener('submit', async evento => {
    evento.preventDefault();
    const form = evento.currentTarget;
    const archivo = form.elements.archivo.files[0];
    if (!archivo || archivo.size > 2_000_000 || !['image/png', 'image/jpeg'].includes(archivo.type)) {
      estado('Elige una foto PNG o JPG de hasta 2 MB.', true);
      return;
    }
    const boton = form.querySelector('button[type="submit"]');
    boton.disabled = true;
    try {
      await solicitar('/perfil/foto', { method: 'POST', body: new FormData(form) });
      usuario.tieneFoto = true;
      mostrarFoto();
      const avatar = document.querySelector('.portal-perfil summary .avatar, .portal-perfil summary .avatar-foto');
      if (avatar) avatar.replaceWith(Object.assign(document.createElement('img'), {
        className: 'avatar-foto', src: `/api/perfil/foto?t=${Date.now()}`, alt: ''
      }));
      form.reset();
      form.hidden = true;
      estado('Foto actualizada.');
    } catch (error) { estado(error.message, true); }
    finally { boton.disabled = false; }
  });
}
