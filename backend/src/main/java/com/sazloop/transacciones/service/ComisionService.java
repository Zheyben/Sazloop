package com.sazloop.transacciones.service;

import com.sazloop.transacciones.domain.ComisionPlataforma;
import com.sazloop.transacciones.domain.Pago;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Calcula la comisión confirmada del 10 % sobre el dinero retenido. */
public final class ComisionService {
    private static final BigDecimal TASA = new BigDecimal("0.1000");

    private ComisionService() {}

    public static void recalcular(Pago pago, BigDecimal importeRetenido) {
        ComisionPlataforma comision = pago.getComision();
        if (comision == null) {
            comision = ComisionPlataforma.builder().pago(pago).build();
            pago.setComision(comision);
        }
        BigDecimal importe = importeRetenido.multiply(TASA).setScale(2, RoundingMode.HALF_UP);
        comision.setImporteBase(importeRetenido);
        comision.setPorcentaje(TASA);
        comision.setImporteComision(importe);
        comision.setIngresoEstablecimiento(importeRetenido.subtract(importe));
        comision.setPendientePorEfectivo(pago.getMedioPago() == Pago.MedioPago.EFECTIVO);
    }
}
