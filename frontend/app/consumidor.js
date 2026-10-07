import { iniciarOfertas } from './ofertas-portal.js';

export function montarInicio(usuario) {
  document.querySelector('#usuario').textContent = usuario.nombres.trim().split(/\s+/)[0];
  iniciarOfertas(usuario, 6);
}
