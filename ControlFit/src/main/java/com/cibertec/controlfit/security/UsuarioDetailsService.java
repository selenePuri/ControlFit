package com.cibertec.controlfit.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cibertec.controlfit.model.Usuario;
import com.cibertec.controlfit.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
/*Seguridad*/
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService, UserDetailsPasswordService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        String nombre = username == null ? "" : username.trim();

        Usuario usuario = usuarioRepository.findByUsuario(nombre)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return new UsuarioDetails(usuario);
    }

    @Override
    @Transactional
    public UserDetails updatePassword(UserDetails user, String nuevaClaveCodificada) {

        Usuario usuario = usuarioRepository.findByUsuario(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        usuario.setClave(nuevaClaveCodificada);
        usuarioRepository.save(usuario);

        return new UsuarioDetails(usuario);
    }
}
