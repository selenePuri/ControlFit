package com.cibertec.controlfit.dto;

import java.math.BigDecimal;

import com.cibertec.controlfit.model.enums.EstadoDevolucion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DevolucionForm {

    @NotNull(message = "Selecciona el pago que se va a devolver.")
    private Integer idPago;

    @NotNull(message = "El importe de la devolución es obligatorio.")
    @DecimalMin(value = "0.01", message = "El importe de la devolución debe ser mayor a 0.")
    private BigDecimal monto;

    @Size(max = 200, message = "El motivo no puede superar 200 caracteres.")
    private String motivo;

    /** COMPLETADA (dinero ya entregado, por defecto) o PENDIENTE. */
    private EstadoDevolucion estado = EstadoDevolucion.COMPLETADA;
}
