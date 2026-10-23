package com.cibertec.controlfit.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cibertec.controlfit.model.Cliente;
import com.cibertec.controlfit.model.Devolucion;
import com.cibertec.controlfit.model.Membresia;
import com.cibertec.controlfit.model.Pago;
import com.cibertec.controlfit.model.enums.EstadoDevolucion;
import com.cibertec.controlfit.model.enums.EstadoMembresia;
import com.cibertec.controlfit.model.enums.EstadoPago;
import com.cibertec.controlfit.service.pdf.ReporteDatos;
import com.cibertec.controlfit.service.pdf.ReportePdfGenerador;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final PagoService pagoService;
    private final MembresiaService membresiaService;
    private final DevolucionService devolucionService;
    private final ReportePdfGenerador generador;
    private final Clock clock;

    @Transactional(readOnly = true)
    public ReporteDatos.Pagos datosPagos(String generadoPor) {
        List<Pago> pagos = pagoService.obtenerTodosLosPagos();
        List<Devolucion> devoluciones = devolucionService.obtenerTodasLasDevoluciones();

        Map<Integer, BigDecimal> devueltoPorPago = new HashMap<>();
        for (Devolucion d : devoluciones) {
            if (d.getEstado() == EstadoDevolucion.COMPLETADA) {
                devueltoPorPago.merge(d.getPago().getIdPago(), d.getMonto(), BigDecimal::add);
            }
        }

        List<ReporteDatos.FilaPago> filas = new ArrayList<>();
        for (Pago p : pagos) {
            Cliente cliente = p.getMembresia().getCliente();
            filas.add(new ReporteDatos.FilaPago(
                    "#P" + p.getIdPago(),
                    "#M" + p.getMembresia().getIdMembresia(),
                    cliente.getNombres() + " " + cliente.getApellidos(),
                    cliente.getDni(),
                    p.getFechaPago(),
                    p.getMonto(),
                    devueltoPorPago.getOrDefault(p.getIdPago(), BigDecimal.ZERO),
                    p.getMetodoPago().name(),
                    p.getEstado().name()));
        }

        List<ReporteDatos.FilaDevolucion> filasDevolucion = new ArrayList<>();
        for (Devolucion d : devoluciones) {
            Cliente cliente = d.getPago().getMembresia().getCliente();
            filasDevolucion.add(new ReporteDatos.FilaDevolucion(
                    "#D" + d.getIdDevolucion(),
                    "#P" + d.getPago().getIdPago(),
                    cliente.getNombres() + " " + cliente.getApellidos(),
                    d.getFechaDevolucion(),
                    d.getMonto(),
                    d.getMotivo(),
                    d.getEstado().name()));
        }

        BigDecimal totalCobrado = pagos.stream()
                .filter(p -> p.getEstado() == EstadoPago.PAGADO)
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDevuelto = devoluciones.stream()
                .filter(d -> d.getEstado() == EstadoDevolucion.COMPLETADA)
                .map(Devolucion::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ingresosNetos = totalCobrado.subtract(totalDevuelto);

        List<ReporteDatos.ConteoEstado> porEstado = new ArrayList<>();
        for (EstadoPago estado : EstadoPago.values()) {
            List<Pago> delEstado = pagos.stream().filter(p -> p.getEstado() == estado).toList();
            BigDecimal monto = delEstado.stream().map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
            porEstado.add(new ReporteDatos.ConteoEstado(estado.name(), delEstado.size(), monto));
        }

        List<ReporteDatos.ConteoEstado> devolucionesPorEstado = new ArrayList<>();
        for (EstadoDevolucion estado : EstadoDevolucion.values()) {
            List<Devolucion> delEstado = devoluciones.stream().filter(d -> d.getEstado() == estado).toList();
            BigDecimal monto = delEstado.stream().map(Devolucion::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
            devolucionesPorEstado.add(new ReporteDatos.ConteoEstado(estado.name(), delEstado.size(), monto));
        }

        return new ReporteDatos.Pagos(LocalDateTime.now(clock), generadoPor, pagos.size(),
                totalCobrado, totalDevuelto, ingresosNetos, porEstado, devolucionesPorEstado,
                filas, filasDevolucion);
    }

    @Transactional(readOnly = true)
    public ReporteDatos.Membresias datosMembresias(String generadoPor) {
        List<Membresia> membresias = membresiaService.obtenerTodasLasMembresias();

        List<ReporteDatos.FilaMembresia> filas = new ArrayList<>();
        for (Membresia m : membresias) {
            Cliente cliente = m.getCliente();
            filas.add(new ReporteDatos.FilaMembresia(
                    "#M" + m.getIdMembresia(),
                    cliente.getNombres() + " " + cliente.getApellidos(),
                    cliente.getDni(),
                    m.getTipoMembresia().getNombre().name().replace('_', ' '),
                    m.getPromocion() != null ? m.getPromocion().getNombre() : "Sin promoción",
                    m.getFechaInicio(),
                    m.getFechaFin(),
                    m.getPrecioFinal(),
                    m.getEstado().name()));
        }

        List<ReporteDatos.ConteoEstado> porEstado = new ArrayList<>();
        for (EstadoMembresia estado : EstadoMembresia.values()) {
            int cantidad = (int) membresias.stream().filter(m -> m.getEstado() == estado).count();
            porEstado.add(new ReporteDatos.ConteoEstado(estado.name(), cantidad, BigDecimal.ZERO));
        }

        return new ReporteDatos.Membresias(LocalDateTime.now(clock), generadoPor, membresias.size(), porEstado, filas);
    }

    public byte[] generarPdfPagos(String generadoPor) {
        return generador.generarPagos(datosPagos(generadoPor));
    }

    public byte[] generarPdfMembresias(String generadoPor) {
        return generador.generarMembresias(datosMembresias(generadoPor));
    }
}
