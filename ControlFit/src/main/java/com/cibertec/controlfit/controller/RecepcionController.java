package com.cibertec.controlfit.controller;

import com.cibertec.controlfit.model.Cliente;
import java.time.Clock;
import java.time.LocalDate;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cibertec.controlfit.dto.ClienteForm;
import com.cibertec.controlfit.dto.MembresiaForm;
import com.cibertec.controlfit.model.enums.MetodoPago;
import com.cibertec.controlfit.security.UsuarioDetails;
import com.cibertec.controlfit.service.AsistenciaService;
import com.cibertec.controlfit.service.ClienteService;
import com.cibertec.controlfit.service.DashboardService;
import com.cibertec.controlfit.service.MembresiaService;
import com.cibertec.controlfit.service.PagoService;
import com.cibertec.controlfit.service.PromocionService;
import com.cibertec.controlfit.service.TipoMembresiaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
/*Controladores*/

@Controller
@RequestMapping("/recepcion")
@PreAuthorize("hasRole('RECEPCIONISTA')")
@RequiredArgsConstructor
public class RecepcionController extends ControllerSupport {

    private final DashboardService dashboardService;
    private final AsistenciaService asistenciaService;
    private final ClienteService clienteService;
    private final MembresiaService membresiaService;
    private final TipoMembresiaService tipoMembresiaService;
    private final PromocionService promocionService;
    private final PagoService pagoService;
    private final Clock clock;

    @GetMapping
    public String panel(@AuthenticationPrincipal UsuarioDetails yo, Model model) {

        LocalDate hoy = LocalDate.now(clock);

        model.addAttribute("hoy", hoy);
        model.addAttribute("resumen", dashboardService.resumenRecepcion());
        model.addAttribute("clientes", clienteService.obtenerClientesActivos());
        model.addAttribute("clientesTodos", clienteService.obtenerTodosLosClientes());
        model.addAttribute("membresias", membresiaService.obtenerTodasLasMembresias());
        model.addAttribute("tipos", tipoMembresiaService.obtenerTiposMembresiaActivos());
        model.addAttribute("promos", promocionService.obtenerPromocionesVigentes(hoy));
        model.addAttribute("asistenciasHoy", asistenciaService.obtenerAsistenciasDeHoy());
        model.addAttribute("pagosRecientes", pagoService.obtenerUltimosPagos());
        model.addAttribute("metodos", MetodoPago.values());

        if (!model.containsAttribute("clienteForm")) {
            model.addAttribute("clienteForm", new ClienteForm());
        }
        if (!model.containsAttribute("membresiaForm")) {
            MembresiaForm form = new MembresiaForm();
            form.setFechaInicio(hoy);
            model.addAttribute("membresiaForm", form);
        }

        return "recepcion/panel";
    }

    @PostMapping("/entrada")
    public String entrada(@RequestParam(required = false) String dni, RedirectAttributes ra) {

        String dniLimpio = dni == null ? null : dni.trim();

        ejecutar(ra, () -> {
            asistenciaService.registrarEntradaPorDni(dniLimpio);
            return "Entrada registrada para el DNI " + dniLimpio + ".";
        });

        return "redirect:/recepcion#asistencias";
    }

    @PostMapping("/salida")
    public String salida(@RequestParam Integer idAsistencia, RedirectAttributes ra) {

        ejecutar(ra, () -> {
            asistenciaService.registrarSalidaPorAsistencia(idAsistencia);
            return "Salida registrada correctamente.";
        });

        return "redirect:/recepcion#asistencias";
    }

    @PostMapping("/clientes")
    public String registrarCliente(@Valid @ModelAttribute("clienteForm") ClienteForm form,
                                   BindingResult resultado,
                                   RedirectAttributes ra) {

        if (!hayErrores(resultado, ra)) {
            ejecutar(ra, () -> {
                form.setEstado(Boolean.TRUE);   
                clienteService.registrarCliente(AdminClienteController.aCliente(form));
                return "Cliente registrado: " + form.getNombres() + " " + form.getApellidos() + ".";
            });
        }

        return "redirect:/recepcion#clientes";
    }

    @PostMapping("/membresias")
    public String venderMembresia(@Valid @ModelAttribute("membresiaForm") MembresiaForm form,
                                  BindingResult resultado,
                                  RedirectAttributes ra) {

        if (!hayErrores(resultado, ra)) {
            ejecutar(ra, () -> mensajeVenta(membresiaService.venderMembresia(
                    form.getIdCliente(), form.getIdTipo(), form.getIdPromocion(),
                    form.getFechaInicio(), form.getMetodoPago())));
        }

        return "redirect:/recepcion#membresias";
    }

    @PostMapping("/clientes/{id}/editar")
    public String editarCliente(@PathVariable Integer id,
                                @Valid @ModelAttribute("clienteForm") ClienteForm form,
                                BindingResult resultado,
                                RedirectAttributes ra) {

        if (!hayErrores(resultado, ra)) {
            ejecutar(ra, () -> {
                Cliente cliente = AdminClienteController.aCliente(form);
                cliente.setIdCliente(id);
                cliente.setEstado(null);   
                clienteService.actualizarCliente(cliente);
                return "Cliente actualizado: " + form.getNombres() + " " + form.getApellidos() + ".";
            });
        }

        return "redirect:/recepcion#clientes";
    }
}
