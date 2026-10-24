package com.cibertec.controlfit.controller;
/*Controladores*/
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

import com.cibertec.controlfit.dto.ClienteForm;
import com.cibertec.controlfit.model.Cliente;
import com.cibertec.controlfit.service.ClienteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/clientes")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class AdminClienteController extends ControllerSupport {

    private final ClienteService clienteService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", clienteService.obtenerTodosLosClientes());
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ClienteForm());
        }
        return "admin/clientes";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") ClienteForm form,
                        BindingResult resultado,
                        RedirectAttributes ra) {

        if (!hayErrores(resultado, ra)) {
            ejecutar(ra, () -> {
                clienteService.registrarCliente(aCliente(form));
                return "Cliente registrado: " + form.getNombres() + " " + form.getApellidos() + ".";
            });
        }
        return "redirect:/admin/clientes";
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Integer id, RedirectAttributes ra) {
        ejecutar(ra, () -> {
            clienteService.activarCliente(id);
            return "Cliente activado correctamente.";
        });
        return "redirect:/admin/clientes";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Integer id, RedirectAttributes ra) {
        ejecutar(ra, () -> {
            clienteService.desactivarCliente(id);
            return "Cliente desactivado correctamente.";
        });
        return "redirect:/admin/clientes";
    }

    static Cliente aCliente(ClienteForm form) {
        Cliente cliente = new Cliente();
        cliente.setDni(form.getDni());
        cliente.setNombres(form.getNombres());
        cliente.setApellidos(form.getApellidos());
        cliente.setTelefono(form.getTelefono());
        cliente.setCorreo(form.getCorreo());
        cliente.setFechaNacimiento(form.getFechaNacimiento());
        cliente.setEstado(form.getEstado() == null ? Boolean.TRUE : form.getEstado());
        return cliente;
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Integer id,
                         @Valid @ModelAttribute("form") ClienteForm form,
                         BindingResult resultado,
                         RedirectAttributes ra) {

        if (!hayErrores(resultado, ra)) {
            ejecutar(ra, () -> {
                Cliente cliente = aCliente(form);
                cliente.setIdCliente(id);
                cliente.setEstado(null);  
                clienteService.actualizarCliente(cliente);
                return "Cliente actualizado: " + form.getNombres() + " " + form.getApellidos() + ".";
            });
        }

        return "redirect:/admin/clientes";
    }
}
