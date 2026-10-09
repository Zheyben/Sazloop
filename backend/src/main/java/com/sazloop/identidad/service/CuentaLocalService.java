package com.sazloop.identidad.service;

import com.sazloop.identidad.domain.*;
import com.sazloop.identidad.dto.CuentaLocalRequest;
import com.sazloop.identidad.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.Locale;

/** Cuenta compartida vinculada solo al establecimiento aprobado (RF-10). */
@Service
public class CuentaLocalService {
    private final EstablecimientoRepository establecimientos;
    private final CuentaEstablecimientoRepository cuentas;
    private final CodificadorContrasena codificador;

    public CuentaLocalService(EstablecimientoRepository establecimientos,
            CuentaEstablecimientoRepository cuentas, CodificadorContrasena codificador) {
        this.establecimientos = establecimientos;
        this.cuentas = cuentas;
        this.codificador = codificador;
    }

    @Transactional
    public CuentaEstablecimiento crear(Long propietarioId, Long localId, CuentaLocalRequest entrada) {
        Establecimiento local = establecimientos.findById(localId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!local.getEmpresa().getPropietario().getUsuario().getId().equals(propietarioId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (!puedeOperar(local))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El local debe estar aprobado");
        if (cuentas.findByEstablecimiento_Id(localId).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El local ya tiene cuenta");
        String nombre = entrada.nombreUsuario().trim().toLowerCase(Locale.ROOT);
        if (cuentas.findByNombreUsuarioIgnoreCase(nombre).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario ya existe");
        return cuentas.save(CuentaEstablecimiento.builder().establecimiento(local)
                .nombreUsuario(nombre).passwordHash(codificador.codificar(entrada.contrasena())).build());
    }

    @Transactional
    public CuentaEstablecimiento iniciar(CuentaLocalRequest entrada) {
        CuentaEstablecimiento cuenta = cuentas.findByNombreUsuarioIgnoreCase(entrada.nombreUsuario().trim())
                .orElseThrow(CuentaLocalService::denegado);
        if (!cuenta.isActiva() || !puedeOperar(cuenta.getEstablecimiento()) ||
                !codificador.coincide(entrada.contrasena(), cuenta.getPasswordHash())) throw denegado();
        cuenta.setUltimoAcceso(LocalDateTime.now());
        return cuenta;
    }

    @Transactional(readOnly = true)
    public CuentaEstablecimiento obtener(Long id) {
        CuentaEstablecimiento cuenta = cuentas.findById(id).orElseThrow(CuentaLocalService::denegado);
        if (!cuenta.isActiva() || !puedeOperar(cuenta.getEstablecimiento())) throw denegado();
        return cuenta;
    }

    /** Un local suspendido conserva su cuenta para entregar las reservas ya pagadas (RF-09). */
    private static boolean puedeOperar(Establecimiento local) {
        return local.getEstado() == Establecimiento.EstadoEstablecimiento.ACTIVO
                || local.getEstado() == Establecimiento.EstadoEstablecimiento.SUSPENDIDO;
    }

    private static ResponseStatusException denegado() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales del establecimiento inválidas");
    }
}
