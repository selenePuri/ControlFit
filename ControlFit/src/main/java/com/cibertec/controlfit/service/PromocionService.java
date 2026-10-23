package com.cibertec.controlfit.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cibertec.controlfit.model.Promocion;
import com.cibertec.controlfit.model.TipoMembresia;
import com.cibertec.controlfit.model.enums.TipoDescuento;
import com.cibertec.controlfit.repository.PromocionRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PromocionService {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private final PromocionRepository promocionRepository;
    private final TipoMembresiaService tipoMembresiaService;

    public void registrarPromocion(Promocion promocion) {

        validarDatosPromocion(promocion);

        promocion.setTiposMembresia(
                obtenerTiposMembresiaValidos(promocion.getTiposMembresia()));

        if (promocion.getEstado() == null) {
            promocion.setEstado(true);
        }

        promocionRepository.save(promocion);
    }

    public void actualizarPromocion(Promocion promocion) {

        if (promocion == null || promocion.getIdPromocion() == null) {
            throw new IllegalArgumentException(
                    "La promoción que desea actualizar no es válida.");
        }

        if (!promocionRepository.existsById(promocion.getIdPromocion())) {
            throw new IllegalArgumentException("La promoción no existe.");
        }

        validarDatosPromocion(promocion);

        promocion.setTiposMembresia(
                obtenerTiposMembresiaValidos(promocion.getTiposMembresia()));

        promocionRepository.save(promocion);
    }

    @Transactional(readOnly = true)
    public Promocion obtenerPromocionPorId(Integer idPromocion) {

        if (idPromocion == null || idPromocion <= 0) {
            throw new IllegalArgumentException("El código de la promoción no es válido.");
        }

        return promocionRepository.buscarConTipos(idPromocion)
                .orElseThrow(() -> new IllegalArgumentException("La promoción no existe."));
    }

    @Transactional(readOnly = true)
    public List<Promocion> obtenerTodasLasPromociones() {
        return promocionRepository.listarConTipos();
    }

    @Transactional(readOnly = true)
    public List<Promocion> obtenerPromocionesVigentes(LocalDate fecha) {

        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }

        return promocionRepository.vigentes(fecha);
    }

    @Transactional(readOnly = true)
    public List<Promocion> obtenerPromocionesVigentesPorTipoMembresia(
            Integer idTipo, LocalDate fecha) {

        if (idTipo == null || idTipo <= 0) {
            throw new IllegalArgumentException("El tipo de membresía no es válido.");
        }

        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }

        tipoMembresiaService.obtenerTipoMembresiaPorId(idTipo);

        return promocionRepository.vigentesPorTipo(idTipo, fecha);
    }

    @Transactional(readOnly = true)
    public Promocion obtenerPromocionValida(Integer idPromocion,
                                            TipoMembresia tipoMembresia,
                                            LocalDate fecha) {

        Promocion promocion = obtenerPromocionPorId(idPromocion);

        if (!Boolean.TRUE.equals(promocion.getEstado())) {
            throw new IllegalStateException("La promoción se encuentra inactiva.");
        }

        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }

        if (fecha.isBefore(promocion.getFechaInicio())
                || fecha.isAfter(promocion.getFechaFin())) {

            throw new IllegalStateException("La promoción no se encuentra vigente.");
        }

        if (tipoMembresia == null || tipoMembresia.getIdTipo() == null) {
            throw new IllegalArgumentException("El tipo de membresía no es válido.");
        }

        TipoMembresia tipoExistente =
                tipoMembresiaService.obtenerTipoMembresiaPorId(tipoMembresia.getIdTipo());

        boolean compatible = promocion.getTiposMembresia().stream()
                .anyMatch(tipo -> tipo.getIdTipo().equals(tipoExistente.getIdTipo()));

        if (!compatible) {
            throw new IllegalArgumentException(
                    "La promoción no aplica al tipo de membresía seleccionado.");
        }

        return promocion;
    }

    public BigDecimal calcularDescuento(Promocion promocion, BigDecimal precioBase) {

        if (promocion == null) {
            return BigDecimal.ZERO;
        }

        if (precioBase == null || precioBase.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio base debe ser mayor a 0.");
        }

        if (promocion.getTipoDescuento() == null) {
            throw new IllegalArgumentException("El tipo de descuento es obligatorio.");
        }

        if (promocion.getValorDescuento() == null
                || promocion.getValorDescuento().compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "El valor del descuento debe ser mayor a 0.");
        }

        BigDecimal descuento;

        if (promocion.getTipoDescuento() == TipoDescuento.PORCENTAJE) {

            if (promocion.getValorDescuento().compareTo(CIEN) > 0) {
                throw new IllegalArgumentException(
                        "El porcentaje de descuento no puede ser mayor a 100.");
            }

            descuento = precioBase
                    .multiply(promocion.getValorDescuento())
                    .divide(CIEN, 2, RoundingMode.HALF_UP);

        } else if (promocion.getTipoDescuento() == TipoDescuento.MONTO_FIJO) {

            descuento = promocion.getValorDescuento();

        } else {
            throw new IllegalArgumentException("El tipo de descuento no es válido.");
        }

        if (descuento.compareTo(precioBase) > 0) {
            throw new IllegalArgumentException(
                    "El descuento no puede ser mayor al precio de la membresía.");
        }

        return descuento;
    }

    public void desactivarPromocion(Integer idPromocion) {

        Promocion promocion = obtenerPromocionPorId(idPromocion);
        promocion.setEstado(false);
        promocionRepository.save(promocion);
    }

    public void activarPromocion(Integer idPromocion) {

        Promocion promocion = obtenerPromocionPorId(idPromocion);
        promocion.setEstado(true);
        promocionRepository.save(promocion);
    }

    private void validarDatosPromocion(Promocion promocion) {

        if (promocion == null) {
            throw new IllegalArgumentException("La promoción es obligatoria.");
        }

        if (promocion.getNombre() == null || promocion.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la promoción es obligatorio.");
        }

        if (promocion.getTipoDescuento() == null) {
            throw new IllegalArgumentException("El tipo de descuento es obligatorio.");
        }

        if (promocion.getValorDescuento() == null
                || promocion.getValorDescuento().compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "El valor del descuento debe ser mayor a 0.");
        }

        if (promocion.getFechaInicio() == null || promocion.getFechaFin() == null) {
            throw new IllegalArgumentException(
                    "Las fechas de la promoción son obligatorias.");
        }

        if (promocion.getFechaFin().isBefore(promocion.getFechaInicio())) {
            throw new IllegalArgumentException(
                    "La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        if (promocion.getTipoDescuento() == TipoDescuento.PORCENTAJE
                && promocion.getValorDescuento().compareTo(CIEN) > 0) {

            throw new IllegalArgumentException(
                    "El porcentaje de descuento no puede ser mayor a 100.");
        }

        if (promocion.getTiposMembresia() == null
                || promocion.getTiposMembresia().isEmpty()) {

            throw new IllegalArgumentException(
                    "La promoción debe estar asociada al menos a un tipo de membresía.");
        }
    }

    private List<TipoMembresia> obtenerTiposMembresiaValidos(List<TipoMembresia> tipos) {

        List<TipoMembresia> tiposValidos = new ArrayList<>();

        for (TipoMembresia tipo : tipos) {

            if (tipo == null || tipo.getIdTipo() == null) {
                throw new IllegalArgumentException(
                        "La promoción contiene un tipo de membresía no válido.");
            }

            TipoMembresia existente =
                    tipoMembresiaService.obtenerTipoMembresiaHabilitado(tipo.getIdTipo());

            boolean repetido = tiposValidos.stream()
                    .anyMatch(t -> t.getIdTipo().equals(existente.getIdTipo()));

            if (!repetido) {
                tiposValidos.add(existente);
            }
        }

        return tiposValidos;
    }
}
