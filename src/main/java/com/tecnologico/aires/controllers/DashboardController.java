package com.tecnologico.aires.controllers;

import com.tecnologico.aires.services.EdificioService;
import com.tecnologico.aires.services.EquipoService;
import com.tecnologico.aires.services.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private EdificioService edificioService;

    @GetMapping({"/", "/dashboard"})
    public String mostrarDashboard(Model model) {
        // Métricas ejecutivas para el panel principal
        model.addAttribute("totalEdificios", edificioService.listarTodos().size());
        model.addAttribute("totalEquipos", equipoService.listarTodos().size());
        var activos = reporteService.buscar("ACTIVOS", null).stream()
        .sorted(java.util.Comparator.comparing(
                        com.tecnologico.aires.models.Reporte::getPrioridad,
                        java.util.Comparator.reverseOrder())
                .thenComparing(com.tecnologico.aires.models.Reporte::getFechaReporte))
        .toList();
        model.addAttribute("totalReportes", activos.size());
        model.addAttribute("listadoReportes", activos);

        return "dashboard";
    }
}
