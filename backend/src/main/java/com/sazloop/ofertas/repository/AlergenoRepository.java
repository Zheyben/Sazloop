package com.sazloop.ofertas.repository;

import com.sazloop.ofertas.domain.Alergeno;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AlergenoRepository extends JpaRepository<Alergeno, Long> {
    Optional<Alergeno> findByNombreIgnoreCase(String nombre);
}
