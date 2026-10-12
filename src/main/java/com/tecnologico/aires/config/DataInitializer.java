package com.tecnologico.aires.config;

import com.tecnologico.aires.models.Usuario;
import com.tecnologico.aires.models.enums.Rol;
import com.tecnologico.aires.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Si no existen usuarios en el sistema, creamos el administrador principal
        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // Contraseña cifrada
            admin.setRol(Rol.ADMINISTRADOR);
            admin.setTelefono("0000000000");
            admin.setEstadoCuenta("Activo");
            admin.setFechaRegistro(LocalDate.now());

            usuarioRepository.save(admin);
            System.out.println(">>> USUARIO ADMINISTRADOR CREADO POR DEFECTO: admin / admin123 <<<");
        }
    }
}