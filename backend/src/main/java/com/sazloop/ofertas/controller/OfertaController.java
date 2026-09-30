package com.sazloop.ofertas.controller;

import com.sazloop.identidad.service.CuentaLocalActual;
import com.sazloop.ofertas.dto.*;
import com.sazloop.ofertas.service.OfertaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class OfertaController {
    private final CuentaLocalActual cuenta;
    private final OfertaService servicio;
    public OfertaController(CuentaLocalActual cuenta, OfertaService servicio) {
        this.cuenta = cuenta; this.servicio = servicio;
    }
    @PostMapping("/local/packs")
    @ResponseStatus(HttpStatus.CREATED)
    public PackCreado crearPack(@Valid @RequestBody PackRequest entrada, HttpServletRequest request) {
        return new PackCreado(servicio.crearPack(cuenta.obtener(request), entrada));
    }
    @PostMapping("/local/ofertas")
    @ResponseStatus(HttpStatus.CREATED)
    public OfertaResponse publicar(@Valid @RequestBody OfertaRequest entrada, HttpServletRequest request) {
        return servicio.publicar(cuenta.obtener(request), entrada);
    }
    @GetMapping("/ofertas")
    public List<OfertaResponse> buscar(@RequestParam(required = false) String distrito,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) BigDecimal precioMaximo,
            @RequestParam(required = false) Double calificacionMinima) {
        return servicio.buscar(distrito, direccion, precioMaximo, calificacionMinima);
    }
    @GetMapping("/ofertas/{id}")
    public OfertaResponse consultar(@PathVariable Long id) { return servicio.consultar(id); }
    public record PackCreado(Long id) {}
}
