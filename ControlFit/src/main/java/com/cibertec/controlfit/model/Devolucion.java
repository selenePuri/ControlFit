package com.cibertec.controlfit.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.cibertec.controlfit.model.enums.EstadoDevolucion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "devolucion")
@Getter
@Setter
@NoArgsConstructor
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_devolucion")
    private Integer idDevolucion;

    @NotNull(message = "El pago es obligatorio")
    @ManyToOne
    @JoinColumn(name = "id_pago", nullable = false)
    private Pago pago;

    @NotNull(message = "La fecha de la devolución es obligatoria")
    @Column(name = "fecha_devolucion", nullable = false)
    private LocalDateTime fechaDevolucion;

    @NotNull(message = "El importe es obligatorio")
    @Positive(message = "El importe debe ser mayor a cero")
    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Size(max = 200, message = "El motivo no puede superar 200 caracteres")
    @Column(name = "motivo", length = 200)
    private String motivo;

    @NotNull(message = "El estado de la devolución es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoDevolucion estado = EstadoDevolucion.COMPLETADA;
}
