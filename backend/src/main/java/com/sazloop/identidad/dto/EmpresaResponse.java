package com.sazloop.identidad.dto;

/** Identifica la empresa guardada sin exponer datos de otros propietarios. */
public record EmpresaResponse(Long id, String razonSocial, String nombreComercial, String ruc) {
}
