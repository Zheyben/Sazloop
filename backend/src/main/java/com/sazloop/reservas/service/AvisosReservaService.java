package com.sazloop.reservas.service;

import com.sazloop.experiencia.domain.Notificacion.TipoNotificacion;
import com.sazloop.experiencia.service.NotificacionService;
import com.sazloop.identidad.domain.Establecimiento;
import com.sazloop.ofertas.domain.OfertaPack;
import com.sazloop.reservas.domain.Reserva;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Avisos al consumidor sobre el recojo y los cambios de sus pedidos (RF-25). */
@Service
public class AvisosReservaService {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final String REFERENCIA = "Reserva";
    private final NotificacionService notificaciones;

    public AvisosReservaService(NotificacionService notificaciones) { this.notificaciones = notificaciones; }

    /** El recordatorio de recojo se crea al confirmar la compra. */
    public void recordatorioRecojo(Reserva reserva) {
        OfertaPack oferta = reserva.getOferta();
        Establecimiento local = oferta.getPackSorpresa().getEstablecimiento();
        avisar(reserva, TipoNotificacion.RECORDATORIO_RECOJO, "Recoge tu pedido en " + local.getNombre(),
                "Presenta el código " + reserva.getCodigoRecojo().getCodigo() + " en " + local.getDireccion()
                        .getDireccionLinea() + ", " + local.getDireccion().getDistrito() + ", "
                        + rango(oferta.getInicioRecojo(), oferta.getFinRecojo())
                        + ". Si no lo recoges dentro del horario, el pedido se cerrará sin entrega.");
    }

    public void cancelada(Reserva reserva, BigDecimal reembolso) {
        avisar(reserva, TipoNotificacion.CAMBIO_RESERVA, "Compra cancelada: " + local(reserva),
                "Registramos un reembolso simulado de S/ " + reembolso + " (50 % del total). El código "
                        + reserva.getCodigoRecojo().getCodigo() + " ya no es válido.");
    }

    public void noRecogida(Reserva reserva) {
        avisar(reserva, TipoNotificacion.CAMBIO_RESERVA, "Pedido no recogido: " + local(reserva),
                "El horario de recojo terminó el " + reserva.getOferta().getFinRecojo().format(FECHA) + " a las "
                        + reserva.getOferta().getFinRecojo().format(HORA)
                        + " sin entrega. El código ya no es válido y el importe pagado no se reembolsa.");
    }

    public void efectivoVencido(Reserva reserva) {
        avisar(reserva, TipoNotificacion.CAMBIO_RESERVA, "Reserva en efectivo vencida: " + local(reserva),
                "El pago en efectivo no se confirmó dentro de los 10 minutos de retención; "
                        + "las unidades volvieron a estar disponibles.");
    }

    private void avisar(Reserva reserva, TipoNotificacion tipo, String titulo, String mensaje) {
        notificaciones.crear(reserva.getConsumidor().getUsuario(), tipo, titulo, mensaje, REFERENCIA, reserva.getId());
    }

    private static String local(Reserva reserva) {
        return reserva.getOferta().getPackSorpresa().getEstablecimiento().getNombre();
    }

    private static String rango(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio.toLocalDate().equals(fin.toLocalDate()))
            return "el " + inicio.format(FECHA) + " de " + inicio.format(HORA) + " a " + fin.format(HORA);
        return "del " + inicio.format(FECHA) + " a las " + inicio.format(HORA)
                + " al " + fin.format(FECHA) + " a las " + fin.format(HORA);
    }
}
