package com.cibertec.controlfit.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cibertec.controlfit.model.Cliente;
import com.cibertec.controlfit.model.Entrenador;
import com.cibertec.controlfit.model.Rutina;
import com.cibertec.controlfit.model.RutinaCliente;
import com.cibertec.controlfit.repository.ClienteRepository;
import com.cibertec.controlfit.repository.RutinaClienteRepository;
import com.cibertec.controlfit.repository.RutinaRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class RutinaService {

    private final RutinaRepository rutinaRepository;
    private final RutinaClienteRepository rutinaClienteRepository;
    private final ClienteRepository clienteRepository;
    private final EntrenadorService entrenadorService;
    private final Clock clock;

    public void registrarRutina(Rutina rutina) {

        validarDatosRutina(rutina);

        Entrenador entrenador = entrenadorService
                .obtenerEntrenadorHabilitado(rutina.getEntrenador().getIdEntrenador());

        rutina.setEntrenador(entrenador);

        if (rutina.getFechaCreacion() == null) {
            rutina.setFechaCreacion(LocalDate.now(clock));
        }

        if (rutina.getEstado() == null) {
            rutina.setEstado(true);
        }

        rutinaRepository.save(rutina);
    }

    public void actualizarRutina(Rutina rutina) {

        if (rutina == null || rutina.getIdRutina() == null) {
            throw new IllegalArgumentException(
                    "La rutina que desea actualizar no es válida.");
        }

        if (!rutinaRepository.existsById(rutina.getIdRutina())) {
            throw new IllegalArgumentException("La rutina no existe.");
        }

        validarDatosRutina(rutina);

        Entrenador entrenador = entrenadorService
                .obtenerEntrenadorHabilitado(rutina.getEntrenador().getIdEntrenador());

        rutina.setEntrenador(entrenador);

        rutinaRepository.save(rutina);
    }

    @Transactional(readOnly = true)
    public Rutina obtenerRutinaPorId(Integer idRutina) {

        if (idRutina == null || idRutina <= 0) {
            throw new IllegalArgumentException("El código de la rutina no es válido.");
        }

        return rutinaRepository.findById(idRutina)
                .orElseThrow(() -> new IllegalArgumentException("La rutina no existe."));
    }

    @Transactional(readOnly = true)
    public List<Rutina> obtenerTodasLasRutinas() {
        return rutinaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Rutina> obtenerRutinasPorEntrenador(Integer idEntrenador) {

        if (idEntrenador == null || idEntrenador <= 0) {
            throw new IllegalArgumentException("El código del entrenador no es válido.");
        }

        entrenadorService.obtenerEntrenadorPorId(idEntrenador);

        return rutinaRepository.findByEntrenador_IdEntrenador(idEntrenador);
    }

    @Transactional(readOnly = true)
    public List<Rutina> obtenerRutinasActivas() {
        return rutinaRepository.findByEstadoTrue();
    }

    public void desactivarRutina(Integer idRutina) {

        Rutina rutina = obtenerRutinaPorId(idRutina);
        rutina.setEstado(false);
        rutinaRepository.save(rutina);
    }

    public void activarRutina(Integer idRutina) {

        Rutina rutina = obtenerRutinaPorId(idRutina);

        entrenadorService.obtenerEntrenadorHabilitado(
                rutina.getEntrenador().getIdEntrenador());

        rutina.setEstado(true);
        rutinaRepository.save(rutina);
    }

    public void asignarRutina(Integer idRutina,
                              Integer idCliente,
                              LocalDate fechaAsignacion,
                              LocalDate fechaFin) {

        Cliente cliente = obtenerClienteActivo(idCliente);
        Rutina rutina = obtenerRutinaActiva(idRutina);

        if (fechaAsignacion == null) {
            fechaAsignacion = LocalDate.now(clock);
        }

        if (fechaFin != null && fechaFin.isBefore(fechaAsignacion)) {
            throw new IllegalArgumentException(
                    "La fecha de fin no puede ser anterior a la fecha de asignación.");
        }

        boolean yaAsignada = !rutinaClienteRepository.buscarAsignacionesActivas(
                cliente.getIdCliente(), rutina.getIdRutina(), fechaAsignacion).isEmpty();

        if (yaAsignada) {
            throw new IllegalStateException(
                    "El cliente ya tiene esta rutina asignada actualmente.");
        }

        RutinaCliente asignacion = new RutinaCliente();
        asignacion.setRutina(rutina);
        asignacion.setCliente(cliente);
        asignacion.setFechaAsignacion(fechaAsignacion);
        asignacion.setFechaFin(fechaFin);
        asignacion.setEstado(true);

        rutinaClienteRepository.save(asignacion);
    }

    @Transactional(readOnly = true)
    public RutinaCliente obtenerAsignacionPorId(Integer idRutinaCliente) {

        if (idRutinaCliente == null || idRutinaCliente <= 0) {
            throw new IllegalArgumentException("El código de la asignación no es válido.");
        }

        return rutinaClienteRepository.findById(idRutinaCliente)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La asignación de rutina no existe."));
    }

    @Transactional(readOnly = true)
    public List<RutinaCliente> obtenerTodasLasAsignaciones() {
        return rutinaClienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<RutinaCliente> obtenerRutinasPorCliente(Integer idCliente) {

        Cliente cliente = obtenerClienteExistente(idCliente);

        return rutinaClienteRepository.findByCliente_IdCliente(cliente.getIdCliente());
    }

    /** Rutinas vigentes del cliente, para mostrarlas en su portal. */
    @Transactional(readOnly = true)
    public List<RutinaCliente> obtenerRutinasActivasDeCliente(Integer idCliente) {
        return rutinaClienteRepository
                .findByCliente_IdClienteAndEstadoTrueOrderByFechaAsignacionDesc(idCliente);
    }

    @Transactional(readOnly = true)
    public List<RutinaCliente> obtenerClientesPorRutina(Integer idRutina) {

        Rutina rutina = obtenerRutinaPorId(idRutina);

        return rutinaClienteRepository.findByRutina_IdRutina(rutina.getIdRutina());
    }

    public void finalizarAsignacion(Integer idRutinaCliente, LocalDate fechaFin) {

        RutinaCliente asignacion = obtenerAsignacionPorId(idRutinaCliente);

        if (!Boolean.TRUE.equals(asignacion.getEstado())) {
            throw new IllegalStateException("La asignación ya se encuentra inactiva.");
        }

        if (fechaFin == null) {
            fechaFin = LocalDate.now(clock);
        }

        if (fechaFin.isBefore(asignacion.getFechaAsignacion())) {
            throw new IllegalArgumentException(
                    "La fecha de fin no puede ser anterior a la fecha de asignación.");
        }

        asignacion.setFechaFin(fechaFin);
        asignacion.setEstado(false);

        rutinaClienteRepository.save(asignacion);
    }

    private Rutina obtenerRutinaActiva(Integer idRutina) {

        Rutina rutina = obtenerRutinaPorId(idRutina);

        if (!Boolean.TRUE.equals(rutina.getEstado())) {
            throw new IllegalStateException(
                    "La rutina se encuentra inactiva y no puede ser asignada.");
        }

        entrenadorService.obtenerEntrenadorHabilitado(
                rutina.getEntrenador().getIdEntrenador());

        return rutina;
    }

    private Cliente obtenerClienteActivo(Integer idCliente) {

        Cliente cliente = obtenerClienteExistente(idCliente);

        if (!Boolean.TRUE.equals(cliente.getEstado())) {
            throw new IllegalStateException(
                    "El cliente se encuentra inactivo y no puede recibir nuevas rutinas.");
        }

        return cliente;
    }

    private Cliente obtenerClienteExistente(Integer idCliente) {

        if (idCliente == null || idCliente <= 0) {
            throw new IllegalArgumentException("El código del cliente no es válido.");
        }

        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("El cliente no existe."));
    }

    private void validarDatosRutina(Rutina rutina) {

        if (rutina == null) {
            throw new IllegalArgumentException("La rutina es obligatoria.");
        }

        if (rutina.getEntrenador() == null
                || rutina.getEntrenador().getIdEntrenador() == null) {

            throw new IllegalArgumentException(
                    "La rutina debe estar asociada a un entrenador.");
        }

        if (rutina.getNombre() == null || rutina.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la rutina es obligatorio.");
        }
    }
}
