package com.cibertec.controlfit.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer idCliente;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(
        regexp = "^[0-9]{8}$",
        message = "El DNI debe contener exactamente 8 dígitos"
    )
    @Column(name = "dni", nullable = false, unique = true, columnDefinition = "CHAR(8)")
    private String dni;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 80)
    @Column(name = "nombres", nullable = false, length = 80)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 80)
    @Column(name = "apellidos", nullable = false, length = 80)
    private String apellidos;

    @Pattern(
        regexp = "^9[0-9]{8}$",
        message = "El teléfono debe contener 9 dígitos y comenzar con 9"
    )
    @Column(name = "telefono", length = 9)
    private String telefono;

    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 100)
    @Column(name = "correo", unique = true, length = 100)
    private String correo;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @NotNull
    @Column(name = "estado", nullable = false)
    private Boolean estado = true;

    @OneToOne
    @JoinColumn(name = "id_usuario", unique = true)
    private Usuario usuario;
}