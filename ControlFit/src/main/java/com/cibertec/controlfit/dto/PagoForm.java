package com.cibertec.controlfit.dto;

import java.math.BigDecimal;

import com.cibertec.controlfit.model.enums.EstadoPago;
import com.cibertec.controlfit.model.enums.MetodoPago;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagoForm {

    @NotNull(message = "Seleccione la membresía.")
    private Integer idMembresia;

    @NotNull(message = "El monto del pago es obligatorio.")
    @Positive(message = "El monto del pago debe ser mayor a 0.")
    private BigDecimal monto;

    @NotNull(message = "El método de pago es obligatorio.")
    private MetodoPago metodoPago;

    private EstadoPago estado = EstadoPago.PAGADO;
}
