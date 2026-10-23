package com.cibertec.controlfit.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class RolRedirectSuccessHandlerTest {

    private final RolRedirectSuccessHandler handler = new RolRedirectSuccessHandler();

    private String destinoPara(String rol) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        var auth = new UsernamePasswordAuthenticationToken(
                "u", "p", List.of(new SimpleGrantedAuthority("ROLE_" + rol)));

        handler.onAuthenticationSuccess(request, response, auth);

        return response.getRedirectedUrl();
    }

    @Test
    void cadaRolVaASuPanel() throws Exception {
        assertEquals("/admin", destinoPara("ADMINISTRADOR"));
        assertEquals("/recepcion", destinoPara("RECEPCIONISTA"));
        assertEquals("/portal", destinoPara("CLIENTE"));
    }

    @Test
    void rolSinPanelVuelveAlLogin() throws Exception {
        assertEquals("/login?rol", destinoPara("ENTRENADOR"));
    }
}
