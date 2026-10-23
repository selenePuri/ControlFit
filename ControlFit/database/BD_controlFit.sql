
create database controlFit
character set utf8mb4
collate utf8mb4_unicode_ci;

use controlFit;

-- TABLAS
-- Tabla usuario 
create table usuario (
	id_usuario int auto_increment,
    usuario varchar(50) not null,
    clave varchar(255) not null,
    rol varchar(20) not null,
    estado boolean not null default true,
    
    constraint pk_usuario primary key (id_usuario),
    constraint uk_usuario unique (usuario),
    constraint chk_usuario_rol check (rol in
			('ADMINISTRADOR','RECEPCIONISTA','ENTRENADOR','CLIENTE'))
);

-- Tabla empleado
create table empleado (
	id_empleado int auto_increment,
    dni char(8) not null,
    nombres varchar(80) not null,
    apellidos varchar(80) not null,
    telefono varchar(9),
    correo varchar(100),
    fecha_contratacion date not null,
    estado boolean not null default true,
    id_usuario int not null,
    
    constraint pk_empleado primary key (id_empleado),
    constraint uk_empleado_dni unique (dni),
    constraint uk_empleado_correo unique (correo),
    constraint uk_empleado_usuario unique (id_usuario),
    constraint chk_empleado_dni check (dni REGEXP '^[0-9]{8}$'),
    constraint chk_empleado_telefono check (telefono is null or telefono REGEXP 
			'^9[0-9]{8}$'),
    constraint chk_empleado_correo check (correo is null or correo REGEXP
            '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$'),
	constraint fk_empleado_usuario foreign key (id_usuario) references usuario(id_usuario)
    on update cascade
    on delete restrict
);

-- Tabla entrenador
create table entrenador (
	id_entrenador int auto_increment,
    id_empleado int not null,
    especialidad varchar(100),
    certificacion varchar(150),
    anhos_experiencia int,
    
    constraint pk_entrenador primary key (id_entrenador),
    constraint uk_entrenador_empleado unique (id_empleado),
    constraint chk_entrenador_experiencia check (anhos_experiencia is null or
	anhos_experiencia >= 0),
    constraint fk_entrenador_empleado foreign key (id_empleado) references empleado(id_empleado)
    on update cascade
    on delete restrict
);

-- Tabla cliente 
create table cliente (
	id_cliente int auto_increment,
    dni char(8) not null,
    nombres varchar(80) not null,
    apellidos varchar(80) not null,
    telefono varchar(9),
    correo varchar(100),
    fecha_nacimiento date,
    estado boolean not null default true,
    id_usuario int,
    
    constraint pk_cliente primary key (id_cliente),
    constraint uk_cliente_dni unique (dni),
    constraint uk_cliente_correo unique (correo),
    constraint uk_cliente_usuario unique (id_usuario),
	constraint chk_cliente_dni check (dni regexp '^[0-9]{8}$'),
    constraint chk_cliente_telefono check (telefono is null or telefono regexp 
			'^9[0-9]{8}$'),
    constraint chk_cliente_correo check (correo is null or correo regexp  
			'^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$'),
    constraint fk_cliente_usuario foreign key (id_usuario) references usuario(id_usuario) 
    on update cascade
    on delete restrict
);

-- Tabla tipo de membresía 
create table tipo_membresia (
	id_tipo int auto_increment,
    nombre varchar(20) not null,
    descripcion varchar(200),
    duracion_dias int not null,
    precio decimal(10,2) not null,
    estado boolean not null default true,
    
    constraint pk_tipo_membresia primary key (id_tipo),
    constraint uk_tipo_membresia_nombre unique (nombre),
    constraint chk_tipo_membresia_nombre check (nombre in 
			('PASE_DIARIO', 'MENSUAL', 'TRIMESTRAL','SEMESTRAL','ANUAL')),
    constraint chk_tipo_membresia_duracion check (duracion_dias > 0),
    constraint chk_tipo_membresia_precio check (precio > 0)
);

USE controlfit;

-- Tabla devolucion 
CREATE TABLE IF NOT EXISTS devolucion (
	id_devolucion int auto_increment,
    id_pago int not null,
    fecha_devolucion datetime not null,
    monto decimal(10,2) not null,
    motivo varchar(200),
    estado varchar(20) not null default 'COMPLETADA',

    constraint pk_devolucion primary key (id_devolucion),
    constraint chk_devolucion_monto check (monto > 0),
    constraint chk_devolucion_estado check (estado in
			('PENDIENTE','COMPLETADA','ANULADA')),
    constraint fk_devolucion_pago foreign key (id_pago) references pago(id_pago)
    on update cascade
    on delete restrict
);

-- Tabla promocion
create table promocion (
	id_promocion int auto_increment,
    nombre varchar(80) not null,
    descripcion varchar(200),
    tipo_descuento varchar(20) not null,
    valor_descuento decimal(10,2) not null,
    fecha_inicio date not null,
    fecha_fin date not null,
    estado boolean not null default true,
    
    constraint pk_promocion primary key (id_promocion),
    constraint chk_promocion_tipo check (tipo_descuento in 
			('PORCENTAJE','MONTO_FIJO')),
    constraint chk_promocion_valor check (valor_descuento > 0),
    constraint chk_promocion_porcentaje check (tipo_descuento <> 
			'PORCENTAJE' or valor_descuento <= 100),
    constraint chk_promocion_fechas check (fecha_fin >= fecha_inicio)
);

