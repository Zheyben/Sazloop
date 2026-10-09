package com.sazloop.reservas.service;

import com.sazloop.reservas.domain.CodigoRecojo;
import com.sazloop.reservas.domain.Reserva;
import com.sazloop.reservas.repository.ReservaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/** Cierra reservas pagadas no entregadas al finalizar el horario. */
@Service
public class ReservaVencimientoService {
    private final ReservaRepository reservas;
    private final AvisosReservaService avisos;

    public ReservaVencimientoService(ReservaRepository reservas, AvisosReservaService avisos) {
        this.reservas = reservas;
        this.avisos = avisos;
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void vencer() {
        LocalDateTime ahora = LocalDateTime.now();
        for (Reserva candidata : reservas.findByEstadoAndOferta_FinRecojoLessThanEqual(
                Reserva.EstadoReserva.CONFIRMADA, ahora)) {
            Reserva reserva = reservas.bloquear(candidata.getId()).orElseThrow();
            if (reserva.getEstado() == Reserva.EstadoReserva.CONFIRMADA
                    && !reserva.getOferta().getFinRecojo().isAfter(ahora)) {
                reserva.setEstado(Reserva.EstadoReserva.EXPIRADA);
                reserva.getCodigoRecojo().setEstado(CodigoRecojo.EstadoCodigo.VENCIDO);
                avisos.noRecogida(reserva);
            }
        }
    }
}
