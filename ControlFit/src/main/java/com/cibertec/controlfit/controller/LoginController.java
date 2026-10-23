package com.cibertec.controlfit.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.cibertec.controlfit.security.RutasPorRol;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(Authentication autenticacion) {

        if (autenticacion != null && autenticacion.isAuthenticated()
                && RutasPorRol.panelDe(autenticacion).isPresent()) {
            return "redirect:" + RutasPorRol.panelDe(autenticacion).get();
        }

        return "login";
    }

    @GetMapping("/")
    public String inicio(Authentication autenticacion) {
        return "redirect:" + RutasPorRol.panelDe(autenticacion).orElse("/login");
    }
}
