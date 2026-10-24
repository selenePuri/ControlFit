package com.cibertec.controlfit.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cibertec.controlfit.model.Asistencia;
//Repositorios
public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer> {

    List<Asistencia> findByCliente_IdCliente(Integer idCliente);

    List<Asistencia> findByFecha(LocalDate fecha);

    List<Asistencia> findByFechaOrderByHoraEntradaDesc(LocalDate fecha);

    long countByFecha(LocalDate fecha);

    long countByCliente_IdCliente(Integer idCliente);

    long countByCliente_IdClienteAndFechaBetween(Integer idCliente, LocalDate desde, LocalDate hasta);

    Optional<Asistencia> findFirstByCliente_IdClienteAndHoraSalidaIsNullOrderByFechaDescHoraEntradaDesc(
            Integer idCliente);

    Optional<Asistencia> findFirstByCliente_IdClienteAndFechaAndHoraSalidaIsNull(
            Integer idCliente, LocalDate fecha);

    List<Asistencia> findTop10ByCliente_IdClienteOrderByFechaDescHoraEntradaDesc(Integer idCliente);
}
