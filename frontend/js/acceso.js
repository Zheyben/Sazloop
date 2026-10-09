const modalAcceso = document.querySelector('#modal-acceso');
const menuMovil = document.querySelector('.menu-movil');
let cerrando = false;
let cierreDeRespaldo;

function finalizarCierre() {
  if (modalAcceso.open) modalAcceso.close();
}

function cerrarModal() {
  if (!modalAcceso.open || cerrando) return;
  cerrando = true;
  modalAcceso.classList.add('cerrando');
  cierreDeRespaldo = setTimeout(finalizarCierre, 380);
}

document.querySelectorAll('[data-abrir-acceso]').forEach(enlace => {
  enlace.addEventListener('click', evento => {
    if (typeof modalAcceso.showModal !== 'function') return;
    evento.preventDefault();
    if (menuMovil) menuMovil.open = false;
    modalAcceso.showModal();
    document.body.classList.add('modal-abierto');
  });
});

modalAcceso.querySelector('.modal-cerrar').addEventListener('click', () => cerrarModal());

modalAcceso.addEventListener('click', evento => {
  if (evento.target !== modalAcceso) return;
  const area = modalAcceso.getBoundingClientRect();
  if (evento.clientX < area.left || evento.clientX > area.right ||
      evento.clientY < area.top || evento.clientY > area.bottom) cerrarModal();
});

modalAcceso.addEventListener('cancel', evento => {
  evento.preventDefault();
  cerrarModal();
});

modalAcceso.addEventListener('animationend', evento => {
  if (evento.target === modalAcceso && evento.animationName === 'modal-salir') finalizarCierre();
});

modalAcceso.addEventListener('close', () => {
  clearTimeout(cierreDeRespaldo);
  modalAcceso.classList.remove('cerrando');
  document.body.classList.remove('modal-abierto');
  cerrando = false;
});
