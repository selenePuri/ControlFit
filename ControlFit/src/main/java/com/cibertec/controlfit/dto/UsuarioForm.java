package com.cibertec.controlfit.dto;

import com.cibertec.controlfit.model.enums.Rol;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
//dto

@Getter
@Setter
public class UsuarioForm {

    @NotBlank(message = "El nombre de usuario es obligatorio.")
    @Size(max = 50, message = "El usuario no puede superar los 50 caracteres.")
    private String usuario;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 6, max = 72, message = "La contraseña debe tener entre 6 y 72 caracteres.")
    private String clave;

    @NotNull(message = "El usuario debe tener un rol.")
    private Rol rol;
}
