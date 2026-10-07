import { iniciarPortal } from './portal.js';
import { iniciarOfertas } from './ofertas-portal.js';

const usuario = await iniciarPortal('catalogo.html');
if (usuario) iniciarOfertas(usuario);
