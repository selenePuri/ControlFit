package com.cibertec.controlfit.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cibertec.controlfit.dto.PromocionForm;
import com.cibertec.controlfit.model.Promocion;
import com.cibertec.controlfit.model.TipoMembresia;
import com.cibertec.controlfit.model.enums.TipoDescuento;
import com.cibertec.controlfit.service.PromocionService;
import com.cibertec.controlfit.service.TipoMembresiaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
/*Controladores*/

@Controller
@RequestMapping("/admin/promociones")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class AdminPromocionController extends ControllerSupport {

    private final PromocionService promocionService;
    private final TipoMembresiaService tipoMembresiaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("promociones", promocionService.obtenerTodasLasPromociones());
        model.addAttribute("tipos", tipoMembresiaService.obtenerTiposMembresiaActivos());
        model.addAttribute("tiposDescuento", TipoDescuento.values());
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new PromocionForm());
        }
        return "admin/promociones";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") PromocionForm form,
                        BindingResult resultado,
                        RedirectAttributes ra) {

        if (!hayErrores(resultado, ra)) {
            ejecutar(ra, () -> {
                Promocion promocion = aPromocion(form);
                promocionService.registrarPromocion(promocion);
                return "Promoción registrada correctamente.";
            });
        }
        return "redirect:/admin/promociones";
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Integer id, RedirectAttributes ra) {
        ejecutar(ra, () -> {
            promocionService.activarPromocion(id);
            return "Promoción activada correctamente.";
        });
        return "redirect:/admin/promociones";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Integer id, RedirectAttributes ra) {
        ejecutar(ra, () -> {
            promocionService.desactivarPromocion(id);
            return "Promoción desactivada correctamente.";
        });
        return "redirect:/admin/promociones";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Integer id,
                         @Valid @ModelAttribute("form") PromocionForm form,
                         BindingResult resultado,
                         RedirectAttributes ra) {

        if (!hayErrores(resultado, ra)) {
            ejecutar(ra, () -> {
                Promocion promocion = aPromocion(form);
                promocion.setIdPromocion(id);
                promocionService.actualizarPromocion(promocion);
                return "Promoción actualizada correctamente.";
            });
        }

        return "redirect:/admin/promociones";
    }

    private static Promocion aPromocion(PromocionForm form) {
        Promocion promocion = new Promocion();
        promocion.setNombre(form.getNombre());
        promocion.setDescripcion(form.getDescripcion());
        promocion.setTipoDescuento(form.getTipoDescuento());
        promocion.setValorDescuento(form.getValorDescuento());
        promocion.setFechaInicio(form.getFechaInicio());
        promocion.setFechaFin(form.getFechaFin());
        promocion.setEstado(form.getEstado() == null ? Boolean.TRUE : form.getEstado());

        List<TipoMembresia> tipos = new ArrayList<>();
        if (form.getTiposMembresia() != null) {
            for (Integer idTipo : form.getTiposMembresia()) {
                TipoMembresia tipo = new TipoMembresia();
                tipo.setIdTipo(idTipo);
                tipos.add(tipo);
            }
        }
        promocion.setTiposMembresia(tipos);
        return promocion;
    }
}
