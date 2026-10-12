package com.tecnologico.aires.controllers;

import com.tecnologico.aires.models.Espacio;
import com.tecnologico.aires.models.enums.TipoEspacio;
import com.tecnologico.aires.services.EdificioService;
import com.tecnologico.aires.services.EspacioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/espacios")
public class EspacioController {

    @Autowired
    private EspacioService espacioService;

    @Autowired
    private EdificioService edificioService;

    @GetMapping
    public String listarEspacios(@RequestParam(value = "edificioId", required = false) Long edificioId, Model model) {
        if (edificioId != null) {
            model.addAttribute("espacios", espacioService.listarPorEdificio(edificioId));
        } else {
            model.addAttribute("espacios", espacioService.listarTodos());
        }
        model.addAttribute("edificios", edificioService.listarTodos());
        model.addAttribute("edificioSeleccionado", edificioId);
        return "espacios/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("espacio", new Espacio());
        model.addAttribute("edificios", edificioService.listarTodos());
        model.addAttribute("tiposEspacio", TipoEspacio.values());
        return "espacios/formulario";
    }

    @PostMapping("/guardar")
    public String guardarEspacio(@ModelAttribute("espacio") Espacio espacio, RedirectAttributes redirectAttributes) {
        if (espacio.getId() == null && espacioService.existeClave(espacio.getClaveEspacio())) {
            redirectAttributes.addFlashAttribute("error", "La clave del espacio ya se encuentra registrada.");
            return "redirect:/espacios/nuevo";
        }
        espacioService.guardar(espacio);
        redirectAttributes.addFlashAttribute("exito", "Espacio guardado exitosamente.");
        return "redirect:/espacios";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return espacioService.buscarPorId(id)
                .map(espacio -> {
                    model.addAttribute("espacio", espacio);
                    model.addAttribute("edificios", edificioService.listarTodos());
                    model.addAttribute("tiposEspacio", TipoEspacio.values());
                    return "espacios/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Espacio no encontrado.");
                    return "redirect:/espacios";
                });
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarEspacio(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            espacioService.eliminar(id);
            redirectAttributes.addFlashAttribute("exito", "Espacio eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar el espacio porque tiene equipos asociados.");
        }
        return "redirect:/espacios";
    }
}