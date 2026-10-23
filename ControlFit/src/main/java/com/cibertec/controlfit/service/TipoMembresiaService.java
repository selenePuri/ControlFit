package com.cibertec.controlfit.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cibertec.controlfit.model.TipoMembresia;
import com.cibertec.controlfit.model.enums.TipoPlan;
import com.cibertec.controlfit.repository.TipoMembresiaRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TipoMembresiaService {

    private final TipoMembresiaRepository tipoMembresiaRepository;

    public void registrarTipoMembresia(TipoMembresia tipoMembresia) {

        validarTipoMembresia(tipoMembresia);

        if (tipoMembresiaRepository.findByNombre(tipoMembresia.getNombre()).isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe un tipo de membresía para ese plan.");
        }

        tipoMembresia.setDuracionDias(obtenerDuracionPlan(tipoMembresia.getNombre()));

        if (tipoMembresia.getEstado() == null) {
            tipoMembresia.setEstado(true);
        }

        tipoMembresiaRepository.save(tipoMembresia);
    }

    public void actualizarTipoMembresia(TipoMembresia tipoMembresia) {

        if (tipoMembresia == null || tipoMembresia.getIdTipo() == null) {
            throw new IllegalArgumentException(
                    "El tipo de membresía que desea actualizar no es válido.");
        }

        if (!tipoMembresiaRepository.existsById(tipoMembresia.getIdTipo())) {
            throw new IllegalArgumentException("El tipo de membresía no existe.");
        }

        validarTipoMembresia(tipoMembresia);

        tipoMembresiaRepository.findByNombre(tipoMembresia.getNombre())
                .filter(mismoPlan -> !mismoPlan.getIdTipo().equals(tipoMembresia.getIdTipo()))
                .ifPresent(mismoPlan -> {
                    throw new IllegalArgumentException(
                            "Ya existe un tipo de membresía para ese plan.");
                });

        tipoMembresia.setDuracionDias(obtenerDuracionPlan(tipoMembresia.getNombre()));

        tipoMembresiaRepository.save(tipoMembresia);
    }

    @Transactional(readOnly = true)
    public TipoMembresia obtenerTipoMembresiaPorId(Integer idTipoMembresia) {

        if (idTipoMembresia == null || idTipoMembresia <= 0) {
            throw new IllegalArgumentException(
                    "El código del tipo de membresía no es válido.");
        }

        return tipoMembresiaRepository.findById(idTipoMembresia)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El tipo de membresía no existe."));
    }

    @Transactional(readOnly = true)
    public List<TipoMembresia> obtenerTodosLosTiposMembresia() {
        return tipoMembresiaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TipoMembresia> obtenerTiposMembresiaActivos() {
        return tipoMembresiaRepository.findByEstadoTrueOrderByDuracionDiasAsc();
    }

    @Transactional(readOnly = true)
    public TipoMembresia obtenerTipoMembresiaHabilitado(Integer idTipoMembresia) {

        TipoMembresia tipo = obtenerTipoMembresiaPorId(idTipoMembresia);

        if (!Boolean.TRUE.equals(tipo.getEstado())) {
            throw new IllegalStateException("El tipo de membresía se encuentra inactivo.");
        }

        return tipo;
    }

    public void desactivarTipoMembresia(Integer idTipoMembresia) {

        TipoMembresia tipo = obtenerTipoMembresiaPorId(idTipoMembresia);
        tipo.setEstado(false);
        tipoMembresiaRepository.save(tipo);
    }

    public void activarTipoMembresia(Integer idTipoMembresia) {

        TipoMembresia tipo = obtenerTipoMembresiaPorId(idTipoMembresia);
        tipo.setEstado(true);
        tipoMembresiaRepository.save(tipo);
    }

    private void validarTipoMembresia(TipoMembresia tipoMembresia) {

        if (tipoMembresia == null) {
            throw new IllegalArgumentException("El tipo de membresía es obligatorio.");
        }

        if (tipoMembresia.getNombre() == null) {
            throw new IllegalArgumentException("El plan de membresía es obligatorio.");
        }

        if (tipoMembresia.getPrecio() == null) {
            throw new IllegalArgumentException("El precio de la membresía es obligatorio.");
        }

        if (tipoMembresia.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El precio de la membresía debe ser mayor a 0.");
        }
    }

    private Integer obtenerDuracionPlan(TipoPlan plan) {

        switch (plan) {
            case PASE_DIARIO:
                return 1;
            case MENSUAL:
                return 30;
            case TRIMESTRAL:
                return 90;
            case SEMESTRAL:
                return 180;
            case ANUAL:
                return 365;
            default:
                throw new IllegalArgumentException("El plan de membresía no es válido.");
        }
    }
}
