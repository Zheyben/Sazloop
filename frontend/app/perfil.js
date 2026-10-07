import { iniciarPortal } from './portal.js';

const usuario = await iniciarPortal('perfil.html');
if (usuario) {
  document.querySelector('#perfil-nombres').textContent = usuario.nombres;
  document.querySelector('#perfil-apellidos').textContent = usuario.apellidos;
  document.querySelector('#perfil-email').textContent = usuario.email;
}
