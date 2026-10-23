package com.cibertec.controlfit.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rutina")
@Getter
@Setter
@NoArgsConstructor
public class Rutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rutina")
    private Integer idRutina;

    @NotNull(message = "El entrenador es obligatorio")
    @ManyToOne
    @JoinColumn(name = "id_entrenador", nullable = false)
    private Entrenador entrenador;

    @NotBlank(message = "El nombre de la rutina es obligatorio")
    @Size(max = 80)
    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Size(max = 255)
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Size(max = 100)
    @Column(name = "objetivo", length = 100)
    private String objetivo;

    @NotNull(message = "La fecha de creación es obligatoria")
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion;

    @NotNull
    @Column(name = "estado", nullable = false)
    private Boolean estado = true;
}