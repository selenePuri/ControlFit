package com.cibertec.controlfit.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cibertec.controlfit.config.AppConfig;
import com.cibertec.controlfit.model.Cliente;
import com.cibertec.controlfit.model.Usuario;
import com.cibertec.controlfit.repository.ClienteRepository;
import com.cibertec.controlfit.repository.UsuarioRepository;

/** El registro público no debe permitir "reclamar" el perfil de otra persona solo con su DNI. */
@ExtendWith(MockitoExtension.class)
class ClienteServiceRegistroTest {

    @Mock ClienteRepository clienteRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;

    ClienteService service;

    @BeforeEach
    void preparar() {
        service = new ClienteService(clienteRepository, usuarioRepository, passwordEncoder,
                Clock.fixed(Instant.parse("2026-10-18T15:00:00Z"), AppConfig.ZONA_LIMA));
    }

    private Cliente solicitante(String correo) {
        Cliente c = new Cliente();
        c.setDni("12345678");
        c.setCorreo(correo);
        return c;
    }

    @Test
    void noVinculaSiElCorreoNoCoincideConElDeRecepcion() {
        Cliente existente = solicitante("real@correo.com");
        when(clienteRepository.findByDni("12345678")).thenReturn(Optional.of(existente));

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> service.registrarClienteAutoservicio(solicitante("intruso@correo.com"), new Usuario()));

        assertTrue(e.getMessage().contains("acércate a recepción"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void noVinculaSiElClienteExistenteNoTieneCorreoRegistrado() {
        when(clienteRepository.findByDni("12345678")).thenReturn(Optional.of(solicitante(null)));

        assertThrows(IllegalStateException.class,
                () -> service.registrarClienteAutoservicio(solicitante("alguien@correo.com"), new Usuario()));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void noVinculaSiElClienteYaTieneCuenta() {
        Cliente existente = solicitante("real@correo.com");
        existente.setUsuario(new Usuario());
        when(clienteRepository.findByDni("12345678")).thenReturn(Optional.of(existente));

        assertThrows(IllegalStateException.class,
                () -> service.registrarClienteAutoservicio(solicitante("real@correo.com"), new Usuario()));
        verify(usuarioRepository, never()).save(any());
    }
}
