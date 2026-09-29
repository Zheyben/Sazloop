package com.sazloop.identidad.repository;

import com.sazloop.identidad.domain.Establecimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Establecimientos asociados a una empresa. */
public interface EstablecimientoRepository extends JpaRepository<Establecimiento, Long> {
    List<Establecimiento> findByEmpresa_IdOrderByIdAsc(Long empresaId);
}
