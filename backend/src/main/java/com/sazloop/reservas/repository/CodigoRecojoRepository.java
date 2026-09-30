package com.sazloop.reservas.repository;

import com.sazloop.reservas.domain.CodigoRecojo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface CodigoRecojoRepository extends JpaRepository<CodigoRecojo, Long> {
    Optional<CodigoRecojo> findByCodigo(String codigo);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CodigoRecojo c where c.codigo = :codigo")
    Optional<CodigoRecojo> bloquear(@Param("codigo") String codigo);
}
