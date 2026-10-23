package com.cibertec.controlfit.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cibertec.controlfit.model.enums.TipoPlan;

@Entity
@Table(name = "tipo_membresia")
@Getter
@Setter
@NoArgsConstructor
public class TipoMembresia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo")
    private Integer idTipo;

    @NotNull(message = "El tipo de plan es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "nombre", nullable = false, unique = true, length = 20)
    private TipoPlan nombre;

    @Size(max = 200)
    @Column(name = "descripcion", length = 200)
    private String descripcion;

    @NotNull(message = "La duración es obligatoria")
    @Positive(message = "La duración debe ser mayor a cero")
    @Column(name = "duracion_dias", nullable = false)
    private Integer duracionDias;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a cero")
    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @NotNull
    @Column(name = "estado", nullable = false)
    private Boolean estado = true;
}