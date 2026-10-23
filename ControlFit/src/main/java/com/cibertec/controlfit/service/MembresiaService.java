package com.cibertec.controlfit.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class MembresiaService {

    private final MembresiaRepository membresiaRepository;
    private final PagoRepository pagoRepository;
    private final ClienteRepository clienteRepository;
    private final TipoMembresiaService tipoMembresiaService;
    private final PromocionService promocionService;
    private final Clock clock;

    public void contratarMembresia(Membresia membresia, MetodoPago metodoPago) {

        if (membresia == null) {
            throw new IllegalArgumentException("La membresía es obligatoria.");
        }

        if (metodoPago == null) {
            throw new IllegalArgumentException("El método de pago es obligatorio.");
        }

        Cliente cliente = obtenerClienteValido(membresia.getCliente());

        TipoMembresia tipo = obtenerTipoMembresiaValido(membresia.getTipoMembresia());

        if (tipo.getDuracionDias() == null || tipo.getDuracionDias() <= 0) {
            throw new IllegalStateException(
                    "La duración del tipo de membresía no es válida.");
        }

        if (tipo.getPrecio() == null || tipo.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "El precio del tipo de membresía no es válido.");
        }

        LocalDate fechaInicio = membresia.getFechaInicio();

        if (fechaInicio == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria.");
        }

        LocalDate fechaFin = fechaInicio.plusDays(tipo.getDuracionDias() - 1L);

        boolean existeSolapamiento = membresiaRepository.contarSolapadas(
                cliente.getIdCliente(),
                EstadoMembresia.ACTIVA,
                fechaInicio,
                fechaFin) > 0;

        if (existeSolapamiento) {
            throw new IllegalStateException(
                    "El cliente ya tiene una membresía activa durante ese período.");
        }

        BigDecimal precioBase = tipo.getPrecio();
        BigDecimal descuento = BigDecimal.ZERO;
        Promocion promocion = null;

        if (membresia.getPromocion() != null
                && membresia.getPromocion().getIdPromocion() != null) {

            promocion = promocionService.obtenerPromocionValida(
                    membresia.getPromocion().getIdPromocion(),
                    tipo,
                    fechaInicio);

            descuento = promocionService.calcularDescuento(promocion, precioBase);
        }

        BigDecimal precioFinal = precioBase.subtract(descuento);

        if (precioFinal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "El precio final de la membresía no puede ser negativo.");
        }

        // RN-PA05
        if (precioFinal.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalStateException(
                    "El precio final debe ser mayor a 0 para registrar el pago inicial.");
        }

        membresia.setCliente(cliente);
        membresia.setTipoMembresia(tipo);
        membresia.setPromocion(promocion);
        membresia.setFechaInicio(fechaInicio);
        membresia.setFechaFin(fechaFin);
        membresia.setPrecioBase(precioBase);
        membresia.setDescuento(descuento);
        membresia.setPrecioFinal(precioFinal);
        membresia.setEstado(EstadoMembresia.ACTIVA);

        Pago pago = new Pago();
        pago.setMembresia(membresia);
        pago.setFechaPago(LocalDateTime.now(clock));
        pago.setMonto(precioFinal);
        pago.setMetodoPago(metodoPago);
        pago.setEstado(EstadoPago.PAGADO);

        membresiaRepository.save(membresia);
        pagoRepository.save(pago);
    }

    public Membresia venderMembresia(Integer idCliente,
                                     Integer idTipo,
                                     Integer idPromocion,
                                     LocalDate fechaInicio,
                                     MetodoPago metodoPago) {

        if (idCliente == null || idTipo == null) {
            throw new IllegalArgumentException(
                    "Seleccione un cliente y un tipo de membresía.");
        }

        if (metodoPago == null) {
            throw new IllegalArgumentException("Seleccione un método de pago válido.");
        }

        if (fechaInicio == null || fechaInicio.isBefore(LocalDate.now(clock))) {
            throw new IllegalArgumentException(
                    "La fecha de inicio no es válida (no puede ser anterior a hoy).");
        }

        Cliente cliente = new Cliente();
        cliente.setIdCliente(idCliente);

        TipoMembresia tipo = new TipoMembresia();
        tipo.setIdTipo(idTipo);

        Membresia membresia = new Membresia();
        membresia.setCliente(cliente);
        membresia.setTipoMembresia(tipo);
        membresia.setFechaInicio(fechaInicio);

        if (idPromocion != null) {
            Promocion promocion = new Promocion();
            promocion.setIdPromocion(idPromocion);
            membresia.setPromocion(promocion);
        }

        contratarMembresia(membresia, metodoPago);

        return membresia;
    }

    @Transactional(readOnly = true)
    public Membresia obtenerMembresiaPorId(Integer idMembresia) {

        if (idMembresia == null || idMembresia <= 0) {
            throw new IllegalArgumentException("El código de la membresía no es válido.");
        }

        return membresiaRepository.findById(idMembresia)
                .orElseThrow(() -> new IllegalArgumentException("La membresía no existe."));
    }

    @Transactional(readOnly = true)
    public List<Membresia> obtenerTodasLasMembresias() {
        return membresiaRepository.findAllByOrderByIdMembresiaDesc();
    }

    @Transactional(readOnly = true)
    public List<Membresia> obtenerMembresiasPorCliente(Integer idCliente) {

        if (idCliente == null || idCliente <= 0) {
            throw new IllegalArgumentException("El código del cliente no es válido.");
        }

        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("El cliente no existe."));

        return membresiaRepository.findByCliente_IdCliente(cliente.getIdCliente());
    }

    @Transactional(readOnly = true)
    public List<Membresia> obtenerMembresiasPorEstado(EstadoMembresia estado) {

        if (estado == null) {
            throw new IllegalArgumentException(
                    "El estado de la membresía es obligatorio.");
        }

        return membresiaRepository.findByEstado(estado);
    }

    @Transactional(readOnly = true)
    public Membresia obtenerMembresiaVigente(Integer idCliente, LocalDate fecha) {

        if (idCliente == null || idCliente <= 0) {
            throw new IllegalArgumentException("El código del cliente no es válido.");
        }

        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }

        return membresiaRepository
                .buscarVigentes(idCliente, EstadoMembresia.ACTIVA, fecha)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean tieneMembresiaVigente(Integer idCliente, LocalDate fecha) {
        return obtenerMembresiaVigente(idCliente, fecha) != null;
    }

    public void cancelarMembresia(Integer idMembresia) {

        Membresia membresia = obtenerMembresiaPorId(idMembresia);

        if (membresia.getEstado() == EstadoMembresia.CANCELADA) {
            throw new IllegalStateException("La membresía ya está cancelada.");
        }
        if (membresia.getEstado() == EstadoMembresia.VENCIDA) {
            throw new IllegalStateException("La membresía ya venció y no se puede cancelar.");
        }

        membresia.setEstado(EstadoMembresia.CANCELADA);
        membresiaRepository.save(membresia);
    }

    public void actualizarEstado(Integer idMembresia, EstadoMembresia nuevoEstado) {

        if (nuevoEstado == null) {
            throw new IllegalArgumentException(
                    "El nuevo estado de la membresía es obligatorio.");
        }

        Membresia membresia = obtenerMembresiaPorId(idMembresia);

        membresia.setEstado(nuevoEstado);

        membresiaRepository.save(membresia);
    }

    private Cliente obtenerClienteValido(Cliente clienteRecibido) {

        if (clienteRecibido == null || clienteRecibido.getIdCliente() == null) {
            throw new IllegalArgumentException("Debe seleccionar un cliente.");
        }

        Cliente cliente = clienteRepository.findById(clienteRecibido.getIdCliente())
                .orElseThrow(() -> new IllegalArgumentException("El cliente no existe."));

        if (!Boolean.TRUE.equals(cliente.getEstado())) {
            throw new IllegalStateException(
                    "El cliente se encuentra inactivo y no puede adquirir una membresía.");
        }

        return cliente;
    }

    private TipoMembresia obtenerTipoMembresiaValido(TipoMembresia tipoRecibido) {

        if (tipoRecibido == null || tipoRecibido.getIdTipo() == null) {
            throw new IllegalArgumentException("Debe seleccionar un tipo de membresía.");
        }

        return tipoMembresiaService.obtenerTipoMembresiaHabilitado(tipoRecibido.getIdTipo());
    }
}
