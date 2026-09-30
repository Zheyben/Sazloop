package com.sazloop.afiliacion.repository;

import com.sazloop.afiliacion.domain.RevisionDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RevisionDocumentoRepository extends JpaRepository<RevisionDocumento, Long> {
    Optional<RevisionDocumento> findFirstByDocumento_IdOrderByIdDesc(Long documentoId);
}
