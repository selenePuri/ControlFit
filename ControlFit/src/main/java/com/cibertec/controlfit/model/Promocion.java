package com.cibertec.controlfit.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cibertec.controlfit.model.enums.TipoDescuento;

@Entity
@Table(name = "promocion")
@Getter
@Setter
@NoArgsConstructor
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_promocion")
    private Integer idPromocion;

    @NotBlank(message = "El nombre de la promoción es obligatorio")
    @Size(max = 80)
    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Size(max = 200)
    @Column(name = "descripcion", length = 200)
    private String descripcion;

    @NotNull(message = "El tipo de descuento es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_descuento", nullable = false, length = 20)
    private TipoDescuento tipoDescuento;

    @NotNull(message = "El valor del descuento es obligatorio")
    @DecimalMin(
        value = "0.0",
        inclusive = false,
        message = "El descuento debe ser mayor a cero"
    )
    @Column(
        name = "valor_descuento",
        nullable = false,
        precision = 10,
        scale = 2
    )
    private BigDecimal valorDescuento;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @NotNull
    @Column(name = "estado", nullable = false)
    private Boolean estado = true;

    @ManyToMany
    @JoinTable(
        name = "promocion_tipo_membresia",
        joinColumns = @JoinColumn(name = "id_promocion"),
        inverseJoinColumns = @JoinColumn(name = "id_tipo")
    )
    private List<TipoMembresia> tiposMembresia = new ArrayList<>();
}