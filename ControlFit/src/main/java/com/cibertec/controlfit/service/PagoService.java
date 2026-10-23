package com.cibertec.controlfit.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cibertec.controlfit.model.Membresia;
import com.cibertec.controlfit.model.Pago;
import com.cibertec.controlfit.model.enums.EstadoPago;
import com.cibertec.controlfit.model.enums.MetodoPago;
import com.cibertec.controlfit.repository.MembresiaRepository;
import com.cibertec.controlfit.repository.PagoRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final MembresiaRepository membresiaRepository;
    private final Clock clock;

    public void registrarPago(Pago pago) {

        Membresia membresia = validarPago(pago);

        pago.setMembresia(membresia);

        if (pago.getFechaPago() == null) {
            pago.setFechaPago(LocalDateTime.now(clock));
        }

        if (pago.getEstado() == null) {
            pago.setEstado(EstadoPago.PAGADO);
        }

        pagoRepository.save(pago);
    }

    public void actualizarPago(Pago pago) {

        if (pago == null || pago.getIdPago() == null || pago.getIdPago() <= 0) {
            throw new IllegalArgumentException(
                    "El pago que desea actualizar no es válido.");
        }

        Pago existente = pagoRepository.findById(pago.getIdPago())
                .orElseThrow(() -> new IllegalArgumentException("El pago no existe."));

        Membresia membresia = validarPago(pago);

        pago.setMembresia(membresia);

        if (pago.getFechaPago() == null) {
            pago.setFechaPago(existente.getFechaPago());
        }

        if (pago.getEstado() == null) {
            pago.setEstado(existente.getEstado());
        }

        pagoRepository.save(pago);
    }

    @Transactional(readOnly = true)
    public Pago obtenerPagoPorId(Integer idPago) {

        validarIdPago(idPago);

        return pagoRepository.findById(idPago)
                .orElseThrow(() -> new IllegalArgumentException("El pago no existe."));
    }

    @Transactional(readOnly = true)
    public List<Pago> obtenerTodosLosPagos() {
        return pagoRepository.findAllByOrderByFechaPagoDescIdPagoDesc();
    }

    @Transactional(readOnly = true)
    public List<Pago> obtenerPagosPorMembresia(Integer idMembresia) {

        Membresia membresia = obtenerMembresiaExistente(idMembresia);

        return pagoRepository.findByMembresia_IdMembresia(membresia.getIdMembresia());
    }

    @Transactional(readOnly = true)
    public List<Pago> obtenerPagosPorEstado(EstadoPago estado) {

        if (estado == null) {
            throw new IllegalArgumentException("El estado del pago es obligatorio.");
        }

        return pagoRepository.findByEstado(estado);
    }

    @Transactional(readOnly = true)
    public List<Pago> obtenerUltimosPagos() {
        return pagoRepository.findTop8ByOrderByFechaPagoDescIdPagoDesc();
    }

    @Transactional(readOnly = true)
    public List<Pago> obtenerUltimosPagosDeCliente(Integer idCliente) {
        return pagoRepository.findTop10ByMembresia_Cliente_IdClienteOrderByFechaPagoDesc(idCliente);
    }

    private Membresia validarPago(Pago pago) {

        if (pago == null) {
            throw new IllegalArgumentException("El pago es obligatorio.");
        }

        if (pago.getMembresia() == null
                || pago.getMembresia().getIdMembresia() == null
                || pago.getMembresia().getIdMembresia() <= 0) {

            throw new IllegalArgumentException(
                    "El pago debe estar asociado a una membresía válida.");
        }

        Membresia membresia = obtenerMembresiaExistente(pago.getMembresia().getIdMembresia());

        validarMonto(pago.getMonto());
        validarMetodoPago(pago.getMetodoPago());

        return membresia;
    }

    private Membresia obtenerMembresiaExistente(Integer idMembresia) {

        if (idMembresia == null || idMembresia <= 0) {
            throw new IllegalArgumentException("El código de la membresía no es válido.");
        }

        return membresiaRepository.findById(idMembresia)
                .orElseThrow(() -> new IllegalArgumentException("La membresía no existe."));
    }

    private void validarMonto(BigDecimal monto) {

        if (monto == null) {
            throw new IllegalArgumentException("El monto del pago es obligatorio.");
        }

        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a 0.");
        }
    }

    private void validarMetodoPago(MetodoPago metodoPago) {

        if (metodoPago == null) {
            throw new IllegalArgumentException("El método de pago es obligatorio.");
        }
    }

    private void validarIdPago(Integer idPago) {

        if (idPago == null || idPago <= 0) {
            throw new IllegalArgumentException("El código del pago no es válido.");
        }
    }
}
