package com.tecnologico.aires.repositories;

import com.tecnologico.aires.models.Usuario;
import com.tecnologico.aires.models.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Utilizado por Spring Security para autenticación por nombre de usuario
    Optional<Usuario> findByUsername(String username);

    // Verificación de duplicados al dar de alta nuevos usuarios
    boolean existsByUsername(String username);

    // Consultar lista de personal según su rol (ej: listar todos los técnicos para asignar tickets)
    List<Usuario> findByRol(Rol rol);

    // Filtrar por usuarios activos/inactivos
    List<Usuario> findByEstadoCuenta(String estadoCuenta);
}