package com.sazloop.ofertas.repository;

import com.sazloop.ofertas.domain.RestriccionAlimentaria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RestriccionRepository extends JpaRepository<RestriccionAlimentaria, Long> {
    Optional<RestriccionAlimentaria> findByNombreIgnoreCase(String nombre);
}
