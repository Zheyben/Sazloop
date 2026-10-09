package com.sazloop.experiencia.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ResenaRequest(@Min(1) @Max(5) int calificacion,
                            @Size(max = 1000) String comentario) {}
