package com.cibertec.controlfit.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cibertec.controlfit.model.Membresia;
import com.cibertec.controlfit.model.enums.EstadoMembresia;
//Repositorios

public interface MembresiaRepository extends JpaRepository<Membresia, Integer> {

    List<Membresia> findByCliente_IdCliente(Integer idCliente);

    List<Membresia> findByEstado(EstadoMembresia estado);

    List<Membresia> findAllByOrderByIdMembresiaDesc();

    @Query("SELECT m FROM Membresia m "
         + "WHERE m.cliente.idCliente = :idCliente AND m.estado = :estado "
         + "AND :fecha BETWEEN m.fechaInicio AND m.fechaFin "
         + "ORDER BY m.idMembresia DESC")
    List<Membresia> buscarVigentes(@Param("idCliente") Integer idCliente,
                                   @Param("estado") EstadoMembresia estado,
                                   @Param("fecha") LocalDate fecha);

    @Query("SELECT COUNT(m) FROM Membresia m "
         + "WHERE m.cliente.idCliente = :idCliente AND m.estado = :estado "
         + "AND m.fechaInicio <= :fin AND m.fechaFin >= :inicio")
    long contarSolapadas(@Param("idCliente") Integer idCliente,
                         @Param("estado") EstadoMembresia estado,
                         @Param("inicio") LocalDate inicio,
                         @Param("fin") LocalDate fin);

    @Query("SELECT COUNT(m) FROM Membresia m "
         + "WHERE m.estado = :estado AND :fecha BETWEEN m.fechaInicio AND m.fechaFin")
    long contarVigentes(@Param("estado") EstadoMembresia estado,
                        @Param("fecha") LocalDate fecha);
}

