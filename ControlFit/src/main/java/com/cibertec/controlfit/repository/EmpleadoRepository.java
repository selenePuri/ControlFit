package com.cibertec.controlfit.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cibertec.controlfit.model.Empleado;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {

    Optional<Empleado> findByDni(String dni);

    Optional<Empleado> findByUsuario_IdUsuario(Integer idUsuario);

    long countByEstadoTrue();
}
