package com.sazloop.afiliacion.dto;

import com.sazloop.afiliacion.domain.EvaluacionAfiliacion.DecisionAfiliacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DecisionRequest(@NotNull DecisionAfiliacion decision,
                              @NotBlank @Size(max = 500) String motivo) {}
