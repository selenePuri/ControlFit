package com.cibertec.controlfit.controller;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cibertec.controlfit.dto.UsuarioForm;
import com.cibertec.controlfit.model.Usuario;
import com.cibertec.controlfit.model.enums.Rol;
import com.cibertec.controlfit.security.UsuarioDetails;
import com.cibertec.controlfit.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
/*Controladores*/

@Controller
@RequestMapping("/admin/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class AdminUsuarioController extends ControllerSupport {

    private final UsuarioService usuarioService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.obtenerTodosLosUsuarios());
        model.addAttribute("roles", Rol.values());
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new UsuarioForm());
        }
        return "admin/usuarios";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") UsuarioForm form,
                        BindingResult resultado,
                        RedirectAttributes ra) {

        if (!hayErrores(resultado, ra)) {
            ejecutar(ra, () -> {
                Usuario usuario = new Usuario();
                usuario.setUsuario(form.getUsuario());
                usuario.setClave(form.getClave());
                usuario.setRol(form.getRol());
                usuario.setEstado(true);
                usuarioService.registrarUsuario(usuario);
                return "Usuario registrado correctamente.";
            });
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Integer id, RedirectAttributes ra) {
        ejecutar(ra, () -> {
            usuarioService.activarUsuario(id);
            return "Usuario activado correctamente.";
        });
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Integer id,
                             @AuthenticationPrincipal UsuarioDetails yo,
                             RedirectAttributes ra) {

        if (yo != null && id.equals(yo.getIdUsuario())) {
            error(ra, "No puedes desactivar tu propia cuenta.");
        } else {
            ejecutar(ra, () -> {
                usuarioService.desactivarUsuario(id);
                return "Usuario desactivado correctamente.";
            });
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Integer id,
                         @RequestParam String usuario,
                         @RequestParam Rol rol,
                         @AuthenticationPrincipal UsuarioDetails yo,
                         RedirectAttributes ra) {

        if (yo != null && id.equals(yo.getIdUsuario())) {
            error(ra, "Tu propia cuenta no se edita desde aquí.");
        } else {
            ejecutar(ra, () -> {
                Usuario existente = usuarioService.obtenerUsuarioPorId(id);
                existente.setUsuario(usuario.trim());
                existente.setRol(rol);
                usuarioService.actualizarUsuario(existente);  
                return "Usuario actualizado correctamente.";
            });
        }

        return "redirect:/admin/usuarios";
    }
}
