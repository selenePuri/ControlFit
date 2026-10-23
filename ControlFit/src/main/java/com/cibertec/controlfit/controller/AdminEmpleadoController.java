package com.cibertec.controlfit.controller;

import com.cibertec.controlfit.dto.EmpleadoEdicionForm;
import com.cibertec.controlfit.dto.ModoCuenta;
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

import com.cibertec.controlfit.dto.EmpleadoForm;
import com.cibertec.controlfit.model.Empleado;
import com.cibertec.controlfit.model.Usuario;
import com.cibertec.controlfit.model.enums.Rol;
import com.cibertec.controlfit.service.EmpleadoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/admin/empleados")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class AdminEmpleadoController extends ControllerSupport {

    private final EmpleadoService empleadoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("empleados", empleadoService.obtenerTodosLosEmpleados());
        model.addAttribute("roles", new Rol[] { Rol.ADMINISTRADOR, Rol.RECEPCIONISTA, Rol.ENTRENADOR });
        model.addAttribute("usuariosDisponibles", empleadoService.obtenerUsuariosDisponibles());
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new EmpleadoForm());
        }
        return "admin/empleados";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") EmpleadoForm form,
                        BindingResult resultado,
                        RedirectAttributes ra) {

        if (!hayErrores(resultado, ra)) {
            ejecutar(ra, () -> {
                Empleado empleado = new Empleado();
                empleado.setDni(form.getDni());
                empleado.setNombres(form.getNombres());
                empleado.setApellidos(form.getApellidos());
                empleado.setTelefono(form.getTelefono());
                empleado.setCorreo(form.getCorreo());
                empleado.setFechaContratacion(form.getFechaContratacion());
                empleado.setEstado(true);

                if (form.getModoCuenta() == ModoCuenta.EXISTENTE) {
                    empleadoService.registrarEmpleadoConUsuarioExistente(empleado, form.getIdUsuario());
                    return "Empleado registrado y vinculado al usuario existente.";
                }

                Usuario usuario = new Usuario();
                usuario.setUsuario(form.getUsuario() == null ? null : form.getUsuario().trim());
                usuario.setClave(form.getClave());
                usuario.setRol(form.getRol());
                usuario.setEstado(true);

                empleadoService.registrarEmpleadoConCuenta(empleado, usuario);
                return "Empleado registrado con una cuenta nueva.";
            });
        }
        return "redirect:/admin/empleados";
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Integer id, RedirectAttributes ra) {
        ejecutar(ra, () -> {
            empleadoService.activarEmpleado(id);
            return "Empleado activado correctamente.";
        });
        return "redirect:/admin/empleados";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Integer id, RedirectAttributes ra) {
        ejecutar(ra, () -> {
            empleadoService.desactivarEmpleado(id);
            return "Empleado desactivado correctamente.";
        });
        return "redirect:/admin/empleados";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Integer id,
                         @Valid @ModelAttribute("edicion") EmpleadoEdicionForm form,
                         BindingResult resultado,
                         RedirectAttributes ra) {

        if (!hayErrores(resultado, ra)) {
            ejecutar(ra, () -> {
                Empleado empleado = empleadoService.obtenerEmpleadoPorId(id);
                empleado.setDni(form.getDni());
                empleado.setNombres(form.getNombres());
                empleado.setApellidos(form.getApellidos());
                empleado.setTelefono(form.getTelefono());
                empleado.setCorreo(form.getCorreo());
                empleado.setFechaContratacion(form.getFechaContratacion());
                empleadoService.actualizarEmpleado(empleado);   
                return "Empleado actualizado correctamente.";
            });
        }

        return "redirect:/admin/empleados";
    }
}
