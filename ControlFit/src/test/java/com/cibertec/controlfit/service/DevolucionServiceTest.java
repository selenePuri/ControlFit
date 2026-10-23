package com.cibertec.controlfit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cibertec.controlfit.model.Devolucion;
import com.cibertec.controlfit.model.Pago;
import com.cibertec.controlfit.model.enums.EstadoDevolucion;
import com.cibertec.controlfit.model.enums.EstadoPago;
import com.cibertec.controlfit.repository.DevolucionRepository;
import com.cibertec.controlfit.repository.PagoRepository;

class DevolucionServiceTest {

    static final ZoneId LIMA = ZoneId.of("America/Lima");

    DevolucionRepository devolucionRepository;
    PagoRepository pagoRepository;
    DevolucionService service;
    List<Devolucion> tabla;        
    Pago pago;                    

    @BeforeEach
    void preparar() {
        devolucionRepository = mock(DevolucionRepository.class);
        pagoRepository = mock(PagoRepository.class);
        Clock reloj = Clock.fixed(Instant.parse("2026-10-23T15:00:00Z"), LIMA);
        service = new DevolucionService(devolucionRepository, pagoRepository, reloj);

        tabla = new ArrayList<>();
        pago = pago(1, "100.00", EstadoPago.PAGADO);
        when(pagoRepository.findWithLockByIdPago(1)).thenReturn(Optional.of(pago));
        when(pagoRepository.findById(1)).thenReturn(Optional.of(pago));
        when(devolucionRepository.findByPago_IdPago(1)).thenAnswer(inv -> new ArrayList<>(tabla));
        when(devolucionRepository.findAll()).thenAnswer(inv -> new ArrayList<>(tabla));
        when(devolucionRepository.save(any(Devolucion.class))).thenAnswer(inv -> {
            Devolucion d = inv.getArgument(0);
            if (d.getIdDevolucion() == null) {
                d.setIdDevolucion(tabla.size() + 1);
                tabla.add(d);
            }
            return d;
        });
    }

    private static Pago pago(int id, String monto, EstadoPago estado) {
        Pago p = new Pago();
        p.setIdPago(id);
        p.setMonto(new BigDecimal(monto));
        p.setEstado(estado);
        p.setFechaPago(LocalDateTime.of(2026, 10, 1, 10, 0));
        return p;
    }

    private Devolucion existente(String monto, EstadoDevolucion estado) {
        Devolucion d = new Devolucion();
        d.setIdDevolucion(tabla.size() + 1);
        d.setPago(pago);
        d.setMonto(new BigDecimal(monto));
        d.setEstado(estado);
        d.setFechaDevolucion(LocalDateTime.of(2026, 10, 2, 9, 0));
        tabla.add(d);
        return d;
    }


    @Test
    void devolucionParcialGuardaImporteFechaYEstadoYDejaSaldo() {
        Devolucion d = service.registrarDevolucion(1, new BigDecimal("40.00"), "  Cambio de plan ", null);

        assertEquals(new BigDecimal("40.00"), d.getMonto());
        assertEquals(EstadoDevolucion.COMPLETADA, d.getEstado());
        assertEquals("Cambio de plan", d.getMotivo());
        assertEquals(LocalDateTime.of(2026, 10, 23, 10, 0), d.getFechaDevolucion());   
        assertEquals(new BigDecimal("60.00"), service.calcularSaldoDisponible(1));
        verify(pagoRepository, never()).save(any(Pago.class));
        assertEquals(EstadoPago.PAGADO, pago.getEstado());
        assertEquals(new BigDecimal("100.00"), pago.getMonto());
    }

