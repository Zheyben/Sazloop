package com.sazloop.identidad.repository;

import com.sazloop.identidad.domain.CuentaEstablecimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CuentaEstablecimientoRepository extends JpaRepository<CuentaEstablecimiento, Long> {
    Optional<CuentaEstablecimiento> findByNombreUsuarioIgnoreCase(String nombreUsuario);
    Optional<CuentaEstablecimiento> findByEstablecimiento_Id(Long establecimientoId);
}
