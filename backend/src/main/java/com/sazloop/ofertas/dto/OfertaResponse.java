package com.sazloop.ofertas.dto;

import com.sazloop.ofertas.domain.OfertaPack.EstadoOferta;
import com.sazloop.ofertas.domain.VariantePack.TamanioVariante;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Ficha pública de oferta y establecimiento para búsqueda y compra. */
public record OfertaResponse(Long id, Long establecimientoId, String establecimiento,
        String distrito, String direccion, Double calificacion,
        String pack, String descripcion, String posiblesProductos,
        List<String> alergenos, List<String> restricciones,
        LocalDateTime inicioRecojo, LocalDateTime finRecojo, LocalDateTime limiteDefinitivo,
        EstadoOferta estado, List<Variante> variantes) {
    public record Variante(Long id, TamanioVariante tamanio, BigDecimal precioReferencial,
                           BigDecimal precioDescuento, BigDecimal ahorro, int stockDisponible) {}
}
