package com.cibertec.controlfit.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cibertec.controlfit.model.Empleado;
import com.cibertec.controlfit.model.Usuario;
import com.cibertec.controlfit.model.enums.Rol;
import com.cibertec.controlfit.repository.EmpleadoRepository;
import com.cibertec.controlfit.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
/*Logica de negocio*/

@Service
@Transactional
@RequiredArgsConstructor
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    private static final List<Rol> ROLES_EMPLEADO =
            List.of(Rol.ADMINISTRADOR, Rol.RECEPCIONISTA, Rol.ENTRENADOR);

    public void registrarEmpleado(Empleado empleado) {

        validarEmpleado(empleado);

        if (empleadoRepository.findByDni(empleado.getDni()).isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe un empleado registrado con ese DNI.");
        }

        Usuario usuario = obtenerUsuarioValido(empleado.getUsuario());
        validarUsuarioSinEmpleado(usuario, null);
        empleado.setUsuario(usuario);

        if (empleado.getEstado() == null) {
            empleado.setEstado(true);
        }

        empleadoRepository.save(empleado);
    }

    public void registrarEmpleadoConCuenta(Empleado empleado, Usuario usuario) {

        if (empleado == null) {
            throw new IllegalArgumentException("El empleado es obligatorio.");
        }

        if (usuario == null || usuario.getRol() == null) {
            throw new IllegalArgumentException("El usuario del empleado debe tener un rol.");
        }

        validarRolEmpleado(usuario);

        usuarioService.registrarUsuario(usuario);

        empleado.setUsuario(usuario);

        registrarEmpleado(empleado);
    }

    public void registrarEmpleadoConUsuarioExistente(Empleado empleado, Integer idUsuario) {
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado es obligatorio.");
        }
        if (idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("Selecciona el usuario que se vinculará al empleado.");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("El usuario seleccionado no existe."));

        if (!Boolean.TRUE.equals(usuario.getEstado())) {
            throw new IllegalArgumentException("El usuario seleccionado está inactivo.");
        }

        empleado.setUsuario(usuario);
        registrarEmpleado(empleado);   
    }

    @Transactional(readOnly = true)
    public List<Usuario> obtenerUsuariosDisponibles() {
        Set<Integer> vinculados = empleadoRepository.findAll().stream()
                .map(e -> e.getUsuario().getIdUsuario())
                .collect(Collectors.toSet());

        return usuarioRepository.findByEstadoTrueAndRolInOrderByUsuarioAsc(ROLES_EMPLEADO).stream()
                .filter(u -> !vinculados.contains(u.getIdUsuario()))
                .toList();
    }

    public void actualizarEmpleado(Empleado empleado) {

        if (empleado == null || empleado.getIdEmpleado() == null) {
            throw new IllegalArgumentException(
                    "El empleado que desea actualizar no es válido.");
        }

        if (!empleadoRepository.existsById(empleado.getIdEmpleado())) {
            throw new IllegalArgumentException("El empleado no existe.");
        }

        validarEmpleado(empleado);

        empleadoRepository.findByDni(empleado.getDni())
                .filter(mismoDni -> !mismoDni.getIdEmpleado().equals(empleado.getIdEmpleado()))
                .ifPresent(mismoDni -> {
                    throw new IllegalArgumentException("El DNI pertenece a otro empleado.");
                });

        Usuario usuario = obtenerUsuarioValido(empleado.getUsuario());
        validarUsuarioSinEmpleado(usuario, empleado.getIdEmpleado());
        empleado.setUsuario(usuario);

        empleadoRepository.save(empleado);
    }

    @Transactional(readOnly = true)
    public Empleado obtenerEmpleadoPorId(Integer idEmpleado) {

        if (idEmpleado == null || idEmpleado <= 0) {
            throw new IllegalArgumentException("El código del empleado no es válido.");
        }

        return empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new IllegalArgumentException("El empleado no existe."));
    }

    @Transactional(readOnly = true)
    public Empleado obtenerEmpleadoPorDni(String dni) {

        if (dni == null || dni.isBlank()) {
            throw new IllegalArgumentException("El DNI es obligatorio.");
        }

        return empleadoRepository.findByDni(dni).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Empleado> obtenerTodosLosEmpleados() {
        return empleadoRepository.findAll();
    }

    public void desactivarEmpleado(Integer idEmpleado) {

        Empleado empleado = obtenerEmpleadoPorId(idEmpleado);
        empleado.setEstado(false);
        empleadoRepository.save(empleado);
    }

    public void activarEmpleado(Integer idEmpleado) {

        Empleado empleado = obtenerEmpleadoPorId(idEmpleado);
        empleado.setEstado(true);
        empleadoRepository.save(empleado);
    }

    @Transactional(readOnly = true)
    public Empleado obtenerEmpleadoActivo(Integer idEmpleado) {

        Empleado empleado = obtenerEmpleadoPorId(idEmpleado);

        if (!Boolean.TRUE.equals(empleado.getEstado())) {
            throw new IllegalStateException("El empleado se encuentra inactivo.");
        }

        return empleado;
    }

    private void validarEmpleado(Empleado empleado) {

        if (empleado == null) {
            throw new IllegalArgumentException("El empleado es obligatorio.");
        }

        if (empleado.getDni() == null || empleado.getDni().isBlank()) {
            throw new IllegalArgumentException("El DNI del empleado es obligatorio.");
        }

        if (!empleado.getDni().matches("\\d{8}")) {
            throw new IllegalArgumentException(
                    "El DNI debe contener exactamente 8 números.");
        }

        if (empleado.getUsuario() == null || empleado.getUsuario().getIdUsuario() == null) {
            throw new IllegalArgumentException(
                    "El empleado debe estar asociado a un usuario registrado.");
        }
    }

    private Usuario obtenerUsuarioValido(Usuario usuario) {

        if (usuario == null || usuario.getIdUsuario() == null) {
            throw new IllegalArgumentException(
                    "El empleado debe estar asociado a un usuario registrado.");
        }

        Usuario existente = usuarioRepository.findById(usuario.getIdUsuario())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El usuario asociado al empleado no existe."));

        validarRolEmpleado(existente);

        return existente;
    }

    private void validarUsuarioSinEmpleado(Usuario usuario, Integer idEmpleadoActual) {
        empleadoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
                .filter(otro -> idEmpleadoActual == null || !otro.getIdEmpleado().equals(idEmpleadoActual))
                .ifPresent(otro -> {
                    throw new IllegalArgumentException("El usuario \"" + usuario.getUsuario()
                            + "\" ya está vinculado al empleado " + otro.getNombres() + " "
                            + otro.getApellidos() + ".");
                });
    }

    private void validarRolEmpleado(Usuario usuario) {

        if (usuario.getRol() == null) {
            throw new IllegalArgumentException(
                    "El usuario del empleado debe tener un rol.");
        }

        Rol rol = usuario.getRol();

        if (rol != Rol.ADMINISTRADOR
                && rol != Rol.RECEPCIONISTA
                && rol != Rol.ENTRENADOR) {

            throw new IllegalArgumentException(
                    "El usuario asociado no tiene un rol válido para un empleado.");
        }
    }
}
