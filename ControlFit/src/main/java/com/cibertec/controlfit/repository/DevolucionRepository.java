package com.cibertec.controlfit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.cibertec.controlfit.model.Devolucion;

import jakarta.persistence.LockModeType;

public interface DevolucionRepository extends JpaRepository<Devolucion, Integer> {

    List<Devolucion> findByPago_IdPago(Integer idPago);

    List<Devolucion> findAllByOrderByFechaDevolucionDescIdDevolucionDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Devolucion> findWithLockByIdDevolucion(Integer idDevolucion);
}
