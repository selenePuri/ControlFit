package com.cibertec.controlfit.security;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RolRedirectSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        Optional<String> panel = RutasPorRol.panelDe(authentication);

        if (panel.isPresent()) {
            response.sendRedirect(request.getContextPath() + panel.get());
            return;
        }

        new SecurityContextLogoutHandler().logout(request, response, authentication);
        response.sendRedirect(request.getContextPath() + "/login?rol");
    }
}

