package com.tecnologico.aires.controllers;

import com.tecnologico.aires.models.Mantenimiento;
import com.tecnologico.aires.models.enums.TipoMantenimiento;
import com.tecnologico.aires.services.EquipoService;
import com.tecnologico.aires.services.MantenimientoService;
import com.tecnologico.aires.services.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.tecnologico.aires.models.Edificio;
import com.tecnologico.aires.services.EdificioService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/mantenimientos")
public class MantenimientoController {

    @Autowired
    private MantenimientoService mantenimientoService;

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private EdificioService edificioService;

    @GetMapping
    public String listar(@RequestParam(value = "equipoId", required = false) Long equipoId,
            @RequestParam(value = "tipo", required = false) TipoMantenimiento tipo,
            Model model) {

        List<Mantenimiento> mantenimientos;
        if (equipoId != null && tipo != null) {
            mantenimientos = mantenimientoService.listarPorEquipoYTipo(equipoId, tipo);
        } else if (equipoId != null) {
            mantenimientos = mantenimientoService.listarPorEquipo(equipoId);
        } else if (tipo != null) {
            mantenimientos = mantenimientoService.listarPorTipo(tipo);
        } else {
            mantenimientos = mantenimientoService.listarTodos();
        }

        List<com.tecnologico.aires.models.Equipo> equipos = listaNoNula(equipoService.listarTodos());
        model.addAttribute("mantenimientos", listaNoNula(mantenimientos));
        model.addAttribute("equipos", equipos);
        model.addAttribute("tipos", TipoMantenimiento.values());
        model.addAttribute("equipoSeleccionado", equipoId);
        model.addAttribute("tipoSeleccionado", tipo);
        BigDecimal costoTotalAcumulado = mantenimientoService.calcularCostoTotalAcumulado();
        model.addAttribute("costoTotalAcumulado", costoTotalAcumulado != null ? costoTotalAcumulado : BigDecimal.ZERO);
        return "mantenimientos/lista";
    }

    // Intervención libre, sin ticket previo (mantenimiento preventivo suelto)
    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(value = "equipoId", required = false) Long equipoId, Model model) {
        cargarCatalogos(model);
        Mantenimiento mantenimiento = new Mantenimiento();
        if (equipoId != null) {
            equipoService.buscarPorId(equipoId).ifPresent(mantenimiento::setEquipo);
        }
        model.addAttribute("mantenimiento", mantenimiento);
        return "mantenimientos/formulario";
    }

    // Cerrar un ticket específico con bitácora (PROC-07)
    @GetMapping("/nuevo/{reporteId}")
    public String nuevoDesdeReporte(@PathVariable Long reporteId, Model model,
            RedirectAttributes redirectAttributes) {
        return reporteService.buscarPorId(reporteId)
                .map(reporte -> {
                    cargarCatalogos(model);
                    Mantenimiento mantenimiento = new Mantenimiento();
                    mantenimiento.setReporte(reporte);
                    mantenimiento.setEquipo(reporte.getEquipo());
                    mantenimiento.setTipoMantenimiento(TipoMantenimiento.CORRECTIVO);
                    model.addAttribute("mantenimiento", mantenimiento);
                    return "mantenimientos/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "El reporte indicado no existe.");
                    return "redirect:/mantenimientos";
                });
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("mantenimiento") Mantenimiento mantenimiento,
            RedirectAttributes redirectAttributes) {
        try {
            mantenimientoService.registrarMantenimiento(mantenimiento);
            redirectAttributes.addFlashAttribute("exito", "Intervención registrada correctamente en la bitácora.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar la intervención: " + e.getMessage());
        }
        return "redirect:/mantenimientos";
    }

    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return mantenimientoService.buscarPorId(id)
                .map(m -> {
                    model.addAttribute("mantenimiento", m);
                    return "mantenimientos/detalle";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Registro de bitácora no encontrado.");
                    return "redirect:/mantenimientos";
                });
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            mantenimientoService.eliminar(id);
            redirectAttributes.addFlashAttribute("exito", "Registro eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el registro.");
        }
        return "redirect:/mantenimientos";
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("equipos",
                listaNoNula(equipoService.listarTodos()).stream().filter(Objects::nonNull).toList());
        model.addAttribute("tipos", TipoMantenimiento.values());
        model.addAttribute("edificios", edificioService.listarTodos());
    }

    private <T> List<T> listaNoNula(List<T> elementos) {
        return elementos != null ? elementos : List.of();
    }
}