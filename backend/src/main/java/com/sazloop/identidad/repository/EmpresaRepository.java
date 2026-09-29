package com.sazloop.identidad.repository;

import com.sazloop.identidad.domain.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Empresas registradas por los propietarios. */
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    boolean existsByRuc(String ruc);
    Optional<Empresa> findByPropietario_Usuario_Id(Long usuarioId);
}
