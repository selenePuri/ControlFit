package com.cibertec.controlfit.controller;

import java.time.Clock;
import java.time.LocalDate;

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

import com.cibertec.controlfit.dto.MembresiaForm;
import com.cibertec.controlfit.model.enums.MetodoPago;
import com.cibertec.controlfit.service.ClienteService;
import com.cibertec.controlfit.service.MembresiaService;
import com.cibertec.controlfit.service.PromocionService;
import com.cibertec.controlfit.service.TipoMembresiaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
/*Controladores*/

@Controller
@RequestMapping("/admin/membresias")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class AdminMembresiaController extends ControllerSupport {

    private final MembresiaService membresiaService;
    private final ClienteService clienteService;
    private final TipoMembresiaService tipoMembresiaService;
    private final PromocionService promocionService;
    private final Clock clock;

    @GetMapping
    public String listar(Model model) {
        LocalDate hoy = LocalDate.now(clock);
        model.addAttribute("membresias", membresiaService.obtenerTodasLasMembresias());
        model.addAttribute("clientes", clienteService.obtenerClientesActivos());
        model.addAttribute("tipos", tipoMembresiaService.obtenerTiposMembresiaActivos());
        model.addAttribute("promos", promocionService.obtenerPromocionesVigentes(hoy));
        model.addAttribute("metodos", MetodoPago.values());
        model.addAttribute("hoy", hoy);
        if (!model.containsAttribute("form")) {
            MembresiaForm form = new MembresiaForm();
            form.setFechaInicio(hoy);
            model.addAttribute("form", form);
        }
        return "admin/membresias";
    }

    @PostMapping
    public String vender(@Valid @ModelAttribute("form") MembresiaForm form,
                         BindingResult resultado,
                         RedirectAttributes ra) {

        if (!hayErrores(resultado, ra)) {
            ejecutar(ra, () -> mensajeVenta(membresiaService.venderMembresia(
                    form.getIdCliente(), form.getIdTipo(), form.getIdPromocion(),
                    form.getFechaInicio(), form.getMetodoPago())));
        }
        return "redirect:/admin/membresias";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Integer id, RedirectAttributes ra) {
        ejecutar(ra, () -> {
            membresiaService.cancelarMembresia(id);
            return "Membresía cancelada. Sus pagos no se modificaron; si corresponde devolver dinero, "
                    + "regístralo en Devoluciones.";
        });
        return "redirect:/admin/membresias";
    }
}
