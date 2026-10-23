package com.cibertec.controlfit.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cibertec.controlfit.model.enums.Rol;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor

public class Usuario {
	
	 @Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	 @Column(name = "id_usuario")
	 private Integer idUsuario;

	 @NotBlank(message = "El nombre de usuario es obligatorio")
	 @Size(max = 50, message = "El usuario no puede superar los 50 caracteres")
	 @Column(name = "usuario", nullable = false, unique = true, length = 50)
	 private String usuario;

	 @NotBlank(message = "La contraseña es obligatoria")
	 @Size(max = 255, message = "La contraseña no puede superar los 255 caracteres")
	 @Column(name = "clave", nullable = false, length = 255)
	 private String clave;

	 @NotNull(message = "El rol es obligatorio")
	 @Enumerated(EnumType.STRING)
	 @Column(name = "rol", nullable = false, length = 20)
	 private Rol rol;

	 @NotNull
	 @Column(name = "estado", nullable = false)
	 private Boolean estado = true;

}
