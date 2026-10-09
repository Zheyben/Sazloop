package com.sazloop.experiencia.repository;

import com.sazloop.experiencia.domain.ImpactoCO2Evitado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImpactoCO2EvitadoRepository extends JpaRepository<ImpactoCO2Evitado, Long> {
    boolean existsByRecojo_Id(Long recojoId);
}
