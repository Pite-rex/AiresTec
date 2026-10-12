package com.tecnologico.aires.controllers;

import com.tecnologico.aires.models.Edificio;
import com.tecnologico.aires.services.EdificioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/edificios")
public class EdificioController {

    @Autowired
    private EdificioService edificioService;

    @GetMapping
    public String listarEdificios(Model model) {
        model.addAttribute("edificios", edificioService.listarTodos());
        return "edificios/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("edificio", new Edificio());
        return "edificios/formulario";
    }

    @PostMapping("/guardar")
    public String guardarEdificio(@ModelAttribute("edificio") Edificio edificio, RedirectAttributes redirectAttributes) {
        if (edificio.getId() == null && edificioService.existeClave(edificio.getClaveEdificio())) {
            redirectAttributes.addFlashAttribute("error", "La clave del edificio ya se encuentra registrada.");
            return "redirect:/edificios/nuevo";
        }
        edificioService.guardar(edificio);
        redirectAttributes.addFlashAttribute("exito", "Edificio guardado exitosamente.");
        return "redirect:/edificios";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return edificioService.buscarPorId(id)
                .map(edificio -> {
                    model.addAttribute("edificio", edificio);
                    return "edificios/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Edificio no encontrado.");
                    return "redirect:/edificios";
                });
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarEdificio(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            edificioService.eliminar(id);
            redirectAttributes.addFlashAttribute("exito", "Edificio eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar el edificio porque contiene áreas o equipos asociados.");
        }
        return "redirect:/edificios";
    }
}