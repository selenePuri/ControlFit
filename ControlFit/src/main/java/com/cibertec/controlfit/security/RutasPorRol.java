package com.cibertec.controlfit.security;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
/*Seguridad*/

public final class RutasPorRol {

    private RutasPorRol() {
    }

    public static Optional<String> panelDe(Authentication autenticacion) {

        if (autenticacion == null) {
            return Optional.empty();
        }

        for (GrantedAuthority autoridad : autenticacion.getAuthorities()) {
            switch (autoridad.getAuthority()) {
                case "ROLE_ADMINISTRADOR":
                    return Optional.of("/admin");
                case "ROLE_RECEPCIONISTA":
                    return Optional.of("/recepcion");
                case "ROLE_CLIENTE":
                    return Optional.of("/portal");
                default:
                    break;
            }
        }

        return Optional.empty();
    }
}
