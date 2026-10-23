package com.cibertec.controlfit.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cibertec.controlfit.model.RutinaCliente;

public interface RutinaClienteRepository extends JpaRepository<RutinaCliente, Integer> {

    List<RutinaCliente> findByCliente_IdCliente(Integer idCliente);

    List<RutinaCliente> findByRutina_IdRutina(Integer idRutina);

    List<RutinaCliente> findByCliente_IdClienteAndEstadoTrueOrderByFechaAsignacionDesc(Integer idCliente);

    @Query("SELECT rc FROM RutinaCliente rc "
         + "WHERE rc.cliente.idCliente = :idCliente AND rc.rutina.idRutina = :idRutina "
         + "AND rc.estado = true AND rc.fechaAsignacion <= :fecha "
         + "AND (rc.fechaFin IS NULL OR rc.fechaFin >= :fecha)")
    List<RutinaCliente> buscarAsignacionesActivas(@Param("idCliente") Integer idCliente,
                                                  @Param("idRutina") Integer idRutina,
                                                  @Param("fecha") LocalDate fecha);
}
