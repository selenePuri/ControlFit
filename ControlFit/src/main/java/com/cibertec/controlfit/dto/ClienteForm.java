package com.cibertec.controlfit.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Alta de cliente desde recepción o administración. */
@Getter
@Setter
public class ClienteForm {
	//dto

    @NotBlank(message = "El DNI debe tener exactamente 8 dígitos.")
    @Pattern(regexp = "[0-9]{8}", message = "El DNI debe tener exactamente 8 dígitos.")
    private String dni;

    @NotBlank(message = "Nombres y apellidos son obligatorios.")
    @Size(max = 80, message = "Los nombres no pueden superar 80 caracteres.")
    private String nombres;

    @NotBlank(message = "Nombres y apellidos son obligatorios.")
    @Size(max = 80, message = "Los apellidos no pueden superar 80 caracteres.")
    private String apellidos;

    @Pattern(regexp = "9[0-9]{8}", message = "El teléfono debe tener 9 dígitos y empezar con 9.")
    private String telefono;

    @Size(max = 100, message = "El correo no puede superar 100 caracteres.")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "El correo electrónico no es válido.")
    private String correo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @PastOrPresent(message = "La fecha de nacimiento no es válida.")
    private LocalDate fechaNacimiento;

    /** Solo lo usa administración; recepción siempre registra clientes activos. */
    private Boolean estado = Boolean.TRUE;
}
