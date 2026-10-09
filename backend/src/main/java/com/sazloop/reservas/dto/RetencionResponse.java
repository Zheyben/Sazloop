package com.sazloop.reservas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RetencionResponse(Long reservaId, String codigoRecojo,
        BigDecimal total, LocalDateTime venceEn) {}
