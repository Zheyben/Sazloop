package com.sazloop.ofertas.service;

import com.sazloop.afiliacion.domain.SuspensionEstablecimiento;
import com.sazloop.afiliacion.repository.SuspensionEstablecimientoRepository;
import com.sazloop.identidad.domain.CuentaEstablecimiento;
import com.sazloop.identidad.domain.Establecimiento;
import com.sazloop.ofertas.domain.OfertaPack;
import com.sazloop.ofertas.domain.VariantePack;
import com.sazloop.ofertas.dto.OfertaResponse;
import com.sazloop.ofertas.repository.OfertaPackRepository;
import com.sazloop.reservas.domain.Reserva;
import com.sazloop.reservas.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class PanelLocalService {
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private final OfertaService servicioOfertas;
    private final OfertaPackRepository ofertas;
    private final ReservaRepository reservas;
    private final SuspensionEstablecimientoRepository suspensiones;

    public PanelLocalService(OfertaService servicioOfertas, OfertaPackRepository ofertas,
            ReservaRepository reservas, SuspensionEstablecimientoRepository suspensiones) {
        this.servicioOfertas = servicioOfertas;
        this.ofertas = ofertas;
        this.reservas = reservas;
        this.suspensiones = suspensiones;
    }

    @Transactional(readOnly = true)
    public Resumen resumen(CuentaEstablecimiento cuenta) {
        Long localId = cuenta.getEstablecimiento().getId();
        List<OfertaPack> publicaciones = ofertas.findByPackSorpresa_Establecimiento_Id(localId);
        List<Reserva> pedidos = reservas.findByOferta_PackSorpresa_Establecimiento_Id(localId);
        LocalDate hoy = LocalDate.now(LIMA);
        LocalDateTime ahora = LocalDateTime.now(LIMA);
        OfertaResponse vigente = servicioOfertas.vigente(cuenta).orElse(null);
        int stock = vigente == null ? 0 : vigente.variantes().stream()
                .mapToInt(OfertaResponse.Variante::stockDisponible).sum();
        String tokenQr = vigente == null ? null : publicaciones.stream()
                .filter(oferta -> oferta.getId().equals(vigente.id()))
                .findFirst().orElseThrow().getTokenQr();
        String motivoSuspension = suspensiones.findFirstByEstablecimiento_IdAndActivaTrueOrderByIdDesc(localId)
                .map(SuspensionEstablecimiento::getMotivo).orElse(null);
        return new Resumen(cuenta.getEstablecimiento().getNombre(), vigente, tokenQr, stock,
                periodo(pedidos, publicaciones, hoy, ahora),
                periodo(pedidos, publicaciones,
                        hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), ahora),
                periodo(pedidos, publicaciones, hoy.withDayOfMonth(1), ahora),
                cuenta.getEstablecimiento().getEstado(), motivoSuspension);
    }

    private Periodo periodo(List<Reserva> pedidos, List<OfertaPack> publicaciones,
            LocalDate inicio, LocalDateTime ahora) {
        List<Reserva> entregados = pedidos.stream()
                .filter(pedido -> pedido.getEstado() == Reserva.EstadoReserva.RECOGIDA)
                .filter(pedido -> pedido.getRecojo() != null && pedido.getPago() != null && pedido.getPago().getComision() != null)
                .filter(pedido -> !pedido.getRecojo().getFechaRecojo().toLocalDate().isBefore(inicio))
                .toList();
        BigDecimal ganancia = entregados.stream()
                .map(pedido -> pedido.getPago().getComision().getIngresoEstablecimiento())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal comision = entregados.stream()
                .map(pedido -> pedido.getPago().getComision().getImporteComision())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int unidadesEntregadas = entregados.stream()
                .flatMap(pedido -> pedido.getDetalles().stream()).mapToInt(d -> d.getCantidad()).sum();
        int noVendidas = publicaciones.stream()
                .filter(oferta -> !oferta.getFinRecojo().isAfter(ahora))
                .filter(oferta -> !oferta.getFechaOferta().isBefore(inicio))
                .flatMap(oferta -> oferta.getVariantes().stream())
                .mapToInt(VariantePack::getStockDisponible).sum();
        return new Periodo(ganancia, noVendidas, ganancia.add(comision), comision, unidadesEntregadas);
    }

    /** ganancia = neta (ya sin la comisión del 10 %); ventaBruta = ganancia + comisión. */
    public record Periodo(BigDecimal ganancia, int unidadesNoVendidas, BigDecimal ventaBruta,
            BigDecimal comision, int packsEntregados) {}
    public record Resumen(String establecimiento, OfertaResponse ofertaVigente,
            String tokenQr, int stockDisponible, Periodo dia, Periodo semana, Periodo mes,
            Establecimiento.EstadoEstablecimiento estadoLocal, String motivoSuspension) {}
}
