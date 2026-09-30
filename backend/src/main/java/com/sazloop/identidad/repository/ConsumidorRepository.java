package com.sazloop.identidad.repository;

import com.sazloop.identidad.domain.Consumidor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConsumidorRepository extends JpaRepository<Consumidor, Long> {
    Optional<Consumidor> findByUsuario_Id(Long usuarioId);
}
