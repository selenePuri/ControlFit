package com.cibertec.controlfit.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.cibertec.controlfit.config.AppConfig;
import com.cibertec.controlfit.config.SecurityConfig;
import com.cibertec.controlfit.controller.AdminController;
import com.cibertec.controlfit.controller.AdminUsuarioController;
import com.cibertec.controlfit.controller.PortalController;
import com.cibertec.controlfit.controller.RecepcionController;
import com.cibertec.controlfit.service.AsistenciaService;
import com.cibertec.controlfit.service.ClienteService;
import com.cibertec.controlfit.service.DashboardService;
import com.cibertec.controlfit.service.MembresiaService;
import com.cibertec.controlfit.service.PagoService;
import com.cibertec.controlfit.service.PromocionService;
import com.cibertec.controlfit.service.RutinaService;
import com.cibertec.controlfit.service.TipoMembresiaService;
import com.cibertec.controlfit.service.UsuarioService;

/** Matriz de acceso por rol: quién puede entrar a qué panel. No necesita MySQL. */
@WebMvcTest(controllers = { AdminController.class, AdminUsuarioController.class,
                            RecepcionController.class, PortalController.class })
@Import({ SecurityConfig.class, RolRedirectSuccessHandler.class, AppConfig.class })
class SecurityConfigTest {

    @Autowired MockMvc mvc;

    @MockitoBean DashboardService dashboardService;
    @MockitoBean AsistenciaService asistenciaService;
    @MockitoBean ClienteService clienteService;
    @MockitoBean MembresiaService membresiaService;
    @MockitoBean TipoMembresiaService tipoMembresiaService;
    @MockitoBean PromocionService promocionService;
    @MockitoBean PagoService pagoService;
    @MockitoBean RutinaService rutinaService;
    @MockitoBean UsuarioService usuarioService;

    @Test
    void anonimoEsEnviadoAlLogin() throws Exception {
        for (String url : new String[] { "/admin", "/recepcion", "/portal", "/admin/usuarios" }) {
            mvc.perform(get(url))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrlPattern("**/login"));
        }
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoEntraAAdminNiARecepcion() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().isForbidden());
        mvc.perform(get("/admin/usuarios")).andExpect(status().isForbidden());
        mvc.perform(get("/recepcion")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaNoEntraAAdminNiAlPortal() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().isForbidden());
        mvc.perform(get("/portal")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void adminNoEntraARecepcionNiAlPortal() throws Exception {
        mvc.perform(get("/recepcion")).andExpect(status().isForbidden());
        mvc.perform(get("/portal")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaNoPuedeCrearUsuarios() throws Exception {
        mvc.perform(post("/admin/usuarios").with(csrf())
                .param("usuario", "x").param("clave", "123456").param("rol", "ADMINISTRADOR"))
           .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void postSinTokenCsrfEsRechazado() throws Exception {
        mvc.perform(post("/admin/usuarios")
                .param("usuario", "x").param("clave", "123456").param("rol", "CLIENTE"))
           .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMINISTRADOR")
    void adminVeSuDashboard() throws Exception {
        when(dashboardService.resumenAdmin())
                .thenReturn(new DashboardService.ResumenAdmin(5, 3, new BigDecimal("1250.50"), 2));

        mvc.perform(get("/admin"))
           .andExpect(status().isOk())
           .andExpect(content().string(org.hamcrest.Matchers.containsString("Panel de Administración")));
    }
}
