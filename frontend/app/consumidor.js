import { iniciarPortal } from './portal.js';
import { iniciarOfertas } from './ofertas-portal.js';

const usuario = await iniciarPortal('consumidor.html');
if (usuario) {
  document.querySelector('#usuario').textContent = usuario.nombres.trim().split(/\s+/)[0];
  iniciarOfertas(usuario, 6);
}
