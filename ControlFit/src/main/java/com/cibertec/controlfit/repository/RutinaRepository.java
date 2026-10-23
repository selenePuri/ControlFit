package com.cibertec.controlfit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cibertec.controlfit.model.Rutina;

public interface RutinaRepository extends JpaRepository<Rutina, Integer> {

    List<Rutina> findByEntrenador_IdEntrenador(Integer idEntrenador);

    List<Rutina> findByEstadoTrue();
}
