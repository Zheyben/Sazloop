package com.sazloop.ofertas.service;

import com.sazloop.experiencia.repository.ResenaRepository;
import com.sazloop.identidad.domain.CuentaEstablecimiento;
import com.sazloop.identidad.domain.Establecimiento;
import com.sazloop.identidad.repository.EstablecimientoRepository;
import com.sazloop.ofertas.domain.*;
import com.sazloop.ofertas.dto.*;
import com.sazloop.ofertas.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/** Publica una oferta por local y presenta la ficha pública (RF-11 a RF-15). */
@Service
public class OfertaService {
    private final PackSorpresaRepository packs;
    private final OfertaPackRepository ofertas;
    private final EstablecimientoRepository establecimientos;
    private final AlergenoRepository alergenos;
    private final RestriccionRepository restricciones;
    private final ResenaRepository resenas;

    public OfertaService(PackSorpresaRepository packs, OfertaPackRepository ofertas,
            EstablecimientoRepository establecimientos, AlergenoRepository alergenos,
            RestriccionRepository restricciones, ResenaRepository resenas) {
        this.packs = packs;
        this.ofertas = ofertas;
        this.establecimientos = establecimientos;
        this.alergenos = alergenos;
        this.restricciones = restricciones;
        this.resenas = resenas;
    }

    @Transactional
    public Long crearPack(CuentaEstablecimiento cuenta, PackRequest entrada) {
        PackSorpresa pack = PackSorpresa.builder().establecimiento(cuenta.getEstablecimiento())
                .nombre(entrada.nombre().trim()).descripcion(entrada.descripcion().trim())
                .posiblesProductos(entrada.posiblesProductos().trim()).build();
        for (String nombre : nombres(entrada.alergenos(), 80)) {
            pack.getAlergenos().add(alergenos.findByNombreIgnoreCase(nombre)
                    .orElseGet(() -> alergenos.save(Alergeno.builder().nombre(nombre).build())));
        }
        for (String nombre : nombres(entrada.restricciones(), 100)) {
            pack.getRestricciones().add(restricciones.findByNombreIgnoreCase(nombre)
                    .orElseGet(() -> restricciones.save(RestriccionAlimentaria.builder().nombre(nombre).build())));
        }
        return packs.save(pack).getId();
    }

