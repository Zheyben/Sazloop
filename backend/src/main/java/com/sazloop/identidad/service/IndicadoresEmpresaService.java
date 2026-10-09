package com.sazloop.identidad.service;

import com.sazloop.experiencia.domain.Resena;
import com.sazloop.experiencia.repository.ResenaRepository;
import com.sazloop.identidad.dto.EstablecimientoResponse;
import com.sazloop.reservas.domain.DetalleReserva;
import com.sazloop.reservas.domain.Reserva;
import com.sazloop.reservas.repository.ReservaRepository;
import com.sazloop.transacciones.domain.Pago;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/** Indicadores por local y consolidados, calculados de los registros existentes. */
@Service
public class IndicadoresEmpresaService {
    private final EmpresaService empresas;
    private final ReservaRepository reservas;
    private final ResenaRepository resenas;

    public IndicadoresEmpresaService(EmpresaService empresas, ReservaRepository reservas,
            ResenaRepository resenas) {
        this.empresas = empresas; this.reservas = reservas; this.resenas = resenas;
    }

    @Transactional(readOnly = true)
    public Resumen resumen(Long usuarioId) {
        List<Indicadores> locales = new ArrayList<>();
        for (EstablecimientoResponse local : empresas.listarEstablecimientos(usuarioId)) {
            locales.add(calcular(local.id(), local.nombre(),
                    reservas.findByOferta_PackSorpresa_Establecimiento_Id(local.id()),
                    resenas.findByEstablecimiento_IdAndVisibleTrue(local.id())));
        }
        int ventas = 0, cancelaciones = 0, noRecogidas = 0, entregados = 0, numResenas = 0;
        BigDecimal importeVentas = BigDecimal.ZERO, comisiones = BigDecimal.ZERO,
                ingresos = BigDecimal.ZERO, sumaNotas = BigDecimal.ZERO;
        for (Indicadores local : locales) {
            ventas += local.ventas(); cancelaciones += local.cancelaciones();
            noRecogidas += local.noRecogidas(); entregados += local.packsEntregados();
            numResenas += local.cantidadResenas();
            importeVentas = importeVentas.add(local.importeVentas());
            comisiones = comisiones.add(local.comisiones());
            ingresos = ingresos.add(local.ingresos());
            sumaNotas = sumaNotas.add(local.calificacionPromedio()
                    .multiply(BigDecimal.valueOf(local.cantidadResenas())));
        }
        return new Resumen(locales, new Indicadores(null, "Total empresa", ventas,
                importeVentas, comisiones, ingresos, cancelaciones, noRecogidas,
                numResenas, promedio(sumaNotas, numResenas), entregados));
    }

    private Indicadores calcular(Long id, String nombre, List<Reserva> pedidos, List<Resena> opiniones) {
        int ventas = 0, cancelaciones = 0, noRecogidas = 0, entregados = 0;
        BigDecimal importeVentas = BigDecimal.ZERO, comisiones = BigDecimal.ZERO,
                ingresos = BigDecimal.ZERO, sumaNotas = BigDecimal.ZERO;
        for (Reserva pedido : pedidos) {
            if (pedido.getEstado() == Reserva.EstadoReserva.CANCELADA) cancelaciones++;
            if (pedido.getEstado() == Reserva.EstadoReserva.EXPIRADA
                    && pedido.getPago().getEstado() == Pago.EstadoPago.CONFIRMADO) noRecogidas++;
            if (pedido.getPago().getEstado() != Pago.EstadoPago.CONFIRMADO) continue;
            if (pedido.getEstado() != Reserva.EstadoReserva.CANCELADA) {
                ventas++;
                importeVentas = importeVentas.add(pedido.getMontoTotal());
            }
            comisiones = comisiones.add(pedido.getPago().getComision().getImporteComision());
            ingresos = ingresos.add(pedido.getPago().getComision().getIngresoEstablecimiento());
            if (pedido.getEstado() == Reserva.EstadoReserva.RECOGIDA)
                entregados += pedido.getDetalles().stream().mapToInt(DetalleReserva::getCantidad).sum();
        }
        for (Resena opinion : opiniones)
            sumaNotas = sumaNotas.add(BigDecimal.valueOf(opinion.getCalificacion()));
        return new Indicadores(id, nombre, ventas, importeVentas, comisiones, ingresos,
                cancelaciones, noRecogidas, opiniones.size(), promedio(sumaNotas, opiniones.size()), entregados);
    }

    private BigDecimal promedio(BigDecimal suma, int cantidad) {
        return cantidad == 0 ? BigDecimal.ZERO
                : suma.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP);
    }

    public record Indicadores(Long establecimientoId, String establecimiento, int ventas,
            BigDecimal importeVentas, BigDecimal comisiones, BigDecimal ingresos,
            int cancelaciones, int noRecogidas, int cantidadResenas,
            BigDecimal calificacionPromedio, int packsEntregados) {}
    public record Resumen(List<Indicadores> establecimientos, Indicadores total) {}
}
