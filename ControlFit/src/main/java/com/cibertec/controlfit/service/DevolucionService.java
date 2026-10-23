package com.cibertec.controlfit.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cibertec.controlfit.model.Devolucion;
import com.cibertec.controlfit.model.Pago;
import com.cibertec.controlfit.model.enums.EstadoDevolucion;
import com.cibertec.controlfit.model.enums.EstadoPago;
import com.cibertec.controlfit.repository.DevolucionRepository;
import com.cibertec.controlfit.repository.PagoRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class DevolucionService {

    private final DevolucionRepository devolucionRepository;
    private final PagoRepository pagoRepository;
    private final Clock clock;

    public record SaldoPago(BigDecimal devuelto, BigDecimal pendiente, BigDecimal saldo) {
    }

    
    public Devolucion registrarDevolucion(Integer idPago, BigDecimal monto, String motivo,
                                          EstadoDevolucion estadoInicial) {

        if (idPago == null || idPago <= 0) {
            throw new IllegalArgumentException("Selecciona el pago que se va a devolver.");
        }
        validarImporte(monto);

        EstadoDevolucion estado = estadoInicial == null ? EstadoDevolucion.COMPLETADA : estadoInicial;
        if (estado == EstadoDevolucion.ANULADA) {
            throw new IllegalArgumentException("Una devolución nueva no puede registrarse como ANULADA.");
        }

        String motivoLimpio = motivo == null || motivo.isBlank() ? null : motivo.trim();
        if (motivoLimpio != null && motivoLimpio.length() > 200) {
            throw new IllegalArgumentException("El motivo no puede superar 200 caracteres.");
        }

        Pago pago = pagoRepository.findWithLockByIdPago(idPago)
                .orElseThrow(() -> new IllegalArgumentException("El pago no existe."));

        if (pago.getEstado() != EstadoPago.PAGADO) {
            throw new IllegalArgumentException("Solo se pueden devolver pagos en estado PAGADO.");
        }

        BigDecimal saldo = calcularSaldo(pago);
        if (saldo.signum() <= 0) {
            throw new IllegalArgumentException("Este pago ya fue devuelto por completo.");
        }
        if (monto.compareTo(saldo) > 0) {
            throw new IllegalArgumentException("El importe (S/ " + soles(monto)
                    + ") supera el saldo disponible del pago (S/ " + soles(saldo) + ").");
        }

        Devolucion devolucion = new Devolucion();
        devolucion.setPago(pago);
        devolucion.setMonto(monto);
        devolucion.setMotivo(motivoLimpio);
        devolucion.setEstado(estado);
        devolucion.setFechaDevolucion(LocalDateTime.now(clock));
        return devolucionRepository.save(devolucion);
    }


    public void completarDevolucion(Integer idDevolucion) {
        Devolucion devolucion = obtenerConBloqueo(idDevolucion);

        if (devolucion.getEstado() != EstadoDevolucion.PENDIENTE) {
            throw new IllegalStateException("La devolución ya está " + devolucion.getEstado()
                    + "; solo se pueden completar las pendientes.");
        }

        devolucion.setEstado(EstadoDevolucion.COMPLETADA);
        devolucionRepository.save(devolucion);
    }

    public void anularDevolucion(Integer idDevolucion) {
        Devolucion devolucion = obtenerConBloqueo(idDevolucion);

        if (devolucion.getEstado() != EstadoDevolucion.PENDIENTE) {
            throw new IllegalStateException("La devolución ya está " + devolucion.getEstado()
                    + "; solo se pueden anular las pendientes.");
        }

        devolucion.setEstado(EstadoDevolucion.ANULADA);
        devolucionRepository.save(devolucion);
    }


    @Transactional(readOnly = true)
    public List<Devolucion> obtenerTodasLasDevoluciones() {
        return devolucionRepository.findAllByOrderByFechaDevolucionDescIdDevolucionDesc();
    }

    @Transactional(readOnly = true)
    public List<Devolucion> obtenerDevolucionesDePago(Integer idPago) {
        return devolucionRepository.findByPago_IdPago(idPago);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoDisponible(Integer idPago) {
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new IllegalArgumentException("El pago no existe."));
        return calcularSaldo(pago);
    }

    @Transactional(readOnly = true)
    public Map<Integer, SaldoPago> saldosPorPago(List<Pago> pagos) {
        Map<Integer, BigDecimal> completado = new HashMap<>();
        Map<Integer, BigDecimal> pendiente = new HashMap<>();
        for (Devolucion d : devolucionRepository.findAll()) {
            Map<Integer, BigDecimal> destino = switch (d.getEstado()) {
                case COMPLETADA -> completado;
                case PENDIENTE -> pendiente;
                case ANULADA -> null;
            };
            if (destino != null) {
                destino.merge(d.getPago().getIdPago(), d.getMonto(), BigDecimal::add);
            }
        }

        Map<Integer, SaldoPago> resultado = new HashMap<>();
        for (Pago p : pagos) {
            BigDecimal hecho = completado.getOrDefault(p.getIdPago(), BigDecimal.ZERO);
            BigDecimal pend = pendiente.getOrDefault(p.getIdPago(), BigDecimal.ZERO);
            BigDecimal saldo = p.getEstado() == EstadoPago.PAGADO
                    ? p.getMonto().subtract(hecho).subtract(pend).max(BigDecimal.ZERO)
                    : BigDecimal.ZERO;
            resultado.put(p.getIdPago(), new SaldoPago(hecho, pend, saldo));
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public List<Pago> obtenerPagosDevolubles() {
        List<Pago> pagados = pagoRepository.findByEstado(EstadoPago.PAGADO);
        Map<Integer, SaldoPago> saldos = saldosPorPago(pagados);
        return pagados.stream()
                .filter(p -> saldos.get(p.getIdPago()).saldo().signum() > 0)
                .sorted((a, b) -> b.getFechaPago().compareTo(a.getFechaPago()))
                .toList();
    }

    private Devolucion obtenerConBloqueo(Integer idDevolucion) {
        if (idDevolucion == null || idDevolucion <= 0) {
            throw new IllegalArgumentException("El código de la devolución no es válido.");
        }
        return devolucionRepository.findWithLockByIdDevolucion(idDevolucion)
                .orElseThrow(() -> new IllegalArgumentException("La devolución no existe."));
    }

    private BigDecimal calcularSaldo(Pago pago) {
        if (pago.getEstado() != EstadoPago.PAGADO) {
            return BigDecimal.ZERO;
        }
        BigDecimal comprometido = devolucionRepository.findByPago_IdPago(pago.getIdPago()).stream()
                .filter(d -> d.getEstado() != EstadoDevolucion.ANULADA)
                .map(Devolucion::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return pago.getMonto().subtract(comprometido).max(BigDecimal.ZERO);
    }

    private static void validarImporte(BigDecimal monto) {
        if (monto == null) {
            throw new IllegalArgumentException("El importe de la devolución es obligatorio.");
        }
        if (monto.signum() <= 0) {
            throw new IllegalArgumentException("El importe de la devolución debe ser mayor a 0.");
        }
        if (monto.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("El importe admite como máximo 2 decimales.");
        }
    }

    private static String soles(BigDecimal monto) {
        return String.format(Locale.US, "%,.2f", monto);
    }
}
