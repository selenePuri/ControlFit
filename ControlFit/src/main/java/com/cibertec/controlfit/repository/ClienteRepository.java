package com.cibertec.controlfit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cibertec.controlfit.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Optional<Cliente> findByDni(String dni);

    boolean existsByDni(String dni);

    Optional<Cliente> findByUsuario_IdUsuario(Integer idUsuario);

    long countByEstadoTrue();

    List<Cliente> findByEstadoTrueOrderByApellidosAscNombresAsc();
}
