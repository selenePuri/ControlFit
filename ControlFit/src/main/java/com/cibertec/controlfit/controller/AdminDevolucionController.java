package com.cibertec.controlfit.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cibertec.controlfit.dto.DevolucionForm;
import com.cibertec.controlfit.model.Devolucion;
import com.cibertec.controlfit.model.Pago;
import com.cibertec.controlfit.model.enums.EstadoDevolucion;
import com.cibertec.controlfit.service.DevolucionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/devoluciones")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class AdminDevolucionController extends ControllerSupport {

    private final DevolucionService devolucionService;

    @GetMapping
    public String listar(@RequestParam(name = "pago", required = false) Integer idPagoSeleccionado, Model model) {
        List<Pago> devolubles = devolucionService.obtenerPagosDevolubles();
        Map<Integer, DevolucionService.SaldoPago> saldos = devolucionService.saldosPorPago(devolubles);

        model.addAttribute("devoluciones", devolucionService.obtenerTodasLasDevoluciones());
        model.addAttribute("pagosDevolubles", devolubles);
        model.addAttribute("saldos", saldos);
        model.addAttribute("estadosIniciales",
                new EstadoDevolucion[] { EstadoDevolucion.COMPLETADA, EstadoDevolucion.PENDIENTE });

        if (!model.containsAttribute("form")) {
            DevolucionForm form = new DevolucionForm();
            if (idPagoSeleccionado != null && saldos.containsKey(idPagoSeleccionado)) {
                form.setIdPago(idPagoSeleccionado);
                form.setMonto(saldos.get(idPagoSeleccionado).saldo());
                model.addAttribute("abrirFormulario", true);
            }
            model.addAttribute("form", form);
        }
        return "admin/devoluciones";
    }

    @PostMapping
    public String registrar(@Valid @ModelAttribute("form") DevolucionForm form,
                            BindingResult resultado,
                            RedirectAttributes ra) {

        if (!hayErrores(resultado, ra)) {
            ejecutar(ra, () -> {
                Devolucion d = devolucionService.registrarDevolucion(
                        form.getIdPago(), form.getMonto(), form.getMotivo(), form.getEstado());
                return "Devolución #D" + d.getIdDevolucion() + " registrada (" + d.getEstado()
                        + "). El pago original no se modificó.";
            });
        }
        return "redirect:/admin/devoluciones";
    }

    @PostMapping("/{id}/completar")
    public String completar(@PathVariable Integer id, RedirectAttributes ra) {
        ejecutar(ra, () -> {
            devolucionService.completarDevolucion(id);
            return "Devolución completada.";
        });
        return "redirect:/admin/devoluciones";
    }

    @PostMapping("/{id}/anular")
    public String anular(@PathVariable Integer id, RedirectAttributes ra) {
        ejecutar(ra, () -> {
            devolucionService.anularDevolucion(id);
            return "Devolución anulada. El saldo del pago volvió a estar disponible.";
        });
        return "redirect:/admin/devoluciones";
    }
}
