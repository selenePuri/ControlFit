package com.cibertec.controlfit.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cibertec.controlfit.model.Empleado;
import com.cibertec.controlfit.model.Entrenador;
import com.cibertec.controlfit.model.Usuario;
import com.cibertec.controlfit.model.enums.Rol;
import com.cibertec.controlfit.repository.EntrenadorRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class EntrenadorService {

    private final EntrenadorRepository entrenadorRepository;
    private final EmpleadoService empleadoService;

    public void registrarEntrenador(Entrenador entrenador) {

        validarDatosEntrenador(entrenador);

        Empleado empleado = obtenerEmpleadoValido(entrenador.getEmpleado());
        entrenador.setEmpleado(empleado);

        entrenadorRepository.save(entrenador);
    }

    public void actualizarEntrenador(Entrenador entrenador) {

        if (entrenador == null || entrenador.getIdEntrenador() == null) {
            throw new IllegalArgumentException(
                    "El entrenador que desea actualizar no es válido.");
        }

        if (!entrenadorRepository.existsById(entrenador.getIdEntrenador())) {
            throw new IllegalArgumentException("El entrenador no existe.");
        }

        validarDatosEntrenador(entrenador);

        Empleado empleado = obtenerEmpleadoValido(entrenador.getEmpleado());
        entrenador.setEmpleado(empleado);

        entrenadorRepository.save(entrenador);
    }

    @Transactional(readOnly = true)
    public Entrenador obtenerEntrenadorPorId(Integer idEntrenador) {

        if (idEntrenador == null || idEntrenador <= 0) {
            throw new IllegalArgumentException("El código del entrenador no es válido.");
        }

        return entrenadorRepository.findById(idEntrenador)
                .orElseThrow(() -> new IllegalArgumentException("El entrenador no existe."));
    }

    @Transactional(readOnly = true)
    public List<Entrenador> obtenerTodosLosEntrenadores() {
        return entrenadorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Entrenador> obtenerPorEspecialidad(String especialidad) {

        if (especialidad == null || especialidad.isBlank()) {
            throw new IllegalArgumentException("La especialidad es obligatoria.");
        }

        return entrenadorRepository.findByEspecialidad(especialidad);
    }

    @Transactional(readOnly = true)
    public List<Entrenador> obtenerEntrenadoresActivos() {
        return entrenadorRepository.findByEmpleado_EstadoTrue();
    }

    @Transactional(readOnly = true)
    public Entrenador obtenerEntrenadorHabilitado(Integer idEntrenador) {

        Entrenador entrenador = obtenerEntrenadorPorId(idEntrenador);

        Empleado empleado = empleadoService.obtenerEmpleadoActivo(
                entrenador.getEmpleado().getIdEmpleado());

        validarRolEntrenador(empleado.getUsuario());

        if (!Boolean.TRUE.equals(empleado.getUsuario().getEstado())) {
            throw new IllegalStateException(
                    "La cuenta del entrenador se encuentra inactiva.");
        }

        entrenador.setEmpleado(empleado);

        return entrenador;
    }

    private void validarDatosEntrenador(Entrenador entrenador) {

        if (entrenador == null) {
            throw new IllegalArgumentException("El entrenador es obligatorio.");
        }

        if (entrenador.getEmpleado() == null
                || entrenador.getEmpleado().getIdEmpleado() == null) {

            throw new IllegalArgumentException(
                    "El entrenador debe estar asociado a un empleado registrado.");
        }
    }

    private Empleado obtenerEmpleadoValido(Empleado empleado) {

        if (empleado == null || empleado.getIdEmpleado() == null) {
            throw new IllegalArgumentException(
                    "El entrenador debe estar asociado a un empleado registrado.");
        }

        Empleado existente = empleadoService.obtenerEmpleadoPorId(empleado.getIdEmpleado());

        Usuario usuario = existente.getUsuario();

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El entrenador debe tener una cuenta de usuario asociada.");
        }

        validarRolEntrenador(usuario);

        return existente;
    }

    private void validarRolEntrenador(Usuario usuario) {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El entrenador debe tener una cuenta de usuario asociada.");
        }

        if (usuario.getRol() != Rol.ENTRENADOR) {
            throw new IllegalArgumentException(
                    "El usuario asociado debe tener rol ENTRENADOR.");
        }
    }
}
