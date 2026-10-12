package com.tecnologico.aires.controllers;

import com.tecnologico.aires.models.Equipo;
import com.tecnologico.aires.models.Reporte;
import com.tecnologico.aires.models.enums.EstadoReporte;
import com.tecnologico.aires.models.enums.Prioridad;
import com.tecnologico.aires.services.EquipoService;
import com.tecnologico.aires.services.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import java.util.Objects;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.tecnologico.aires.models.Edificio;
import com.tecnologico.aires.services.EdificioService;
import java.security.Principal;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private EdificioService edificioService;

    // Mesa de ayuda y listado de reportes
    @GetMapping
    public String listarReportes(@RequestParam(value = "estado", required = false) EstadoReporte estado,
            @RequestParam(value = "prioridad", required = false) Prioridad prioridad,
            Model model) {
        List<Reporte> reportes;
        if (estado != null) {
            reportes = reporteService.listarPorEstado(estado);
        } else if (prioridad != null) {
            reportes = reporteService.listarPorPrioridad(prioridad);
        } else {
            reportes = reporteService.listarTodos().stream()
                    .filter(r -> r.getEstadoReporte() != EstadoReporte.ATENDIDO)
                    .toList();
        }

        model.addAttribute("reportes", reportes);
        model.addAttribute("estados", EstadoReporte.values());
        model.addAttribute("prioridades", Prioridad.values());
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("prioridadSeleccionada", prioridad);
        return "reportes/lista";
    }

    // Formulario para levantar nuevo reporte interno
    @GetMapping("/nuevo")
    public String nuevoReporte(@RequestParam(value = "equipoId", required = false) Long equipoId,
            Model model, Principal principal) {

        List<Equipo> equiposLimpios = equipoService.listarTodos()
                .stream()
                .filter(Objects::nonNull)
                .toList();

        List<Edificio> edificios = edificioService.listarTodos();

        Reporte reporte = new Reporte();
        if (equipoId != null) {
            equipoService.buscarPorId(equipoId).ifPresent(reporte::setEquipo);
        }

        model.addAttribute("equipos", equiposLimpios);
        model.addAttribute("edificios", edificios);
        model.addAttribute("reporte", reporte);
        model.addAttribute("equipoSeleccionadoId", equipoId);
        model.addAttribute("nombreUsuarioActual", principal != null ? principal.getName() : "");

        return "reportes/formulario";
    }

    // Formulario público para registrar reporte vía QR desde celular
    @GetMapping("/levantar/{equipoId}")
    public String levantarReportePublico(@PathVariable Long equipoId, Model model,
            RedirectAttributes redirectAttributes) {
        return equipoService.buscarPorId(equipoId)
                .map(equipo -> {
                    Reporte reporte = new Reporte();
                    reporte.setEquipo(equipo);
                    reporte.setPrioridad(Prioridad.MEDIA);
                    model.addAttribute("reporte", reporte);
                    model.addAttribute("equipoFijo", equipo);
                    model.addAttribute("prioridades", Prioridad.values());
                    return "reportes/formulario_publico";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "El equipo escaneado no existe.");
                    return "redirect:/reportes";
                });
    }

    // Guardar reporte
    @PostMapping("/guardar")
    public String guardarReporte(@ModelAttribute("reporte") Reporte reporte,
            @RequestParam(value = "esPublico", defaultValue = "false") boolean esPublico,
            RedirectAttributes redirectAttributes) {
        try {
            Reporte guardado = reporteService.registrarReporte(reporte);
            if (esPublico) {
                return "redirect:/reportes/confirmacion/" + guardado.getId();
            }
            redirectAttributes.addFlashAttribute("exito",
                    "Reporte " + guardado.getFolioReporte() + " registrado con éxito.");
            return "redirect:/reportes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar reporte: " + e.getMessage());
            return "redirect:/reportes/nuevo";
        }
    }

    // Confirmación pública
    @GetMapping("/confirmacion/{id}")
    public String confirmacionReporte(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return reporteService.buscarPorId(id)
                .map(reporte -> {
                    model.addAttribute("reporte", reporte);
                    return "reportes/confirmacion";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Reporte no encontrado.");
                    return "redirect:/reportes";
                });
    }

    // Detalle de reporte
    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return reporteService.buscarPorId(id)
                .map(reporte -> {
                    model.addAttribute("reporte", reporte);
                    model.addAttribute("estados", EstadoReporte.values());
                    return "reportes/detalle";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Reporte no encontrado.");
                    return "redirect:/reportes";
                });
    }

    // Cambiar estado
    @PostMapping("/cambiar-estado")
    public String cambiarEstado(@RequestParam("id") Long id,
            @RequestParam("nuevoEstado") EstadoReporte nuevoEstado,
            RedirectAttributes redirectAttributes) {
        try {
            reporteService.cambiarEstado(id, nuevoEstado);
            redirectAttributes.addFlashAttribute("exito", "Estado del reporte actualizado a " + nuevoEstado);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo actualizar el estado: " + e.getMessage());
        }
        return "redirect:/reportes/detalle/" + id;
    }

    // Eliminar reporte
    @GetMapping("/eliminar/{id}")
    public String eliminarReporte(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reporteService.eliminar(id);
            redirectAttributes.addFlashAttribute("exito", "Reporte eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el reporte.");
        }
        return "redirect:/reportes";
    }
}