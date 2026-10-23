package com.cibertec.controlfit.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.cibertec.controlfit.model.enums.TipoDescuento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromocionForm {

    @NotBlank(message = "El nombre de la promoción es obligatorio.")
    @Size(max = 80, message = "El nombre no puede superar 80 caracteres.")
    private String nombre;

    @Size(max = 200, message = "La descripción no puede superar 200 caracteres.")
    private String descripcion;

    @NotNull(message = "El tipo de descuento es obligatorio.")
    private TipoDescuento tipoDescuento;

    @NotNull(message = "El valor del descuento es obligatorio.")
    private BigDecimal valorDescuento;

    @NotNull(message = "Las fechas de la promoción son obligatorias.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicio;

    @NotNull(message = "Las fechas de la promoción son obligatorias.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaFin;

    private Boolean estado = Boolean.TRUE;

    /** ids de tipo_membresia a los que aplica (el formulario JSP original no lo pedía). */
    private List<Integer> tiposMembresia = new ArrayList<>();
}
