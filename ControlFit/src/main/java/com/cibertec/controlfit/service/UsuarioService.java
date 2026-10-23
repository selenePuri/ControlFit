package com.cibertec.controlfit.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cibertec.controlfit.model.Usuario;
import com.cibertec.controlfit.model.enums.Rol;
import com.cibertec.controlfit.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public void registrarUsuario(Usuario usuario) {

        validarDatosUsuario(usuario);

        if (usuarioRepository.existsByUsuario(usuario.getUsuario())) {
            throw new IllegalArgumentException(
                    "El nombre de usuario ya se encuentra registrado.");
        }

        usuario.setClave(passwordEncoder.encode(usuario.getClave()));

        usuarioRepository.save(usuario);
    }

    public void actualizarUsuario(Usuario usuario) {

        if (usuario == null || usuario.getIdUsuario() == null) {
            throw new IllegalArgumentException(
                    "El usuario que desea actualizar no es válido.");
        }

        Usuario existente = usuarioRepository
                .findById(usuario.getIdUsuario())
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));

        validarDatosActualizacion(usuario);

        usuarioRepository.findByUsuario(usuario.getUsuario())
                .filter(mismoNombre -> !mismoNombre.getIdUsuario().equals(usuario.getIdUsuario()))
                .ifPresent(mismoNombre -> {
                    throw new IllegalArgumentException(
                            "El nombre de usuario ya pertenece a otra cuenta.");
                });

        usuario.setClave(existente.getClave());

        usuarioRepository.save(usuario);
    }

    public void cambiarClave(Integer idUsuario, String nuevaClave) {

        Usuario usuario = obtenerUsuarioExistente(idUsuario);

        if (nuevaClave == null || nuevaClave.isBlank()) {
            throw new IllegalArgumentException("La nueva contraseña es obligatoria.");
        }

        usuario.setClave(passwordEncoder.encode(nuevaClave));

        usuarioRepository.save(usuario);
    }

    public void cambiarClavePropia(Integer idUsuario,
                                   String claveActual,
                                   String claveNueva,
                                   String confirmacion) {

        if (claveActual == null || claveActual.isEmpty()
                || claveNueva == null || claveNueva.isEmpty()) {
            throw new IllegalArgumentException(
                    "Completa la contraseña actual y la nueva.");
        }

        if (claveNueva.length() < 6) {
            throw new IllegalArgumentException(
                    "La nueva contraseña debe tener al menos 6 caracteres.");
        }

        if (!claveNueva.equals(confirmacion)) {
            throw new IllegalArgumentException(
                    "La confirmación no coincide con la nueva contraseña.");
        }

        Usuario usuario = obtenerUsuarioExistente(idUsuario);

        if (!passwordEncoder.matches(claveActual, usuario.getClave())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }

        usuario.setClave(passwordEncoder.encode(claveNueva));

        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorId(Integer idUsuario) {
        return obtenerUsuarioExistente(idUsuario);
    }

    @Transactional(readOnly = true)
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    public void desactivarUsuario(Integer idUsuario) {

        Usuario usuario = obtenerUsuarioExistente(idUsuario);
        usuario.setEstado(false);
        usuarioRepository.save(usuario);
    }

    public void activarUsuario(Integer idUsuario) {

        Usuario usuario = obtenerUsuarioExistente(idUsuario);
        usuario.setEstado(true);
        usuarioRepository.save(usuario);
    }

    private Usuario obtenerUsuarioExistente(Integer idUsuario) {

        if (idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("El código del usuario no es válido.");
        }

        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
    }

    private void validarDatosUsuario(Usuario usuario) {

        if (usuario == null) {
            throw new IllegalArgumentException("El usuario es obligatorio.");
        }

        if (usuario.getUsuario() == null || usuario.getUsuario().isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }

        if (usuario.getClave() == null || usuario.getClave().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }

        if (usuario.getRol() == null) {
            throw new IllegalArgumentException("El usuario debe tener un rol.");
        }

        validarRol(usuario.getRol());

        if (usuario.getEstado() == null) {
            usuario.setEstado(true);
        }
    }

    private void validarDatosActualizacion(Usuario usuario) {

        if (usuario.getUsuario() == null || usuario.getUsuario().isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }

        if (usuario.getRol() == null) {
            throw new IllegalArgumentException("El usuario debe tener un rol.");
        }

        validarRol(usuario.getRol());

        if (usuario.getEstado() == null) {
            usuario.setEstado(true);
        }
    }

    private void validarRol(Rol rol) {

        if (rol != Rol.ADMINISTRADOR
                && rol != Rol.RECEPCIONISTA
                && rol != Rol.ENTRENADOR
                && rol != Rol.CLIENTE) {

            throw new IllegalArgumentException("El rol del usuario no es válido.");
        }
    }
}