-- Tabla intermedia (promocion - tipo de membresia)
create table promocion_tipo_membresia (
	id_promocion int not null,
    id_tipo int not null,
    
    constraint pk_promocion_tipo primary key (id_promocion, id_tipo),
    constraint fk_promocion_tipo_promocion foreign key (id_promocion)
    references promocion(id_promocion) 
    on update cascade
    on delete restrict,
    constraint fk_promocion_tipo_membresia foreign key (id_tipo) 
    references tipo_membresia(id_tipo)
    on update cascade
    on delete restrict
);

-- Tabla membresia
create table membresia (
	id_membresia int auto_increment,
    id_cliente int not null,
    id_tipo int not null,
    id_promocion int,
    fecha_inicio date not null,
    fecha_fin date not null,
    precio_base decimal(10,2) not null,
    descuento decimal(10,2) not null default 0,
    precio_final decimal(10,2) not null,
    estado varchar(20) not null default 'ACTIVA',
    
    constraint pk_membresia primary key (id_membresia),
    constraint chk_membresia_fechas check (fecha_fin >= fecha_inicio),
    constraint chk_membresia_precio_base check (precio_base >= 0),
    constraint chk_membresia_descuento check (descuento >= 0),
    constraint chk_membresia_precio_final check (precio_final >= 0),
    constraint chk_membresia_estado check (estado in 
			('PENDIENTE','ACTIVA','VENCIDA','CANCELADA')),
    constraint fk_membresia_cliente foreign key (id_cliente) references cliente(id_cliente)
    on update cascade
    on delete restrict,
    constraint fk_membresia_tipo foreign key (id_tipo) references tipo_membresia(id_tipo)
    on update cascade
    on delete restrict,
    constraint fk_membresia_promocion foreign key (id_promocion) references promocion(id_promocion)
    on update cascade
    on delete restrict
);

-- Tabla pago
create table pago (
	id_pago int auto_increment,
    id_membresia int not null,
    fecha_pago datetime not null,
    monto decimal(10,2) not null,
    metodo_pago varchar(20) not null,
    estado varchar(20) not null default 'PAGADO',
    
    constraint pk_pago primary key (id_pago),
    constraint chk_pago_monto check (monto > 0),
    constraint chk_pago_metodo check (metodo_pago in 
			('EFECTIVO','TARJETA','YAPE','PLIN','TRANSFERENCIA')),
    constraint chk_pago_estado check (estado in 
			('PENDIENTE','PAGADO','ANULADO')),
    constraint fk_pago_membresia foreign key (id_membresia) references membresia(id_membresia)
    on update cascade
    on delete restrict
);

-- Tabla asistencia
create table asistencia (
	id_asistencia int auto_increment,
    id_cliente int not null,
    fecha date not null,
    hora_entrada time not null,
    hora_salida time,
    
    constraint pk_asistencia primary key (id_asistencia),
    constraint chk_asistencia_horas check (hora_salida is null or hora_salida >= hora_entrada),
    constraint fk_asistencia_cliente foreign key (id_cliente) references cliente(id_cliente)
    on update cascade
    on delete restrict
);

-- Tabla rutina
create table rutina (
	id_rutina int auto_increment,
    id_entrenador int not null,
    nombre varchar(80) not null,
    descripcion varchar(255),
    objetivo varchar(100),
    fecha_creacion date not null,
    estado boolean not null default true,
    
    constraint pk_rutina primary key (id_rutina),
    constraint fk_rutina_entrenador foreign key (id_entrenador) references entrenador(id_entrenador)
    on update cascade
    on delete restrict
);

-- Tabla rutina cliente
create table rutina_cliente (
	id_rutina_cliente int auto_increment,
    id_rutina int not null,
    id_cliente int not null,
    fecha_asignacion date not null,
    fecha_fin date,
    estado boolean not null default true,
    
    constraint pk_rutina_cliente primary key (id_rutina_cliente),
    constraint chk_rutina_cliente_fechas check (fecha_fin is null or fecha_fin >= fecha_asignacion),
    constraint fk_rutina_cliente_rutina foreign key (id_rutina) references rutina(id_rutina)
    on update cascade
    on delete restrict,
    constraint fk_rutina_cliente_cliente foreign key (id_cliente) references cliente(id_cliente)
    on update cascade
    on delete restrict
);

-- INSERT 
-- Tipos de membresias 
INSERT INTO tipo_membresia (nombre, descripcion, duracion_dias, precio)
VALUES
('PASE_DIARIO', 'Acceso al gimnasio por un día', 1, 15.00),
('MENSUAL', 'Acceso al gimnasio por 30 días', 30, 100.00),
('TRIMESTRAL', 'Acceso al gimnasio por 3 meses', 90, 270.00),
('SEMESTRAL', 'Acceso al gimnasio por 6 meses', 180, 500.00),
('ANUAL', 'Acceso al gimnasio por un año', 365, 900.00);

   
-- CONSULTAS
show tables;

SELECT * FROM usuario;
SELECT * FROM empleado;
SELECT * FROM devolucion;
SELECT * FROM entrenador;
SELECT * FROM cliente;
SELECT * FROM tipo_membresia;
SELECT * FROM promocion;
SELECT * FROM promocion_tipo_membresia;
SELECT * FROM membresia;
SELECT * FROM pago;
SELECT * FROM asistencia;
SELECT * FROM rutina;
SELECT * FROM rutina_cliente;

SELECT VERSION();


