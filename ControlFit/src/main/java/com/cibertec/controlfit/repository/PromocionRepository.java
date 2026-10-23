package com.cibertec.controlfit.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cibertec.controlfit.model.Promocion;

public interface PromocionRepository extends JpaRepository<Promocion, Integer> {

    @EntityGraph(attributePaths = "tiposMembresia")
    @Query("SELECT p FROM Promocion p WHERE p.idPromocion = :id")
    Optional<Promocion> buscarConTipos(@Param("id") Integer id);

    @EntityGraph(attributePaths = "tiposMembresia")
    @Query("SELECT p FROM Promocion p ORDER BY p.idPromocion DESC")
    List<Promocion> listarConTipos();

    @EntityGraph(attributePaths = "tiposMembresia")
    @Query("SELECT p FROM Promocion p "
         + "WHERE p.estado = true AND :fecha BETWEEN p.fechaInicio AND p.fechaFin "
         + "ORDER BY p.nombre")
    List<Promocion> vigentes(@Param("fecha") LocalDate fecha);

    @EntityGraph(attributePaths = "tiposMembresia")
    @Query("SELECT DISTINCT p FROM Promocion p JOIN p.tiposMembresia tm "
         + "WHERE tm.idTipo = :idTipo AND p.estado = true "
         + "AND :fecha BETWEEN p.fechaInicio AND p.fechaFin")
    List<Promocion> vigentesPorTipo(@Param("idTipo") Integer idTipo,
                                    @Param("fecha") LocalDate fecha);
}

