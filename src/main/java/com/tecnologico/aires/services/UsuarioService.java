package com.tecnologico.aires.services;

import com.tecnologico.aires.models.Usuario;
import com.tecnologico.aires.models.enums.Rol;
import com.tecnologico.aires.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> listarPorRol(Rol rol) {
        return usuarioRepository.findByRol(rol);
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public boolean existeUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    public Usuario registrarNuevo(Usuario usuario) {
        // Encriptar la contraseña con BCrypt antes de persistir (RNF11)
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizar(Usuario usuarioExistente, Usuario datosNuevos) {
        if (datosNuevos.getNombreCompleto() != null && !datosNuevos.getNombreCompleto().trim().isEmpty()) {
            usuarioExistente.setNombreCompleto(datosNuevos.getNombreCompleto().trim());
        }

        if (datosNuevos.getRol() != null) {
            usuarioExistente.setRol(datosNuevos.getRol());
        }

        if (datosNuevos.getTelefono() != null) {
            usuarioExistente.setTelefono(datosNuevos.getTelefono());
        }

        if (datosNuevos.getEstadoCuenta() != null && !datosNuevos.getEstadoCuenta().trim().isEmpty()) {
            usuarioExistente.setEstadoCuenta(datosNuevos.getEstadoCuenta());
        }

        // Si se ingresó una nueva contraseña, se encripta; si viene vacía, se conserva la actual
        if (datosNuevos.getPassword() != null && !datosNuevos.getPassword().trim().isEmpty()) {
            usuarioExistente.setPassword(passwordEncoder.encode(datosNuevos.getPassword()));
        }

        return usuarioRepository.save(usuarioExistente);
    }

    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }
}