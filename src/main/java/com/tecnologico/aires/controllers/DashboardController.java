package com.tecnologico.aires.controllers;

import com.tecnologico.aires.services.EdificioService;
import com.tecnologico.aires.services.EquipoService;
import com.tecnologico.aires.services.MantenimientoService;
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
    private MantenimientoService mantenimientoService;

    @Autowired
    private EdificioService edificioService;

    @GetMapping({"/", "/dashboard"})
    public String mostrarDashboard(Model model) {
        // Métricas ejecutivas para el panel principal
        model.addAttribute("totalEdificios", edificioService.listarTodos().size());
        model.addAttribute("totalEquipos", equipoService.listarTodos().size());
        model.addAttribute("totalReportes", reporteService.listarTodos().size());
        model.addAttribute("gastoTotalMantenimiento", mantenimientoService.calcularGastoTotal());
        model.addAttribute("listadoReportes", reporteService.listarTodos());

        return "dashboard";
    }
}
