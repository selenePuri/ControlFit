package com.cibertec.controlfit.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rutina_cliente")
@Getter
@Setter
@NoArgsConstructor
public class RutinaCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rutina_cliente")
    private Integer idRutinaCliente;

    @NotNull(message = "La rutina es obligatoria")
    @ManyToOne
    @JoinColumn(name = "id_rutina", nullable = false)
    private Rutina rutina;

    @NotNull(message = "El cliente es obligatorio")
    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @NotNull(message = "La fecha de asignación es obligatoria")
    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDate fechaAsignacion;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @NotNull
    @Column(name = "estado", nullable = false)
    private Boolean estado = true;
}