package com.cibertec.controlfit.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.cibertec.controlfit.model.Usuario;
import com.cibertec.controlfit.model.enums.Rol;
/*Seguridad*/

public class UsuarioDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Integer idUsuario;
    private final String username;
    private final String password;
    private final boolean activo;
    private final Rol rol;

    public UsuarioDetails(Usuario usuario) {
        this.idUsuario = usuario.getIdUsuario();
        this.username = usuario.getUsuario();
        this.password = usuario.getClave();
        this.activo = Boolean.TRUE.equals(usuario.getEstado());
        this.rol = usuario.getRol();
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public Rol getRol() {
        return rol;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof UsuarioDetails)) {
            return false;
        }
        return username.equals(((UsuarioDetails) otro).username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}
