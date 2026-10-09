package com.sazloop.experiencia.service;

import com.sazloop.experiencia.domain.Resena;
import com.sazloop.experiencia.dto.ResenaRequest;
import com.sazloop.experiencia.repository.ResenaRepository;
import com.sazloop.reservas.domain.Reserva;
import com.sazloop.reservas.repository.ReservaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ResenaService {
    private final ReservaRepository reservas;
    private final ResenaRepository resenas;

    public ResenaService(ReservaRepository reservas, ResenaRepository resenas) {
        this.reservas = reservas;
        this.resenas = resenas;
    }

    @Transactional
    public void registrar(Long usuarioId, Long reservaId, ResenaRequest entrada) {
        Reserva reserva = reservas.bloquear(reservaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!reserva.getConsumidor().getUsuario().getId().equals(usuarioId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (reserva.getEstado() != Reserva.EstadoReserva.RECOGIDA)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La compra aún no fue recogida");
        Long localId = reserva.getOferta().getPackSorpresa().getEstablecimiento().getId();
        if (reserva.isResenaOmitida() || resenas.existsByReserva_Id(reservaId)
                || resenas.existsByConsumidor_IdAndEstablecimiento_Id(
                        reserva.getConsumidor().getId(), localId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya no se puede reseñar esta compra");
        String comentario = entrada.comentario() == null || entrada.comentario().isBlank()
                ? null : entrada.comentario().trim();
        resenas.save(Resena.builder().reserva(reserva).consumidor(reserva.getConsumidor())
                .establecimiento(reserva.getOferta().getPackSorpresa().getEstablecimiento())
                .calificacion(entrada.calificacion()).comentario(comentario).build());
    }

    @Transactional
    public void omitir(Long usuarioId, Long reservaId) {
        Reserva reserva = reservas.bloquear(reservaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!reserva.getConsumidor().getUsuario().getId().equals(usuarioId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Long localId = reserva.getOferta().getPackSorpresa().getEstablecimiento().getId();
        if (reserva.getEstado() != Reserva.EstadoReserva.RECOGIDA || reserva.isResenaOmitida()
                || resenas.existsByReserva_Id(reservaId)
                || resenas.existsByConsumidor_IdAndEstablecimiento_Id(
                        reserva.getConsumidor().getId(), localId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No hay una reseña pendiente");
        reserva.setResenaOmitida(true);
    }
}
