package com.cibertec.controlfit.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cibertec.controlfit.model.Usuario;
import com.cibertec.controlfit.model.enums.Rol;
//Repositorios

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByUsuario(String usuario);

    boolean existsByUsuario(String usuario);

    List<Usuario> findByEstadoTrueAndRolInOrderByUsuarioAsc(Collection<Rol> roles);
}
