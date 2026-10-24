# 🏋️ ControlFit — Sistema Web de Gestión de Gimnasio

Sistema web desarrollado para facilitar la gestión administrativa de un gimnasio mediante la administración de clientes, 
empleados, membresías, pagos, promociones y usuarios.

ControlFit fue desarrollado como proyecto académico de la carrera de Computación e Informática en Cibertec, aplicando 
tecnologías Java y el framework Spring Boot para construir una aplicación web organizada por capas, con persistencia de 
datos y control de acceso según roles.

## 📌 Características principales

- **Gestión de usuarios:** administración de cuentas y roles de acceso.
- **Gestión de empleados:** registro y administración de la información del personal.
- **Gestión de clientes:** registro y consulta de información de los clientes.
- **Gestión de membresías:** administración de membresías y consulta de su vigencia.
- **Gestión de pagos:** registro y consulta de pagos asociados a las membresías.
- **Gestión de promociones:** administración de promociones disponibles.
- **Control de acceso:** autenticación y autorización según el rol del usuario.
- **Portal del cliente:** consulta de la membresía propia, historial de pagos y promociones disponibles.
- **Reportes PDF:** generación de documentos PDF con información administrativa del sistema.

## 🛠️ Tecnologías utilizadas

| Tecnología | Utilidad |
|---|---|
| Java 21 | Lenguaje de programación |
| Spring Boot 3.5.11 | Configuración y estructura de la aplicación |
| Spring MVC | Gestión de solicitudes web y controladores |
| Thymeleaf | Construcción de vistas HTML dinámicas |
| Spring Security | Autenticación y autorización |
| Spring Data JPA | Acceso y persistencia de datos |
| Hibernate | Mapeo objeto-relacional |
| MySQL | Sistema de gestión de base de datos |
| Maven | Gestión de dependencias y construcción |
| Jakarta Bean Validation | Validación de datos |
| Lombok | Reducción de código repetitivo |
| OpenPDF | Generación de documentos PDF |
| HTML, CSS y JavaScript | Estructura, estilos e interacción de las interfaces |

## 👥 Roles del sistema

- **Administrador:** gestiona usuarios, empleados, clientes, membresías, pagos y promociones, de acuerdo con los permisos
implementados.
- **Recepcionista:** realiza operaciones relacionadas con clientes, membresías y pagos, según las funcionalidades habilitadas.
- **Cliente:** consulta su propia membresía, su historial de pagos y las promociones disponibles.

## 🏗️ Arquitectura del proyecto

La aplicación sigue una organización por capas para separar las responsabilidades y facilitar el mantenimiento del código.

- `controller`: gestiona las solicitudes recibidas desde las interfaces web.
- `service`: contiene la lógica de negocio.
- `repository`: administra el acceso a los datos mediante Spring Data JPA.
- `model`: contiene las entidades y enumeraciones del sistema.
- `dto`: agrupa los objetos utilizados para transferir y validar datos.
- `config`: contiene la configuración de la aplicación y la seguridad.
- `security`: reúne componentes relacionados con la autenticación y autorización.
- `templates`: contiene las vistas HTML desarrolladas con Thymeleaf.
- `static`: almacena los recursos estáticos, como CSS y JavaScript.

## ⚙️ Requisitos previos

Antes de ejecutar el proyecto, se requiere:

- JDK 21.
- Apache Maven 3.9 o compatible.
- MySQL Server.
- Un entorno de desarrollo compatible con Java, como IntelliJ IDEA, Eclipse o Visual Studio Code.

## 🚀 Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/selenePuri/ControlFit.git
```

### 2. Ingresar al directorio del proyecto

```bash
cd ControlFit/ControlFit
```

### 3. Configurar la base de datos

Crear la base de datos `controlFit` en MySQL utilizando el script SQL disponible en el proyecto, ubicado en:

```text
database/BD_controlFit.sql
```

Verificar la configuración de conexión en `src/main/resources/application.properties`, incluyendo la URL JDBC, el usuario y 
la contraseña de MySQL.

### 4. Ejecutar la aplicación

Si se dispone de Maven instalado:

```bash
mvn spring-boot:run
```

También se pueden ejecutar las pruebas automatizadas mediante:

```bash
mvn test
```

### 5. Acceder a la aplicación

Una vez iniciada correctamente, abrir en el navegador:

```text
http://localhost:8080
```

La disponibilidad de las funciones dependerá de la configuración de la aplicación y de los permisos asignados a cada usuario.

## 🌿 Control de versiones

El desarrollo se organizó mediante Git y GitHub, utilizando ramas para distribuir el trabajo entre los integrantes e 
integrar los avances del equipo.

Ramas de trabajo:

- `develop`: integración de las funcionalidades del proyecto.
- `selene-mayorga`: seguridad, configuración, DTO e integración de reportes PDF.
- `jeremy-bustes`: controladores y repositorios.
- `giovanne-lozano`: entidades, persistencia y servicios de negocio.
- `jose-campos`: interfaces HTML, estilos CSS y funcionalidades JavaScript.

## 👩‍💻 Equipo de desarrollo

Proyecto académico desarrollado en equipo para la carrera de Computación e Informática de Cibertec.

- Jeremy Alfredo Bustes Vasquez
- Selene Anggi Mayorga Purizaca
- Giovanne Aldahir Lozano Barturen
- Jose Antonio Campos Silva

## 📚 Propósito académico

Este proyecto permitió aplicar conocimientos de programación Java, desarrollo web con Spring Boot, persistencia con 
JPA e Hibernate, gestión de bases de datos MySQL, seguridad web, control de versiones y trabajo colaborativo.

---

**ControlFit** — Sistema web de gestión de gimnasio.

Proyecto académico de Cibertec.
