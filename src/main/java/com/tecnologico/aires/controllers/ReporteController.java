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

import java.time.Duration;
import java.util.Optional;
import com.tecnologico.aires.models.enums.EstadoOperativo;
import com.tecnologico.aires.services.ReporteEnEsperaException;

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
    public String listarReportes(@RequestParam(value = "estado", defaultValue = "ACTIVOS") String estado,
            @RequestParam(value = "prioridad", required = false) Prioridad prioridad,
            Model model) {
        model.addAttribute("reportes", reporteService.buscar(estado, prioridad));
        model.addAttribute("prioridades", Prioridad.values());
        model.addAttribute("estadoSeleccionado", estado.toUpperCase());
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

    // Formulario público (QR): sin login
    @GetMapping("/levantar/{equipoId}")
    public String levantarReportePublico(@PathVariable Long equipoId, Model model) {
        Optional<Equipo> encontrado = equipoService.buscarPorId(equipoId);
        if (encontrado.isEmpty()) {
            return avisoPublico(model, "bi-question-circle", "Equipo no encontrado",
                    "El código QR escaneado no corresponde a ningún equipo registrado. "
                            + "Puede acudir al departamento de mantenimiento y equipo.");
        }
        Equipo equipo = encontrado.get();
        if (equipo.getEstadoOperativo() == EstadoOperativo.BAJA) {
            return avisoPublico(model, "bi-slash-circle", "Equipo dado de baja",
                    "Este equipo ya no está en servicio, por lo que no admite reportes.");
        }
        Duration espera = reporteService.esperaParaReportar(equipo);
        if (!espera.isZero()) {
            return avisoEspera(model, espera);
        }

        Reporte reporte = new Reporte();
        reporte.setEquipo(equipo);
        reporte.setPrioridad(Prioridad.MEDIA);
        model.addAttribute("reporte", reporte);
        model.addAttribute("equipoFijo", equipo);
        model.addAttribute("prioridades", Prioridad.values());
        return "reportes/formulario_publico";
    }

    @PostMapping("/publico/guardar")
    public String guardarReportePublico(
            @RequestParam(value = "equipo.id", required = false) Long equipoId,
            @RequestParam(value = "nombreSolicitante", required = false) String nombre,
            @RequestParam(value = "correoSolicitante", required = false) String correo,
            @RequestParam(value = "telefonoSolicitante", required = false) String telefono,
            @RequestParam(value = "prioridad", required = false) Prioridad prioridad,
            @RequestParam(value = "descripcionFalla", required = false) String descripcion,
            Model model) {
        try {
            ReporteService.ResultadoPublico resultado = reporteService.registrarReportePublico(
                    equipoId, nombre, correo, telefono, prioridad, descripcion);
            return "redirect:/reportes/confirmacion/" + resultado.reporte().getId()
                    + (resultado.actualizado() ? "?actualizado=true" : "");
        } catch (ReporteEnEsperaException e) {
            return avisoEspera(model, e.getEspera());
        } catch (IllegalArgumentException e) {
            return avisoPublico(model, "bi-exclamation-triangle", "No se pudo registrar el reporte", e.getMessage());
        } catch (RuntimeException e) {
            return avisoPublico(model, "bi-exclamation-triangle", "No se pudo registrar el reporte",
                    "Ocurrió un error inesperado. Inténtelo de nuevo o acuda al departamento de mantenimiento y equipo.");
        }
    }

    @GetMapping("/confirmacion/{id}")
    public String confirmacionReporte(@PathVariable Long id, Model model) {
        return reporteService.buscarPorId(id)
                .map(reporte -> {
                    model.addAttribute("reporte", reporte);
                    return "reportes/confirmacion";
                })
                .orElseGet(() -> avisoPublico(model, "bi-question-circle", "Reporte no encontrado",
                        "No encontramos ese reporte. Puede acudir al departamento de mantenimiento y equipo."));
    }

    private String avisoPublico(Model model, String icono, String titulo, String mensaje) {
        model.addAttribute("icono", icono);
        model.addAttribute("titulo", titulo);
        model.addAttribute("mensaje", mensaje);
        return "reportes/aviso_publico";
    }

    private String avisoEspera(Model model, Duration espera) {
        return avisoPublico(model, "bi-hourglass-split", "Reporte ya emitido",
                "Ya se ha emitido el reporte de este equipo. Por favor intente después de "
                        + formatearEspera(espera)
                        + " para volver a emitir el reporte, o puede acudir al departamento de mantenimiento y equipo.");
    }

    private String formatearEspera(Duration espera) {
        long minutos = Math.max(1, (espera.getSeconds() + 59) / 60);
        long horas = minutos / 60;
        long resto = minutos % 60;
        String h = horas + (horas == 1 ? " hora" : " horas");
        String m = resto + (resto == 1 ? " minuto" : " minutos");
        if (horas == 0)
            return m;
        if (resto == 0)
            return h;
        return h + " con " + m;
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