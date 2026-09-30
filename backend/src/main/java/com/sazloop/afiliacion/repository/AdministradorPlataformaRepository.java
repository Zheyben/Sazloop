package com.sazloop.afiliacion.repository;

import com.sazloop.afiliacion.domain.AdministradorPlataforma;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdministradorPlataformaRepository extends JpaRepository<AdministradorPlataforma, Long> {
    Optional<AdministradorPlataforma> findByEmailInstitucionalIgnoreCase(String email);
}