    @Transactional
    public OfertaResponse publicar(CuentaEstablecimiento cuenta, OfertaRequest entrada) {
        Long localId = cuenta.getEstablecimiento().getId();
        Establecimiento local = establecimientos.bloquear(localId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (local.getEstado() != Establecimiento.EstadoEstablecimiento.ACTIVO)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Local no aprobado");
        PackSorpresa pack = packs.findById(entrada.packId())
                .filter(p -> p.isActivo() && p.getEstablecimiento().getId().equals(localId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pack no encontrado"));
        LocalDateTime ahora = LocalDateTime.now();
        if (!entrada.inicioRecojo().isAfter(ahora) ||
                !entrada.finRecojo().isAfter(entrada.inicioRecojo()) ||
                !entrada.limiteDefinitivo().isAfter(entrada.finRecojo()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Revisa las horas de recojo y el límite");
        for (OfertaPack anterior : ofertas.findByPackSorpresa_Establecimiento_IdAndEstado(
                localId, OfertaPack.EstadoOferta.ACTIVA)) {
            if (anterior.getLimiteDefinitivo().isBefore(ahora)) anterior.setEstado(OfertaPack.EstadoOferta.FINALIZADA);
            else throw new ResponseStatusException(HttpStatus.CONFLICT, "El local ya tiene una oferta activa");
        }
        Set<VariantePack.TamanioVariante> tamanios = new HashSet<>();
        OfertaPack oferta = OfertaPack.builder().packSorpresa(pack)
                .fechaOferta(entrada.inicioRecojo().toLocalDate())
                .inicioRecojo(entrada.inicioRecojo()).finRecojo(entrada.finRecojo())
                .limiteDefinitivo(entrada.limiteDefinitivo()).estado(OfertaPack.EstadoOferta.ACTIVA).build();
        for (OfertaRequest.VarianteRequest v : entrada.variantes()) {
            if (!tamanios.add(v.tamanio()) || v.stock() < 1 ||
                    v.precioDescuento().compareTo(v.precioReferencial()) >= 0)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Variante repetida o precio/stock inválido");
            oferta.getVariantes().add(VariantePack.builder().oferta(oferta)
                    .tamanio(v.tamanio()).precioReferencial(v.precioReferencial())
                    .precioDescuento(v.precioDescuento()).stockInicial(v.stock())
                    .stockDisponible(v.stock()).build());
        }
        return respuesta(ofertas.saveAndFlush(oferta));
    }

    @Transactional(readOnly = true)
    public List<OfertaResponse> buscar(String distrito, String direccion, BigDecimal precioMaximo,
                                        Double calificacionMinima) {
        if (precioMaximo != null && precioMaximo.signum() <= 0 ||
                calificacionMinima != null && (calificacionMinima < 0 || calificacionMinima > 5))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filtro inválido");
        return ofertas.findByEstado(OfertaPack.EstadoOferta.ACTIVA).stream()
                .filter(o -> o.getLimiteDefinitivo().isAfter(LocalDateTime.now()))
                .filter(o -> o.getPackSorpresa().getEstablecimiento().getEstado() ==
                        Establecimiento.EstadoEstablecimiento.ACTIVO)
                .map(this::respuesta)
                .filter(o -> o.variantes().stream().anyMatch(v -> v.stockDisponible() > 0 &&
                        (precioMaximo == null || v.precioDescuento().compareTo(precioMaximo) <= 0)))
                .filter(o -> contiene(o.distrito(), distrito) && contiene(o.direccion(), direccion))
                .filter(o -> calificacionMinima == null ||
                        o.calificacion() != null && o.calificacion() >= calificacionMinima)
                .toList();
    }

    @Transactional(readOnly = true)
    public OfertaResponse consultar(Long id) {
        OfertaPack oferta = ofertas.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (oferta.getEstado() != OfertaPack.EstadoOferta.ACTIVA ||
                !oferta.getLimiteDefinitivo().isAfter(LocalDateTime.now()) ||
                oferta.getPackSorpresa().getEstablecimiento().getEstado()
                        != Establecimiento.EstadoEstablecimiento.ACTIVO)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return respuesta(oferta);
    }

    private OfertaResponse respuesta(OfertaPack oferta) {
        PackSorpresa pack = oferta.getPackSorpresa();
        Establecimiento local = pack.getEstablecimiento();
        var calificaciones = resenas.findByEstablecimiento_IdAndVisibleTrue(local.getId());
        Double promedio = calificaciones.isEmpty() ? null : calificaciones.stream()
                .mapToInt(r -> r.getCalificacion()).average().orElseThrow();
        return new OfertaResponse(oferta.getId(), local.getId(), local.getNombre(),
                local.getDireccion().getDistrito(), local.getDireccion().getDireccionLinea(), promedio,
                pack.getNombre(), pack.getDescripcion(), pack.getPosiblesProductos(),
                pack.getAlergenos().stream().map(Alergeno::getNombre).sorted().toList(),
                pack.getRestricciones().stream().map(RestriccionAlimentaria::getNombre).sorted().toList(),
                oferta.getInicioRecojo(), oferta.getFinRecojo(), oferta.getLimiteDefinitivo(),
                oferta.getEstado(), oferta.getVariantes().stream()
                .map(v -> new OfertaResponse.Variante(v.getId(), v.getTamanio(),
                        v.getPrecioReferencial(), v.getPrecioDescuento(),
                        v.getPrecioReferencial().subtract(v.getPrecioDescuento()),
                        v.getStockDisponible())).toList());
    }

    private static boolean contiene(String texto, String filtro) {
        return filtro == null || filtro.isBlank() ||
                texto.toLowerCase(Locale.ROOT).contains(filtro.trim().toLowerCase(Locale.ROOT));
    }
    private static Set<String> nombres(List<String> entrada, int max) {
        Set<String> salida = new LinkedHashSet<>();
        if (entrada == null) return salida;
        for (String valor : entrada) {
            String nombre = valor.trim();
            if (nombre.isEmpty() || nombre.length() > max)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre de advertencia inválido");
            salida.add(nombre);
        }
        return salida;
    }
}
