package com.cibertec.controlfit.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.cibertec.controlfit.dto.RegistroClienteForm;
import com.cibertec.controlfit.model.Cliente;
import com.cibertec.controlfit.model.Usuario;
import com.cibertec.controlfit.service.ClienteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
/*Controladores*/

@Controller
@RequiredArgsConstructor
public class RegistroController {

    private final ClienteService clienteService;

    @GetMapping("/registro")
    public String formulario(Model model) {
        model.addAttribute("form", new RegistroClienteForm());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("form") RegistroClienteForm form,
                            BindingResult resultado,
                            Model model) {

        if (resultado.hasErrors()) {
            model.addAttribute("error", resultado.getAllErrors().get(0).getDefaultMessage());
            return "registro";
        }

        try {
            Cliente cliente = new Cliente();
            cliente.setDni(form.getDni());
            cliente.setNombres(form.getNombres());
            cliente.setApellidos(form.getApellidos());
            cliente.setTelefono(form.getTelefono());
            cliente.setCorreo(form.getCorreo());
            cliente.setFechaNacimiento(form.getFechaNacimiento());

            Usuario usuario = new Usuario();
            usuario.setUsuario(form.getCorreo());   
            usuario.setClave(form.getClave());

            clienteService.registrarClienteAutoservicio(cliente, usuario);

            return "redirect:/login?registrado";

        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "Ya existe un registro con ese DNI o correo electrónico.");
        }

        form.setClave(null);
        return "registro";
    }
}
