package com.sazloop.identidad.service;

import com.sazloop.identidad.domain.Direccion;
import com.sazloop.identidad.domain.Empresa;
import com.sazloop.identidad.domain.Establecimiento;
import com.sazloop.identidad.domain.PropietarioEmpresa;
import com.sazloop.identidad.domain.Usuario;
import com.sazloop.identidad.dto.DireccionRequest;
import com.sazloop.identidad.dto.EmpresaRequest;
import com.sazloop.identidad.dto.EmpresaResponse;
import com.sazloop.identidad.dto.EstablecimientoRequest;
import com.sazloop.identidad.dto.EstablecimientoResponse;
import com.sazloop.identidad.repository.EmpresaRepository;
import com.sazloop.identidad.repository.EstablecimientoRepository;
import com.sazloop.identidad.repository.PropietarioEmpresaRepository;
import com.sazloop.identidad.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

/** Registra la empresa del usuario y sus establecimientos (RF-04 y RF-05). */
@Service
public class EmpresaService {
    private final UsuarioRepository usuarios;
    private final PropietarioEmpresaRepository propietarios;
    private final EmpresaRepository empresas;
    private final EstablecimientoRepository establecimientos;

    public EmpresaService(UsuarioRepository usuarios, PropietarioEmpresaRepository propietarios,
                          EmpresaRepository empresas, EstablecimientoRepository establecimientos) {
        this.usuarios = usuarios;
        this.propietarios = propietarios;
        this.empresas = empresas;
        this.establecimientos = establecimientos;
    }

    @Transactional
    public EmpresaResponse registrarEmpresa(Long usuarioId, EmpresaRequest entrada) {
        if (propietarios.existsByUsuario_Id(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cuenta ya tiene una empresa");
        }
        String ruc = entrada.ruc().trim();
        if (empresas.existsByRuc(ruc)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El RUC ya está registrado");
        }
        Usuario usuario = usuarios.findById(usuarioId)
                .filter(Usuario::isActivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        PropietarioEmpresa propietario = propietarios.save(PropietarioEmpresa.builder().usuario(usuario).build());
        Empresa empresa = Empresa.builder()
                .propietario(propietario)
                .razonSocial(entrada.razonSocial().trim())
                .nombreComercial(entrada.nombreComercial().trim())
                .ruc(ruc)
                .build();
        try {
            return respuesta(empresas.saveAndFlush(empresa));
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se pudo registrar la empresa", ex);
        }
    }

    @Transactional(readOnly = true)
    public Optional<EmpresaResponse> obtenerEmpresa(Long usuarioId) {
        return empresas.findByPropietario_Usuario_Id(usuarioId).map(EmpresaService::respuesta);
    }

    @Transactional
    public EstablecimientoResponse registrarEstablecimiento(Long usuarioId, EstablecimientoRequest entrada) {
        Empresa empresa = empresaPropia(usuarioId);
        DireccionRequest dir = entrada.direccion();
        Direccion direccion = Direccion.builder()
                .departamento(dir.departamento().trim())
                .provincia(dir.provincia().trim())
                .distrito(dir.distrito().trim())
                .direccionLinea(dir.direccionLinea().trim())
                .numero(limpiar(dir.numero()))
                .referencia(limpiar(dir.referencia()))
                .codigoPostal(limpiar(dir.codigoPostal()))
                .build();
        Establecimiento establecimiento = Establecimiento.builder()
                .empresa(empresa)
                .nombre(entrada.nombre().trim())
                .descripcion(limpiar(entrada.descripcion()))
                .telefono(limpiar(entrada.telefono()))
                .direccion(direccion)
                .build();
        return respuesta(establecimientos.saveAndFlush(establecimiento));
    }

    @Transactional(readOnly = true)
    public List<EstablecimientoResponse> listarEstablecimientos(Long usuarioId) {
        Empresa empresa = empresaPropia(usuarioId);
        return establecimientos.findByEmpresa_IdOrderByIdAsc(empresa.getId()).stream()
                .map(EmpresaService::respuesta)
                .toList();
    }

    private Empresa empresaPropia(Long usuarioId) {
        return empresas.findByPropietario_Usuario_Id(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Registra tu empresa antes de agregar establecimientos"));
    }

    private static String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static EmpresaResponse respuesta(Empresa empresa) {
        return new EmpresaResponse(empresa.getId(), empresa.getRazonSocial(),
                empresa.getNombreComercial(), empresa.getRuc());
    }

    private static EstablecimientoResponse respuesta(Establecimiento local) {
        return new EstablecimientoResponse(local.getId(), local.getNombre(),
                local.getDireccion().getDistrito(), local.getDireccion().getDireccionLinea(),
                local.getEstado());
    }
}
