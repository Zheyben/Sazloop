package com.sazloop.ofertas.controller;

import com.sazloop.identidad.service.CuentaLocalActual;
import com.sazloop.ofertas.service.PanelLocalService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/local")
public class PanelLocalController {
    private final CuentaLocalActual cuenta;
    private final PanelLocalService panel;

    public PanelLocalController(CuentaLocalActual cuenta, PanelLocalService panel) {
        this.cuenta = cuenta;
        this.panel = panel;
    }

    @GetMapping("/resumen")
    public PanelLocalService.Resumen resumen(HttpServletRequest request) {
        return panel.resumen(cuenta.obtener(request));
    }
}
