package com.sazloop.experiencia.repository;

import com.sazloop.experiencia.domain.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByEstablecimiento_IdAndVisibleTrue(Long localId);
}
