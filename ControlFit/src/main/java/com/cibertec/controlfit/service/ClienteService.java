package com.cibertec.controlfit.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cibertec.controlfit.model.Cliente;
import com.cibertec.controlfit.model.Usuario;
import com.cibertec.controlfit.model.enums.Rol;
import com.cibertec.controlfit.repository.ClienteRepository;
import com.cibertec.controlfit.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ClienteService {

    private static final String REGEX_TELEFONO = "9[0-9]{8}";
    private static final String REGEX_CORREO =
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public void registrarCliente(Cliente cliente) {

        validarCliente(cliente);

        if (clienteRepository.existsByDni(cliente.getDni())) {
            throw new IllegalArgumentException(
                    "Ya existe un cliente registrado con ese DNI.");
        }

        cliente.setUsuario(null);

        if (cliente.getEstado() == null) {
            cliente.setEstado(true);
        }

        clienteRepository.save(cliente);
    }

    public void crearCuentaCliente(String dni, Usuario usuario) {

        Cliente cliente = obtenerClienteExistentePorDni(dni);

        if (cliente.getUsuario() != null) {
            throw new IllegalStateException(
                    "El cliente ya tiene una cuenta de usuario.");
        }

        validarDatosCuenta(usuario);
        validarNombreUsuarioDisponible(usuario);

        prepararCuentaCliente(usuario);

        vincularCuentaCliente(cliente, usuario);
    }

    public void registrarClienteConCuenta(Cliente cliente, Usuario usuario) {

        validarCliente(cliente);
        validarDatosCuenta(usuario);
        validarNombreUsuarioDisponible(usuario);

        Cliente existente = clienteRepository.findByDni(cliente.getDni()).orElse(null);

        prepararCuentaCliente(usuario);

        if (existente != null) {

            if (existente.getUsuario() != null) {
                throw new IllegalStateException(
                        "El cliente ya tiene una cuenta de usuario.");
            }

            vincularCuentaCliente(existente, usuario);

            cliente.setIdCliente(existente.getIdCliente());
            cliente.setUsuario(usuario);
            cliente.setEstado(existente.getEstado());

            return;
        }

        cliente.setEstado(true);
        cliente.setUsuario(usuario);

        usuarioRepository.save(usuario);
        clienteRepository.save(cliente);
    }

    public void registrarClienteAutoservicio(Cliente cliente, Usuario usuario) {

        validarCliente(cliente);

        Cliente existente = clienteRepository.findByDni(cliente.getDni()).orElse(null);

        if (existente != null) {

            boolean correoCoincide = existente.getCorreo() != null
                    && cliente.getCorreo() != null
                    && existente.getCorreo().equalsIgnoreCase(cliente.getCorreo());

            if (existente.getUsuario() != null || !correoCoincide) {
                throw new IllegalStateException(
                        "No fue posible completar el registro con los datos ingresados. "
                        + "Si ya eres cliente del gimnasio, acércate a recepción.");
            }
        }

        registrarClienteConCuenta(cliente, usuario);
    }

    public void actualizarCliente(Cliente cliente) {

        if (cliente == null
                || cliente.getIdCliente() == null
                || cliente.getIdCliente() <= 0) {

            throw new IllegalArgumentException(
                    "El cliente que desea actualizar no es válido.");
        }

        Cliente existente = obtenerClientePorId(cliente.getIdCliente());

        validarCliente(cliente);

        clienteRepository.findByDni(cliente.getDni())
                .filter(conDni -> !conDni.getIdCliente().equals(cliente.getIdCliente()))
                .ifPresent(conDni -> {
                    throw new IllegalArgumentException(
                            "Ya existe otro cliente registrado con ese DNI.");
                });

        if (cliente.getUsuario() == null) {
            cliente.setUsuario(existente.getUsuario());
        }

        if (cliente.getEstado() == null) {
            cliente.setEstado(existente.getEstado());
        }

        clienteRepository.save(cliente);
    }

    public void actualizarDatosContacto(Integer idUsuario,
                                        String telefono,
                                        String correo,
                                        LocalDate fechaNacimiento) {

        Cliente cliente = clienteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tu cuenta no tiene un perfil de cliente asociado."));

        if (telefono != null && !telefono.matches(REGEX_TELEFONO)) {
            throw new IllegalArgumentException(
                    "El teléfono debe tener 9 dígitos y empezar con 9.");
        }

        if (correo != null && !correo.matches(REGEX_CORREO)) {
            throw new IllegalArgumentException("El correo electrónico no es válido.");
        }

        if (fechaNacimiento != null && fechaNacimiento.isAfter(LocalDate.now(clock))) {
            throw new IllegalArgumentException("La fecha de nacimiento no es válida.");
        }

        cliente.setTelefono(telefono);
        cliente.setCorreo(correo);
        cliente.setFechaNacimiento(fechaNacimiento);

        clienteRepository.saveAndFlush(cliente);
    }

    @Transactional(readOnly = true)
    public Cliente obtenerClientePorId(Integer idCliente) {

        validarIdCliente(idCliente);

        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("El cliente no existe."));
    }

    @Transactional(readOnly = true)
    public Cliente obtenerClientePorDni(String dni) {

        validarDni(dni);

        return clienteRepository.findByDni(dni).orElse(null);
    }

    @Transactional(readOnly = true)
    public Cliente obtenerClientePorUsuario(Integer idUsuario) {
        return clienteRepository.findByUsuario_IdUsuario(idUsuario).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Cliente> obtenerTodosLosClientes() {
        return clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Cliente> obtenerClientesActivos() {
        return clienteRepository.findByEstadoTrueOrderByApellidosAscNombresAsc();
    }

    public void desactivarCliente(Integer idCliente) {

        Cliente cliente = obtenerClientePorId(idCliente);
        cliente.setEstado(false);
        clienteRepository.save(cliente);
    }

    public void activarCliente(Integer idCliente) {

        Cliente cliente = obtenerClientePorId(idCliente);
        cliente.setEstado(true);
        clienteRepository.save(cliente);
    }

    private void vincularCuentaCliente(Cliente cliente, Usuario usuario) {

        usuarioRepository.save(usuario);
        cliente.setUsuario(usuario);
        clienteRepository.save(cliente);
    }

    private Cliente obtenerClienteExistentePorDni(String dni) {

        validarDni(dni);

        return clienteRepository.findByDni(dni)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un cliente registrado con ese DNI."));
    }

    private void validarCliente(Cliente cliente) {

        if (cliente == null) {
            throw new IllegalArgumentException("El cliente es obligatorio.");
        }

        validarDni(cliente.getDni());
    }

    private void validarDni(String dni) {

        if (dni == null || dni.isBlank()) {
            throw new IllegalArgumentException("El DNI es obligatorio.");
        }

        if (!dni.matches("\\d{8}")) {
            throw new IllegalArgumentException(
                    "El DNI debe contener exactamente 8 números.");
        }
    }

    private void validarIdCliente(Integer idCliente) {

        if (idCliente == null || idCliente <= 0) {
            throw new IllegalArgumentException("El código del cliente no es válido.");
        }
    }

    private void validarDatosCuenta(Usuario usuario) {

        if (usuario == null) {
            throw new IllegalArgumentException("Los datos de la cuenta son obligatorios.");
        }

        if (usuario.getUsuario() == null || usuario.getUsuario().isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }

        if (usuario.getClave() == null || usuario.getClave().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
    }

    private void validarNombreUsuarioDisponible(Usuario usuario) {

        if (usuarioRepository.existsByUsuario(usuario.getUsuario())) {
            throw new IllegalArgumentException(
                    "El nombre de usuario ya se encuentra registrado.");
        }
    }

    private void prepararCuentaCliente(Usuario usuario) {

        usuario.setRol(Rol.CLIENTE);
        usuario.setEstado(true);
        usuario.setClave(passwordEncoder.encode(usuario.getClave()));
    }
}
