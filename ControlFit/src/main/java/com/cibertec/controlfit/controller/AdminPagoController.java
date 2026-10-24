package com.cibertec.controlfit.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cibertec.controlfit.dto.PagoForm;
import com.cibertec.controlfit.model.Membresia;
import com.cibertec.controlfit.model.Pago;
import com.cibertec.controlfit.model.enums.EstadoPago;
import com.cibertec.controlfit.model.enums.MetodoPago;
import com.cibertec.controlfit.service.DevolucionService;
import com.cibertec.controlfit.service.MembresiaService;
import com.cibertec.controlfit.service.PagoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
/*Controladores*/

@Controller
@RequestMapping("/admin/pagos")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class AdminPagoController extends ControllerSupport {

    private final PagoService pagoService;
    private final MembresiaService membresiaService;
    private final DevolucionService devolucionService;

    @GetMapping
    public String listar(Model model) {
        List<Pago> pagos = pagoService.obtenerTodosLosPagos();
        model.addAttribute("pagos", pagos);
        model.addAttribute("saldos", devolucionService.saldosPorPago(pagos));
        model.addAttribute("membresias", membresiaService.obtenerTodasLasMembresias());
        model.addAttribute("metodos", MetodoPago.values());
        model.addAttribute("estados", EstadoPago.values());
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new PagoForm());
        }
        return "admin/pagos";
    }

    @PostMapping
    public String registrar(@Valid @ModelAttribute("form") PagoForm form,
                            BindingResult resultado,
                            RedirectAttributes ra) {

        if (!hayErrores(resultado, ra)) {
            ejecutar(ra, () -> {
                Membresia membresia = new Membresia();
                membresia.setIdMembresia(form.getIdMembresia());

                Pago pago = new Pago();
                pago.setMembresia(membresia);
                pago.setMonto(form.getMonto());
                pago.setMetodoPago(form.getMetodoPago());
                pago.setEstado(form.getEstado());

                pagoService.registrarPago(pago);
                return "Pago registrado correctamente.";
            });
        }
        return "redirect:/admin/pagos";
    }
}
