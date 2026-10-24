package com.cibertec.controlfit.controller;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cibertec.controlfit.model.Membresia;
/*Controladores*/

public abstract class ControllerSupport {

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected void ok(RedirectAttributes ra, String mensaje) {
        ra.addFlashAttribute("flashTipo", "ok");
        ra.addFlashAttribute("flashMsg", mensaje);
    }

    protected void error(RedirectAttributes ra, String mensaje) {
        ra.addFlashAttribute("flashTipo", "err");
        ra.addFlashAttribute("flashMsg", mensaje);
    }

    protected void ejecutar(RedirectAttributes ra, Supplier<String> accion) {
        try {
            ok(ra, accion.get());
        } catch (IllegalArgumentException | IllegalStateException e) {
            error(ra, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.warn("Violación de integridad: {}", e.getMostSpecificCause().getMessage());
            error(ra, "Ya existe un registro con ese DNI, correo o nombre de usuario.");
        } catch (RuntimeException e) {
            log.error("Error inesperado", e);
            error(ra, "No se pudo completar la operación. Intenta nuevamente.");
        }
    }

    protected boolean hayErrores(BindingResult resultado, RedirectAttributes ra) {
        if (resultado.hasErrors()) {
            error(ra, resultado.getAllErrors().get(0).getDefaultMessage());
            return true;
        }
        return false;
    }

    protected String mensajeVenta(Membresia m) {
        return "Membresía registrada para "
                + m.getCliente().getNombres() + " " + m.getCliente().getApellidos()
                + " (" + m.getFechaInicio() + " al " + m.getFechaFin() + "). "
                + "Pago de S/ " + m.getPrecioFinal().setScale(2, java.math.RoundingMode.HALF_UP)
                + " guardado.";
    }
}
