package com.cibertec.controlfit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cibertec.controlfit.model.TipoMembresia;
import com.cibertec.controlfit.model.enums.TipoPlan;

public interface TipoMembresiaRepository extends JpaRepository<TipoMembresia, Integer> {

    Optional<TipoMembresia> findByNombre(TipoPlan nombre);

    List<TipoMembresia> findByEstadoTrue();

    List<TipoMembresia> findByEstadoTrueOrderByDuracionDiasAsc();
}
