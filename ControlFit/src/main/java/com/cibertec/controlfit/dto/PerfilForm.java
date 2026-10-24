package com.cibertec.controlfit.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

//dto


@Getter
@Setter
public class PerfilForm {

    @Pattern(regexp = "9[0-9]{8}", message = "El teléfono debe tener 9 dígitos y empezar con 9.")
    private String telefono;

    @Size(max = 100, message = "El correo no puede superar 100 caracteres.")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "El correo electrónico no es válido.")
    private String correo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @PastOrPresent(message = "La fecha de nacimiento no es válida.")
    private LocalDate fechaNacimiento;
}
