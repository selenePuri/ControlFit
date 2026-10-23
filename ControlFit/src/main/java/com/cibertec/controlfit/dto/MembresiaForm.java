package com.cibertec.controlfit.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.cibertec.controlfit.model.enums.MetodoPago;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** Venta de membresía + pago. El precio y el descuento NO vienen del navegador. */
@Getter
@Setter
public class MembresiaForm {

    @NotNull(message = "Seleccione un cliente y un tipo de membresía.")
    private Integer idCliente;

    @NotNull(message = "Seleccione un cliente y un tipo de membresía.")
    private Integer idTipo;

    private Integer idPromocion;

    @NotNull(message = "La fecha de inicio no es válida.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicio;

    @NotNull(message = "Seleccione un método de pago válido.")
    private MetodoPago metodoPago;
}
