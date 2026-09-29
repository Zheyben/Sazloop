package com.sazloop.identidad.repository;

import com.sazloop.identidad.domain.PropietarioEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;

/** Perfil empresarial asociado a una cuenta personal. */
public interface PropietarioEmpresaRepository extends JpaRepository<PropietarioEmpresa, Long> {
    boolean existsByUsuario_Id(Long usuarioId);
}