    @Test
    void devolucionTotalDejaSaldoCeroYNoPermiteDevolverDeNuevo() {
        service.registrarDevolucion(1, new BigDecimal("100.00"), null, EstadoDevolucion.COMPLETADA);

        assertEquals(0, service.calcularSaldoDisponible(1).signum());
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.registrarDevolucion(1, new BigDecimal("100.00"), null, null));
        assertTrue(e.getMessage().contains("ya fue devuelto por completo"));
        assertEquals(1, tabla.size());   
        verify(pagoRepository, never()).save(any(Pago.class));
        assertEquals(EstadoPago.PAGADO, pago.getEstado());
    }

    @Test
    void devolucionesParcialesSeAcumulanHastaElTotal() {
        service.registrarDevolucion(1, new BigDecimal("30.00"), null, null);
        service.registrarDevolucion(1, new BigDecimal("70.00"), null, null);

        assertEquals(2, tabla.size());
        assertEquals(0, service.calcularSaldoDisponible(1).signum());
    }


    @Test
    void rechazaUnImporteQueSuperaElSaldoDisponible() {
        existente("70.00", EstadoDevolucion.COMPLETADA);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.registrarDevolucion(1, new BigDecimal("40.00"), null, null));

        assertTrue(e.getMessage().contains("supera el saldo disponible"));
        assertTrue(e.getMessage().contains("30.00"));
        assertEquals(1, tabla.size());
    }

    @Test
    void unaDevolucionPendienteReservaSaldoYUnaAnuladaLoLibera() {
        Devolucion pendiente = existente("60.00", EstadoDevolucion.PENDIENTE);

        assertThrows(IllegalArgumentException.class,
                () -> service.registrarDevolucion(1, new BigDecimal("50.00"), null, null));   

        when(devolucionRepository.findWithLockByIdDevolucion(1)).thenReturn(Optional.of(pendiente));
        service.anularDevolucion(pendiente.getIdDevolucion());
        service.registrarDevolucion(1, new BigDecimal("50.00"), null, null);
        assertEquals(new BigDecimal("50.00"), service.calcularSaldoDisponible(1));
    }


    @Test
    void rechazaImportesNulosCeroNegativosOConMasDeDosDecimales() {
        assertThrows(IllegalArgumentException.class, () -> service.registrarDevolucion(1, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> service.registrarDevolucion(1, BigDecimal.ZERO, null, null));
        assertThrows(IllegalArgumentException.class, () -> service.registrarDevolucion(1, new BigDecimal("-5.00"), null, null));
        IllegalArgumentException decimales = assertThrows(IllegalArgumentException.class,
                () -> service.registrarDevolucion(1, new BigDecimal("10.555"), null, null));
        assertTrue(decimales.getMessage().contains("2 decimales"));
        assertThrows(IllegalArgumentException.class, () -> service.registrarDevolucion(null, new BigDecimal("10"), null, null));
        assertThrows(IllegalArgumentException.class,
                () -> service.registrarDevolucion(1, new BigDecimal("10"), null, EstadoDevolucion.ANULADA));
        assertEquals(0, tabla.size());
    }

    @Test
    void soloSeDevuelvenPagosPagadosYExistentes() {
        Pago pendiente = pago(2, "50.00", EstadoPago.PENDIENTE);
        when(pagoRepository.findWithLockByIdPago(2)).thenReturn(Optional.of(pendiente));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.registrarDevolucion(2, new BigDecimal("10.00"), null, null));
        assertTrue(e.getMessage().contains("PAGADO"));

        when(pagoRepository.findWithLockByIdPago(99)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.registrarDevolucion(99, new BigDecimal("10.00"), null, null));
        assertEquals(0, tabla.size());
    }


    @Test
    void unaDevolucionSoloSePuedeCompletarUnaVez() {
        Devolucion d = existente("20.00", EstadoDevolucion.PENDIENTE);
        when(devolucionRepository.findWithLockByIdDevolucion(1)).thenReturn(Optional.of(d));

        service.completarDevolucion(1);
        assertEquals(EstadoDevolucion.COMPLETADA, d.getEstado());

        IllegalStateException otra = assertThrows(IllegalStateException.class, () -> service.completarDevolucion(1));
        assertTrue(otra.getMessage().contains("ya está COMPLETADA"));
    }

    @Test
    void unaDevolucionCompletadaNoSeAnulaNiUnaAnuladaSeCompleta() {
        Devolucion hecha = existente("20.00", EstadoDevolucion.COMPLETADA);
        Devolucion descartada = existente("10.00", EstadoDevolucion.ANULADA);
        when(devolucionRepository.findWithLockByIdDevolucion(1)).thenReturn(Optional.of(hecha));
        when(devolucionRepository.findWithLockByIdDevolucion(2)).thenReturn(Optional.of(descartada));

        assertThrows(IllegalStateException.class, () -> service.anularDevolucion(1));
        assertThrows(IllegalStateException.class, () -> service.completarDevolucion(2));
        assertThrows(IllegalStateException.class, () -> service.anularDevolucion(2));
        assertEquals(EstadoDevolucion.COMPLETADA, hecha.getEstado());
        assertEquals(EstadoDevolucion.ANULADA, descartada.getEstado());
    }


    @Test
    void saldosPorPagoSeparaDevueltoPendienteYSaldo() {
        existente("30.00", EstadoDevolucion.COMPLETADA);
        existente("20.00", EstadoDevolucion.PENDIENTE);
        existente("10.00", EstadoDevolucion.ANULADA);
        Pago anulado = pago(3, "80.00", EstadoPago.ANULADO);

        Map<Integer, DevolucionService.SaldoPago> saldos = service.saldosPorPago(List.of(pago, anulado));

        assertEquals(new BigDecimal("30.00"), saldos.get(1).devuelto());
        assertEquals(new BigDecimal("20.00"), saldos.get(1).pendiente());
        assertEquals(new BigDecimal("50.00"), saldos.get(1).saldo());
        assertEquals(0, saldos.get(3).saldo().signum());   
    }
}
