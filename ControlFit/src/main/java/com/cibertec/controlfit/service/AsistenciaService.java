package com.cibertec.controlfit.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cibertec.controlfit.model.Asistencia;
import com.cibertec.controlfit.model.Cliente;
import com.cibertec.controlfit.repository.AsistenciaRepository;
import com.cibertec.controlfit.repository.ClienteRepository;

import lombok.RequiredArgsConstructor;
/*Logica de negocio*/
@Service
@Transactional
@RequiredArgsConstructor
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final ClienteRepository clienteRepository;
    private final MembresiaService membresiaService;
    private final Clock clock;

    public void registrarEntrada(Integer idCliente) {

        Cliente cliente = obtenerClienteActivo(idCliente);

        LocalDate fechaActual = LocalDate.now(clock);

        LocalTime horaActual = LocalTime.now(clock).withNano(0);

        validarMembresiaVigente(cliente, fechaActual);
        validarSinAsistenciaAbierta(cliente.getIdCliente(), fechaActual);

        Asistencia asistencia = new Asistencia();
        asistencia.setCliente(cliente);
        asistencia.setFecha(fechaActual);
        asistencia.setHoraEntrada(horaActual);
        asistencia.setHoraSalida(null);

        asistenciaRepository.save(asistencia);
    }

    public void registrarEntradaPorDni(String dni) {

        if (dni == null || !dni.matches("[0-9]{8}")) {
            throw new IllegalArgumentException("Ingrese un DNI válido de 8 dígitos.");
        }

        Cliente cliente = clienteRepository.findByDni(dni)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un cliente con DNI " + dni + "."));

        registrarEntrada(cliente.getIdCliente());
    }

    public void registrarSalida(Integer idCliente) {

        Cliente cliente = obtenerClienteExistente(idCliente);

        Asistencia asistencia = asistenciaRepository
                .findFirstByCliente_IdClienteAndHoraSalidaIsNullOrderByFechaDescHoraEntradaDesc(
                        cliente.getIdCliente())
                .orElseThrow(() -> new IllegalStateException(
                        "El cliente no tiene una entrada pendiente de salida."));

        cerrarAsistencia(asistencia);
    }

    public void registrarSalidaPorAsistencia(Integer idAsistencia) {

        Asistencia asistencia = obtenerAsistenciaPorId(idAsistencia);

        if (asistencia.getHoraSalida() != null) {
            throw new IllegalStateException(
                    "La salida de esta asistencia ya fue registrada.");
        }

        cerrarAsistencia(asistencia);
    }

    @Transactional(readOnly = true)
    public Asistencia obtenerAsistenciaPorId(Integer idAsistencia) {

        validarIdAsistencia(idAsistencia);

        return asistenciaRepository.findById(idAsistencia)
                .orElseThrow(() -> new IllegalArgumentException("La asistencia no existe."));
    }

    @Transactional(readOnly = true)
    public List<Asistencia> obtenerTodasLasAsistencias() {
        return asistenciaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Asistencia> obtenerAsistenciasPorCliente(Integer idCliente) {

        Cliente cliente = obtenerClienteExistente(idCliente);

        return asistenciaRepository.findByCliente_IdCliente(cliente.getIdCliente());
    }

    @Transactional(readOnly = true)
    public List<Asistencia> obtenerAsistenciasPorFecha(LocalDate fecha) {

        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }

        return asistenciaRepository.findByFechaOrderByHoraEntradaDesc(fecha);
    }

    @Transactional(readOnly = true)
    public List<Asistencia> obtenerAsistenciasDeHoy() {
        return asistenciaRepository.findByFechaOrderByHoraEntradaDesc(LocalDate.now(clock));
    }

    @Transactional(readOnly = true)
    public long contarAsistenciasDeCliente(Integer idCliente) {
        return asistenciaRepository.countByCliente_IdCliente(idCliente);
    }

    @Transactional(readOnly = true)
    public long contarAsistenciasDelMes(Integer idCliente, LocalDate hoy) {
        return asistenciaRepository.countByCliente_IdClienteAndFechaBetween(
                idCliente, hoy.withDayOfMonth(1), hoy.withDayOfMonth(hoy.lengthOfMonth()));
    }

    @Transactional(readOnly = true)
    public List<Asistencia> obtenerUltimasAsistenciasDeCliente(Integer idCliente) {
        return asistenciaRepository
                .findTop10ByCliente_IdClienteOrderByFechaDescHoraEntradaDesc(idCliente);
    }

    @Transactional(readOnly = true)
    public Asistencia obtenerAsistenciaAbierta(Integer idCliente) {

        Cliente cliente = obtenerClienteExistente(idCliente);

        return asistenciaRepository
                .findFirstByCliente_IdClienteAndHoraSalidaIsNullOrderByFechaDescHoraEntradaDesc(
                        cliente.getIdCliente())
                .orElse(null);
    }

    private void cerrarAsistencia(Asistencia asistencia) {

        LocalDate fechaActual = LocalDate.now(clock);
        LocalTime horaActual = LocalTime.now(clock).withNano(0);

        if (!fechaActual.equals(asistencia.getFecha())) {
            throw new IllegalStateException(
                    "La salida no puede registrarse en una fecha diferente a la fecha de entrada.");
        }

        if (horaActual.isBefore(asistencia.getHoraEntrada())) {
            throw new IllegalStateException(
                    "La hora de salida no puede ser anterior a la hora de entrada.");
        }

        asistencia.setHoraSalida(horaActual);

        asistenciaRepository.save(asistencia);
    }

    private Cliente obtenerClienteActivo(Integer idCliente) {

        Cliente cliente = obtenerClienteExistente(idCliente);

        if (!Boolean.TRUE.equals(cliente.getEstado())) {
            throw new IllegalStateException(
                    "El cliente se encuentra inactivo y no puede registrar asistencia.");
        }

        return cliente;
    }

    private Cliente obtenerClienteExistente(Integer idCliente) {

        validarIdCliente(idCliente);

        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("El cliente no existe."));
    }

    private void validarMembresiaVigente(Cliente cliente, LocalDate fecha) {

        if (!membresiaService.tieneMembresiaVigente(cliente.getIdCliente(), fecha)) {
            throw new IllegalStateException(
                    "El cliente no tiene una membresía vigente y no puede ingresar.");
        }
    }

    private void validarSinAsistenciaAbierta(Integer idCliente, LocalDate hoy) {

        if (asistenciaRepository
                .findFirstByCliente_IdClienteAndFechaAndHoraSalidaIsNull(idCliente, hoy)
                .isPresent()) {

            throw new IllegalStateException(
                    "El cliente ya tiene una entrada registrada y aún no ha registrado su salida.");
        }
    }

    private void validarIdCliente(Integer idCliente) {

        if (idCliente == null || idCliente <= 0) {
            throw new IllegalArgumentException("El código del cliente no es válido.");
        }
    }

    private void validarIdAsistencia(Integer idAsistencia) {

        if (idAsistencia == null || idAsistencia <= 0) {
            throw new IllegalArgumentException("El código de la asistencia no es válido.");
        }
    }
}
