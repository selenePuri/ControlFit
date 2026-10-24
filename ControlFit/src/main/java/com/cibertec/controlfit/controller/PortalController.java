package com.cibertec.controlfit.controller;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cibertec.controlfit.dto.CambioClaveForm;
import com.cibertec.controlfit.dto.PerfilForm;
import com.cibertec.controlfit.model.Cliente;
import com.cibertec.controlfit.model.Membresia;
import com.cibertec.controlfit.security.UsuarioDetails;
import com.cibertec.controlfit.service.AsistenciaService;
import com.cibertec.controlfit.service.ClienteService;
import com.cibertec.controlfit.service.MembresiaService;
import com.cibertec.controlfit.service.PagoService;
import com.cibertec.controlfit.service.PromocionService;
import com.cibertec.controlfit.service.RutinaService;
import com.cibertec.controlfit.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
/*Controladores*/

@Controller
@RequestMapping("/portal")
@PreAuthorize("hasRole('CLIENTE')")
@RequiredArgsConstructor
public class PortalController extends ControllerSupport {

    private final ClienteService clienteService;
    private final MembresiaService membresiaService;
    private final PagoService pagoService;
    private final AsistenciaService asistenciaService;
    private final PromocionService promocionService;
    private final RutinaService rutinaService;
    private final UsuarioService usuarioService;
    private final Clock clock;

    @GetMapping
    public String panel(@AuthenticationPrincipal UsuarioDetails yo, Model model) {

        LocalDate hoy = LocalDate.now(clock);
        Cliente cliente = clienteService.obtenerClientePorUsuario(yo.getIdUsuario());

        model.addAttribute("hoy", hoy);
        model.addAttribute("cliente", cliente);
        model.addAttribute("promos", promocionService.obtenerPromocionesVigentes(hoy));

        if (cliente != null) {
            Membresia vigente = membresiaService.obtenerMembresiaVigente(cliente.getIdCliente(), hoy);

            model.addAttribute("membresia", vigente);
            if (vigente != null) {
                model.addAttribute("diasRestantes",
                        ChronoUnit.DAYS.between(hoy, vigente.getFechaFin()));
            }
            model.addAttribute("pagos",
                    pagoService.obtenerUltimosPagosDeCliente(cliente.getIdCliente()));
            model.addAttribute("asistencias",
                    asistenciaService.obtenerUltimasAsistenciasDeCliente(cliente.getIdCliente()));
            model.addAttribute("totalAsistencias",
                    asistenciaService.contarAsistenciasDeCliente(cliente.getIdCliente()));
            model.addAttribute("asistenciasMes",
                    asistenciaService.contarAsistenciasDelMes(cliente.getIdCliente(), hoy));
            model.addAttribute("rutinas",
                    rutinaService.obtenerRutinasActivasDeCliente(cliente.getIdCliente()));

            if (!model.containsAttribute("perfilForm")) {
                PerfilForm perfil = new PerfilForm();
                perfil.setTelefono(cliente.getTelefono());
                perfil.setCorreo(cliente.getCorreo());
                perfil.setFechaNacimiento(cliente.getFechaNacimiento());
                model.addAttribute("perfilForm", perfil);
            }
        }

        if (!model.containsAttribute("claveForm")) {
            model.addAttribute("claveForm", new CambioClaveForm());
        }

        return "portal/panel";
    }

    @PostMapping("/datos")
    public String actualizarDatos(@AuthenticationPrincipal UsuarioDetails yo,
                                  @Valid @ModelAttribute("perfilForm") PerfilForm form,
                                  BindingResult resultado,
                                  RedirectAttributes ra) {

        if (!hayErrores(resultado, ra)) {
            ejecutar(ra, () -> {
                clienteService.actualizarDatosContacto(
                        yo.getIdUsuario(), form.getTelefono(), form.getCorreo(), form.getFechaNacimiento());
                return "Tus datos se guardaron correctamente.";
            });
        }

        return "redirect:/portal#datos";
    }

    @PostMapping("/clave")
    public String cambiarClave(@AuthenticationPrincipal UsuarioDetails yo,
                               @ModelAttribute("claveForm") CambioClaveForm form,
                               RedirectAttributes ra) {

        ejecutar(ra, () -> {
            usuarioService.cambiarClavePropia(
                    yo.getIdUsuario(), form.getClaveActual(), form.getClaveNueva(), form.getClaveConfirmar());
            return "Tu contraseña fue actualizada.";
        });

        return "redirect:/portal#clave";
    }
}
