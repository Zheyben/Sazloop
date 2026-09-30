package com.sazloop.ofertas.repository;

import com.sazloop.ofertas.domain.PackSorpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PackSorpresaRepository extends JpaRepository<PackSorpresa, Long> {
    List<PackSorpresa> findByEstablecimiento_IdOrderByIdDesc(Long localId);
}
