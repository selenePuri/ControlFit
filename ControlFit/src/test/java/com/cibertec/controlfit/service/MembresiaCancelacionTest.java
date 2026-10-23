package com.cibertec.controlfit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cibertec.controlfit.model.Devolucion;
import com.cibertec.controlfit.model.Membresia;
import com.cibertec.controlfit.model.Pago;
import com.cibertec.controlfit.model.enums.EstadoMembresia;
import com.cibertec.controlfit.model.enums.EstadoPago;
import com.cibertec.controlfit.repository.ClienteRepository;
import com.cibertec.controlfit.repository.DevolucionRepository;
import com.cibertec.controlfit.repository.MembresiaRepository;
import com.cibertec.controlfit.repository.PagoRepository;

class MembresiaCancelacionTest {

    MembresiaRepository membresiaRepository;
    PagoRepository pagoRepository;
    DevolucionRepository devolucionRepository;
    MembresiaService membresiaService;
    DevolucionService devolucionService;

    @BeforeEach
    void preparar() {
        membresiaRepository = mock(MembresiaRepository.class);
        pagoRepository = mock(PagoRepository.class);
        devolucionRepository = mock(DevolucionRepository.class);
        Clock reloj = Clock.fixed(Instant.parse("2026-10-23T15:00:00Z"), ZoneId.of("America/Lima"));
        membresiaService = new MembresiaService(membresiaRepository, pagoRepository, mock(ClienteRepository.class),
                mock(TipoMembresiaService.class), mock(PromocionService.class), reloj);
        devolucionService = new DevolucionService(devolucionRepository, pagoRepository, reloj);
    }

    private Membresia membresia(EstadoMembresia estado) {
        Membresia m = new Membresia();
        m.setIdMembresia(10);
        m.setEstado(estado);
        when(membresiaRepository.findById(10)).thenReturn(Optional.of(m));
        return m;
    }

    @Test
    void cancelarCambiaSoloElEstadoYNoTocaPagosNiDevoluciones() {
        Membresia m = membresia(EstadoMembresia.ACTIVA);

        membresiaService.cancelarMembresia(10);

        assertEquals(EstadoMembresia.CANCELADA, m.getEstado());
        verify(membresiaRepository).save(m);
        verifyNoMoreInteractions(pagoRepository);
        verifyNoMoreInteractions(devolucionRepository);
    }

    @Test
    void tambienSePuedeCancelarUnaMembresiaPendiente() {
        Membresia m = membresia(EstadoMembresia.PENDIENTE);
        membresiaService.cancelarMembresia(10);
        assertEquals(EstadoMembresia.CANCELADA, m.getEstado());
    }

    @Test
    void noSeCancelaDosVecesNiUnaMembresiaVencida() {
        Membresia cancelada = membresia(EstadoMembresia.CANCELADA);
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> membresiaService.cancelarMembresia(10));
        assertTrue(e.getMessage().contains("ya está cancelada"));

        cancelada.setEstado(EstadoMembresia.VENCIDA);
        assertThrows(IllegalStateException.class, () -> membresiaService.cancelarMembresia(10));
        verify(membresiaRepository, never()).save(any());
    }

    @Test
    void cancelarYDevolverSonPasosIndependientesYElPagoOriginalNoCambia() {
        Membresia m = membresia(EstadoMembresia.ACTIVA);
        Pago pago = new Pago();
        pago.setIdPago(1);
        pago.setMembresia(m);
        pago.setMonto(new BigDecimal("100.00"));
        pago.setEstado(EstadoPago.PAGADO);
        when(pagoRepository.findWithLockByIdPago(1)).thenReturn(Optional.of(pago));
        when(devolucionRepository.findByPago_IdPago(1)).thenReturn(java.util.List.of());
        when(devolucionRepository.save(any(Devolucion.class))).thenAnswer(inv -> inv.getArgument(0));

        membresiaService.cancelarMembresia(10);
        verify(devolucionRepository, never()).save(any(Devolucion.class));

        devolucionService.registrarDevolucion(1, new BigDecimal("60.00"), "Cancelación", null);

        assertEquals(EstadoMembresia.CANCELADA, m.getEstado());
        assertEquals(EstadoPago.PAGADO, pago.getEstado());
        assertEquals(new BigDecimal("100.00"), pago.getMonto());
        verify(pagoRepository, never()).save(any(Pago.class));
    }
}
