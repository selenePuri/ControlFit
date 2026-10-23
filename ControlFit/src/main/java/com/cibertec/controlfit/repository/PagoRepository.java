package com.cibertec.controlfit.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cibertec.controlfit.model.Pago;
import com.cibertec.controlfit.model.enums.EstadoPago;

import jakarta.persistence.LockModeType;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    List<Pago> findByMembresia_IdMembresia(Integer idMembresia);

    List<Pago> findByEstado(EstadoPago estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Pago> findWithLockByIdPago(Integer idPago);

    List<Pago> findAllByOrderByFechaPagoDescIdPagoDesc();

    List<Pago> findTop8ByOrderByFechaPagoDescIdPagoDesc();

    List<Pago> findTop10ByMembresia_Cliente_IdClienteOrderByFechaPagoDesc(Integer idCliente);

    @Query("SELECT SUM(p.monto) FROM Pago p "
         + "WHERE p.estado = :estado AND p.fechaPago >= :desde AND p.fechaPago < :hasta")
    BigDecimal sumarMontos(@Param("estado") EstadoPago estado,
                           @Param("desde") LocalDateTime desde,
                           @Param("hasta") LocalDateTime hasta);
}
