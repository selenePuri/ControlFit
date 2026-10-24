package com.cibertec.controlfit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cibertec.controlfit.model.Entrenador;
//Repositorios

public interface EntrenadorRepository extends JpaRepository<Entrenador, Integer> {

    List<Entrenador> findByEspecialidad(String especialidad);

    List<Entrenador> findByEmpleado_EstadoTrue();
}
