package com.sazloop.identidad.dto;

/** Confirma el registro sin exponer la contraseña ni su hash. */
public record UsuarioResponse(Long id, String nombres, String apellidos, String email, String telefono, boolean tieneFoto) {
}
