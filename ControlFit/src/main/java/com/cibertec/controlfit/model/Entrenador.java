package com.cibertec.controlfit.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "entrenador")
@Getter
@Setter
@NoArgsConstructor
public class Entrenador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrenador")
    private Integer idEntrenador;

    @NotNull(message = "El entrenador debe estar asociado a un empleado")
    @OneToOne
    @JoinColumn(name = "id_empleado", nullable = false, unique = true)
    private Empleado empleado;

    @Size(max = 100)
    @Column(name = "especialidad", length = 100)
    private String especialidad;

    @Size(max = 150)
    @Column(name = "certificacion", length = 150)
    private String certificacion;

    @Min(value = 0, message = "Los años de experiencia no pueden ser negativos")
    @Column(name = "anhos_experiencia")
    private Integer anhosExperiencia;
}