package com.cibertec.controlfit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.cibertec.controlfit.model.Cliente;
import com.cibertec.controlfit.model.Devolucion;
import com.cibertec.controlfit.model.Membresia;
import com.cibertec.controlfit.model.Pago;
import com.cibertec.controlfit.model.enums.EstadoDevolucion;
import com.cibertec.controlfit.model.enums.EstadoPago;
import com.cibertec.controlfit.model.enums.MetodoPago;
import com.cibertec.controlfit.service.pdf.ReporteDatos;
import com.cibertec.controlfit.service.pdf.ReportePdfGenerador;

class ReporteServiceIngresosNetosTest {

    private Pago pago(int id, String monto, EstadoPago estado, Membresia m) {
        Pago p = new Pago();
        p.setIdPago(id);
        p.setMembresia(m);
        p.setMonto(new BigDecimal(monto));
        p.setEstado(estado);
        p.setMetodoPago(MetodoPago.EFECTIVO);
        p.setFechaPago(LocalDateTime.of(2026, 10, 1, 10, 0));
        return p;
    }

    private Devolucion devolucion(int id, Pago pago, String monto, EstadoDevolucion estado) {
        Devolucion d = new Devolucion();
        d.setIdDevolucion(id);
        d.setPago(pago);
        d.setMonto(new BigDecimal(monto));
        d.setEstado(estado);
        d.setFechaDevolucion(LocalDateTime.of(2026, 10, 5, 12, 0));
        return d;
    }

    @Test
    void soloLasDevolucionesCompletadasRestanDelTotalCobrado() {
        Cliente c = new Cliente();
        c.setNombres("Luis");
        c.setApellidos("Gómez");
        c.setDni("22222222");
        Membresia m = new Membresia();
        m.setIdMembresia(1);
        m.setCliente(c);

        Pago p1 = pago(1, "100.00", EstadoPago.PAGADO, m);
        Pago p2 = pago(2, "50.00", EstadoPago.PAGADO, m);
        Pago p3 = pago(3, "80.00", EstadoPago.PENDIENTE, m);   
        Pago p4 = pago(4, "20.00", EstadoPago.ANULADO, m);     

        PagoService pagoService = mock(PagoService.class);
        DevolucionService devolucionService = mock(DevolucionService.class);
        when(pagoService.obtenerTodosLosPagos()).thenReturn(List.of(p1, p2, p3, p4));
        when(devolucionService.obtenerTodasLasDevoluciones()).thenReturn(List.of(
                devolucion(1, p1, "30.00", EstadoDevolucion.COMPLETADA),
                devolucion(2, p1, "10.00", EstadoDevolucion.COMPLETADA),
                devolucion(3, p2, "20.00", EstadoDevolucion.PENDIENTE),   
                devolucion(4, p2, "15.00", EstadoDevolucion.ANULADA)));   

        ReporteService service = new ReporteService(pagoService, mock(MembresiaService.class), devolucionService,
                new ReportePdfGenerador(), Clock.fixed(Instant.parse("2026-10-23T15:00:00Z"), ZoneId.of("America/Lima")));

        ReporteDatos.Pagos datos = service.datosPagos("admin");

        assertEquals(new BigDecimal("150.00"), datos.totalCobrado());     
        assertEquals(new BigDecimal("40.00"), datos.totalDevuelto());     
        assertEquals(new BigDecimal("110.00"), datos.ingresosNetos());    
        assertEquals(4, datos.cantidad());
        assertEquals(new BigDecimal("40.00"), datos.filas().get(0).devuelto());   
        assertEquals(0, datos.filas().get(1).devuelto().signum());                
        assertEquals(4, datos.devoluciones().size());                             

        ReporteDatos.ConteoEstado pendientes = datos.devolucionesPorEstado().stream()
                .filter(e -> e.estado().equals("PENDIENTE")).findFirst().orElseThrow();
        assertEquals(new BigDecimal("20.00"), pendientes.monto());
    }

    @Test
    void sinDevolucionesLosIngresosNetosIgualanAlTotalCobrado() {
        Membresia m = new Membresia();
        m.setIdMembresia(1);
        Cliente c = new Cliente();
        c.setNombres("Ana");
        c.setApellidos("Torres");
        m.setCliente(c);
        PagoService pagoService = mock(PagoService.class);
        DevolucionService devolucionService = mock(DevolucionService.class);
        when(pagoService.obtenerTodosLosPagos()).thenReturn(List.of(pago(1, "100.00", EstadoPago.PAGADO, m)));
        when(devolucionService.obtenerTodasLasDevoluciones()).thenReturn(List.of());

        ReporteService service = new ReporteService(pagoService, mock(MembresiaService.class), devolucionService,
                new ReportePdfGenerador(), Clock.systemDefaultZone());

        ReporteDatos.Pagos datos = service.datosPagos("admin");

        assertEquals(new BigDecimal("100.00"), datos.ingresosNetos());
        assertEquals(0, datos.totalDevuelto().signum());
    }
}
