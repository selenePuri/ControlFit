package com.cibertec.controlfit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cibertec.controlfit.config.AppConfig;
import com.cibertec.controlfit.model.Cliente;
import com.cibertec.controlfit.model.Membresia;
import com.cibertec.controlfit.model.Pago;
import com.cibertec.controlfit.model.Promocion;
import com.cibertec.controlfit.model.TipoMembresia;
import com.cibertec.controlfit.model.enums.EstadoMembresia;
import com.cibertec.controlfit.model.enums.EstadoPago;
import com.cibertec.controlfit.model.enums.MetodoPago;
import com.cibertec.controlfit.repository.ClienteRepository;
import com.cibertec.controlfit.repository.MembresiaRepository;
import com.cibertec.controlfit.repository.PagoRepository;

/** Reglas de negocio de la venta de membresías (sin base de datos). */
@ExtendWith(MockitoExtension.class)
class MembresiaServiceTest {

    @Mock MembresiaRepository membresiaRepository;
    @Mock PagoRepository pagoRepository;
    @Mock ClienteRepository clienteRepository;
    @Mock TipoMembresiaService tipoMembresiaService;
    @Mock PromocionService promocionService;

    MembresiaService service;
    Cliente cliente;
    TipoMembresia mensual;

    @BeforeEach
    void preparar() {
        Clock reloj = Clock.fixed(Instant.parse("2026-10-18T15:00:00Z"), AppConfig.ZONA_LIMA);
        service = new MembresiaService(membresiaRepository, pagoRepository, clienteRepository,
                tipoMembresiaService, promocionService, reloj);

        cliente = new Cliente();
        cliente.setIdCliente(1);
        cliente.setEstado(true);

        mensual = new TipoMembresia();
        mensual.setIdTipo(2);
        mensual.setDuracionDias(30);
        mensual.setPrecio(new BigDecimal("100.00"));
        mensual.setEstado(true);
    }

    private Membresia solicitud(LocalDate inicio, Integer idPromocion) {
        Cliente c = new Cliente();
        c.setIdCliente(1);
        TipoMembresia t = new TipoMembresia();
        t.setIdTipo(2);

        Membresia m = new Membresia();
        m.setCliente(c);
        m.setTipoMembresia(t);
        m.setFechaInicio(inicio);
        if (idPromocion != null) {
            Promocion p = new Promocion();
            p.setIdPromocion(idPromocion);
            m.setPromocion(p);
        }
        return m;
    }

    private void datosBase() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(tipoMembresiaService.obtenerTipoMembresiaHabilitado(2)).thenReturn(mensual);
    }

    @Test
    void ventaSinPromocionCalculaFechaFinYGeneraPagoPagado() {
        datosBase();
        when(membresiaRepository.contarSolapadas(any(), any(), any(), any())).thenReturn(0L);

        Membresia m = solicitud(LocalDate.of(2026, 10, 18), null);
        service.contratarMembresia(m, MetodoPago.EFECTIVO);

        assertEquals(LocalDate.of(2026, 11, 16), m.getFechaFin());   // 30 días contando el de inicio
        assertEquals(new BigDecimal("100.00"), m.getPrecioFinal());
        assertEquals(EstadoMembresia.ACTIVA, m.getEstado());

        ArgumentCaptor<Pago> captor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(captor.capture());
        assertEquals(new BigDecimal("100.00"), captor.getValue().getMonto());
        assertEquals(EstadoPago.PAGADO, captor.getValue().getEstado());
        assertEquals(MetodoPago.EFECTIVO, captor.getValue().getMetodoPago());
    }

    @Test
    void ventaConPromocionDescuentaEnElServidor() {
        datosBase();
        when(membresiaRepository.contarSolapadas(any(), any(), any(), any())).thenReturn(0L);

        Promocion promo = new Promocion();
        promo.setIdPromocion(9);
        when(promocionService.obtenerPromocionValida(eq(9), eq(mensual), any())).thenReturn(promo);
        when(promocionService.calcularDescuento(promo, mensual.getPrecio())).thenReturn(new BigDecimal("20.00"));

        Membresia m = solicitud(LocalDate.of(2026, 10, 18), 9);
        service.contratarMembresia(m, MetodoPago.YAPE);

        assertEquals(new BigDecimal("20.00"), m.getDescuento());
        assertEquals(new BigDecimal("80.00"), m.getPrecioFinal());
    }

    @Test
    void rechazaMembresiaQueSeSolapaConOtraActiva() {
        datosBase();
        when(membresiaRepository.contarSolapadas(any(), any(), any(), any())).thenReturn(1L);

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> service.contratarMembresia(solicitud(LocalDate.of(2026, 10, 18), null), MetodoPago.EFECTIVO));

        assertEquals("El cliente ya tiene una membresía activa durante ese período.", e.getMessage());
        verify(pagoRepository, never()).save(any());
    }

    @Test
    void rechazaPrecioFinalCeroPorRN_PA05() {
        datosBase();
        when(membresiaRepository.contarSolapadas(any(), any(), any(), any())).thenReturn(0L);

        Promocion promo = new Promocion();
        promo.setIdPromocion(9);
        when(promocionService.obtenerPromocionValida(eq(9), eq(mensual), any())).thenReturn(promo);
        when(promocionService.calcularDescuento(promo, mensual.getPrecio())).thenReturn(new BigDecimal("100.00"));

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> service.contratarMembresia(solicitud(LocalDate.of(2026, 10, 18), 9), MetodoPago.EFECTIVO));

        assertEquals("El precio final debe ser mayor a 0 para registrar el pago inicial.", e.getMessage());
        verify(membresiaRepository, never()).save(any());
    }

    @Test
    void ventaDesdeFormularioNoAceptaFechaPasada() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.venderMembresia(1, 2, null, LocalDate.of(2026, 10, 17), MetodoPago.EFECTIVO));

        assertEquals("La fecha de inicio no es válida (no puede ser anterior a hoy).", e.getMessage());
    }
}
