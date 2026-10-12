package com.tecnologico.aires.controllers;

import com.tecnologico.aires.models.Usuario;
import com.tecnologico.aires.models.enums.Rol;
import com.tecnologico.aires.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        Usuario usuario = new Usuario();
        usuario.setEstadoCuenta("Activo"); 
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", Rol.values());
        model.addAttribute("esEdicion", false);
        return "usuarios/formulario";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute("usuario") Usuario usuario,
                                 RedirectAttributes redirectAttributes) {
        
        if (usuario.getId() == null || (usuario.getPassword() != null && !usuario.getPassword().isEmpty())) {
            String pass = usuario.getPassword();
            if (pass == null || !pass.matches("^(?=.*[A-Z])(?=.*\\d).{8,20}$")) {
                redirectAttributes.addFlashAttribute("error", "La contraseña no cumple con los estándares de seguridad.");
                return "redirect:/usuarios/" + (usuario.getId() == null ? "nuevo" : "editar/" + usuario.getId());
            }
        }

        if (usuario.getId() == null) {
            if (usuarioService.existeUsername(usuario.getUsername())) {
                redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya se encuentra en uso.");
                return "redirect:/usuarios/nuevo";
            }
            
            usuario.setEstadoCuenta("Activo");
            usuario.setFechaRegistro(LocalDate.now());
            
            usuarioService.registrarNuevo(usuario);
            redirectAttributes.addFlashAttribute("exito", "Usuario registrado exitosamente.");
        } 
        else {
            Optional<Usuario> optExistente = usuarioService.buscarPorId(usuario.getId());
            if (optExistente.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El usuario a actualizar no existe.");
                return "redirect:/usuarios";
            }
            
            Usuario existente = optExistente.get();
            usuario.setEstadoCuenta(existente.getEstadoCuenta());
            
            usuarioService.actualizar(existente, usuario);
            redirectAttributes.addFlashAttribute("exito", "Información del usuario actualizada correctamente.");
        }

        return "redirect:/usuarios";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return usuarioService.buscarPorId(id)
                .map(usuario -> {
                    usuario.setPassword("");
                    model.addAttribute("usuario", usuario);
                    model.addAttribute("roles", Rol.values());
                    model.addAttribute("esEdicion", true);
                    return "usuarios/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
                    return "redirect:/usuarios";
                });
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, 
                                  Authentication authentication, 
                                  RedirectAttributes redirectAttributes) {
        Optional<Usuario> optUsuario = usuarioService.buscarPorId(id);
        
        if (optUsuario.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El usuario no existe.");
            return "redirect:/usuarios";
        }

        if (optUsuario.get().getUsername().equalsIgnoreCase(authentication.getName())) {
            redirectAttributes.addFlashAttribute("error", "No puedes eliminar tu propia cuenta de usuario en sesión activa.");
            return "redirect:/usuarios";
        }

        try {
            usuarioService.eliminar(id);
            redirectAttributes.addFlashAttribute("exito", "Usuario eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar el usuario porque tiene tickets o bitácoras asociadas.");
        }

        return "redirect:/usuarios";
    }
}