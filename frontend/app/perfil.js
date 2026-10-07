export function montarPerfil(usuario) {
  document.querySelector('#perfil-nombres').textContent = usuario.nombres;
  document.querySelector('#perfil-apellidos').textContent = usuario.apellidos;
  document.querySelector('#perfil-email').textContent = usuario.email;
}
