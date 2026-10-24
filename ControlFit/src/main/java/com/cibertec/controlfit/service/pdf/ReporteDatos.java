package com.cibertec.controlfit.service.pdf;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class ReporteDatos {
//generar pdf
    private ReporteDatos() {
    }

    public record ConteoEstado(String estado, int cantidad, BigDecimal monto) {
    }

    public record FilaPago(String idPago, String idMembresia, String cliente, String dni,
                           LocalDateTime fechaPago, BigDecimal monto, BigDecimal devuelto, String metodo, String estado) {
    }

    public record FilaDevolucion(String id, String idPago, String cliente, LocalDateTime fecha,
                                 BigDecimal monto, String motivo, String estado) {
    }

    public record Pagos(LocalDateTime generadoEn, String generadoPor, int cantidad,
                        BigDecimal totalCobrado, BigDecimal totalDevuelto, BigDecimal ingresosNetos,
                        List<ConteoEstado> porEstado, List<ConteoEstado> devolucionesPorEstado,
                        List<FilaPago> filas, List<FilaDevolucion> devoluciones) {
    }

    public record FilaMembresia(String id, String cliente, String dni, String plan, String promocion,
                                LocalDate inicio, LocalDate fin, BigDecimal precioFinal, String estado) {
    }

    public record Membresias(LocalDateTime generadoEn, String generadoPor, int total,
                             List<ConteoEstado> porEstado, List<FilaMembresia> filas) {
    }
}
