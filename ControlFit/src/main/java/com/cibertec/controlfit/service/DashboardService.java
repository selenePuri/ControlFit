package com.cibertec.controlfit.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import com.cibertec.controlfit.model.enums.EstadoMembresia;
import com.cibertec.controlfit.model.enums.EstadoPago;
import com.cibertec.controlfit.repository.AsistenciaRepository;
import com.cibertec.controlfit.repository.ClienteRepository;
import com.cibertec.controlfit.repository.EmpleadoRepository;
import com.cibertec.controlfit.repository.MembresiaRepository;
import com.cibertec.controlfit.repository.PagoRepository;

import lombok.RequiredArgsConstructor;
/*Logica de negocio*/

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {

    public record ResumenAdmin(long clientesActivos,
                               long membresiasVigentes,
                               BigDecimal ingresosDelMes,
                               long empleadosActivos) { }

    public record ResumenRecepcion(long clientesActivos,
                                   long asistenciasHoy,
                                   long membresiasVigentes,
                                   BigDecimal cobrosDeHoy) { }

    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;
    private final MembresiaRepository membresiaRepository;
    private final PagoRepository pagoRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final Clock clock;

    public ResumenAdmin resumenAdmin() {

        LocalDate hoy = LocalDate.now(clock);
        LocalDateTime inicioMes = hoy.withDayOfMonth(1).atStartOfDay();
        LocalDateTime inicioMesSiguiente = inicioMes.plusMonths(1);

        return new ResumenAdmin(
                clienteRepository.countByEstadoTrue(),
                membresiaRepository.contarVigentes(EstadoMembresia.ACTIVA, hoy),
                sumaOCero(inicioMes, inicioMesSiguiente),
                empleadoRepository.countByEstadoTrue());
    }

    public ResumenRecepcion resumenRecepcion() {

        LocalDate hoy = LocalDate.now(clock);

        return new ResumenRecepcion(
                clienteRepository.countByEstadoTrue(),
                asistenciaRepository.countByFecha(hoy),
                membresiaRepository.contarVigentes(EstadoMembresia.ACTIVA, hoy),
                sumaOCero(hoy.atStartOfDay(), hoy.plusDays(1).atStartOfDay()));
    }

    private BigDecimal sumaOCero(LocalDateTime desde, LocalDateTime hasta) {

        BigDecimal suma = pagoRepository.sumarMontos(EstadoPago.PAGADO, desde, hasta);

        return suma != null ? suma : BigDecimal.ZERO;
    }
}
