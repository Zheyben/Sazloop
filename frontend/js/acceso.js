const modalAcceso = document.querySelector('#modal-acceso');
const menuMovil = document.querySelector('.menu-movil');

document.querySelectorAll('[data-abrir-acceso]').forEach(enlace => {
  enlace.addEventListener('click', evento => {
    if (typeof modalAcceso.showModal !== 'function') return;
    evento.preventDefault();
    if (menuMovil) menuMovil.open = false;
    modalAcceso.showModal();
    document.body.classList.add('modal-abierto');
  });
});

modalAcceso.querySelector('.modal-cerrar').addEventListener('click', () => modalAcceso.close());

modalAcceso.addEventListener('click', evento => {
  if (evento.target !== modalAcceso) return;
  const area = modalAcceso.getBoundingClientRect();
  if (evento.clientX < area.left || evento.clientX > area.right ||
      evento.clientY < area.top || evento.clientY > area.bottom) {
    modalAcceso.close();
  }
});

modalAcceso.addEventListener('close', () => document.body.classList.remove('modal-abierto'));
