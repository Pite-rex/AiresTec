package com.tecnologico.aires.controllers;

import com.tecnologico.aires.models.Equipo;
import com.tecnologico.aires.models.enums.EstadoOperativo;
import com.tecnologico.aires.services.EdificioService;
import com.tecnologico.aires.services.EquipoService;
import com.tecnologico.aires.services.EspacioService;
import com.tecnologico.aires.util.QRCodeGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/equipos")
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private EspacioService espacioService;

    @Autowired
    private EdificioService edificioService;

    @GetMapping
    public String listarEquipos(@RequestParam(value = "edificioId", required = false) Long edificioId,
                                @RequestParam(value = "estado", required = false) EstadoOperativo estado,
                                Model model) {
        List<Equipo> equipos;

        if (edificioId != null) {
            equipos = equipoService.listarPorEdificio(edificioId);
        } else if (estado != null) {
            equipos = equipoService.listarPorEstado(estado);
        } else {
            equipos = equipoService.listarTodos();
        }

        model.addAttribute("equipos", equipos);
        model.addAttribute("edificios", edificioService.listarTodos());
        model.addAttribute("estados", EstadoOperativo.values());
        model.addAttribute("edificioSeleccionado", edificioId);
        model.addAttribute("estadoSeleccionado", estado);
        return "equipos/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        Equipo equipo = new Equipo();
        equipo.setEstadoOperativo(EstadoOperativo.OPERATIVO);
        
        model.addAttribute("equipo", equipo);
        model.addAttribute("espacios", espacioService.listarTodos());
        model.addAttribute("estados", EstadoOperativo.values());
        model.addAttribute("edificios", edificioService.listarTodos());   
        model.addAttribute("esEdicion", false);
        return "equipos/formulario";
    }

    @PostMapping("/guardar")
    public String guardarEquipo(@ModelAttribute("equipo") Equipo equipo,
                                RedirectAttributes redirectAttributes) {

        // Validaciones para nuevo equipo (PROC-03 / RF11)
        if (equipo.getId() == null) {
            if (equipoService.existeCodigo(equipo.getCodigoEquipo())) {
                redirectAttributes.addFlashAttribute("error", "El código institucional del equipo ya está registrado.");
                return "redirect:/equipos/nuevo";
            }
            if (equipoService.existeNumeroSerie(equipo.getNumeroSerie())) {
                redirectAttributes.addFlashAttribute("error", "El número de serie ingresado ya se encuentra registrado.");
                return "redirect:/equipos/nuevo";
            }
        }

        equipoService.guardar(equipo);
        redirectAttributes.addFlashAttribute("exito", "Equipo registrado y asignado correctamente.");
        return "redirect:/equipos";
    }

    @GetMapping("/detalle/{id}")
    public String verFichaTecnica(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return equipoService.buscarPorId(id)
                .map(equipo -> {
                    model.addAttribute("equipo", equipo);
                    return "equipos/detalle";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Equipo no encontrado.");
                    return "redirect:/equipos";
                });
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return equipoService.buscarPorId(id)
                .map(equipo -> {
                    model.addAttribute("equipo", equipo);
                    model.addAttribute("espacios", espacioService.listarTodos());
                    model.addAttribute("estados", EstadoOperativo.values());
                    model.addAttribute("edificios", edificioService.listarTodos()); 
                    model.addAttribute("esEdicion", true);
                    return "equipos/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Equipo no encontrado.");
                    return "redirect:/equipos";
                });
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarEquipo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            equipoService.eliminar(id);
            redirectAttributes.addFlashAttribute("exito", "Equipo eliminado correctamente del inventario.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar el equipo porque cuenta con reportes o bitácoras en su historial.");
        }
        return "redirect:/equipos";
    }

    // Endpoint para servir la imagen PNG del código QR (PROC-04 / RF14)
    @GetMapping(value = "/qr/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> generarCodigoQr(@PathVariable Long id, HttpServletRequest request) {
        return equipoService.buscarPorId(id)
                .map(equipo -> {
                    try {
                        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
                        String qrContent = baseUrl + "/equipos/detalle/" + equipo.getId();
                        byte[] qrImage = QRCodeGenerator.generateQRCodeImage(qrContent, 250, 250);
                        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrImage);
                    } catch (Exception e) {
                        return ResponseEntity.internalServerError().<byte[]>build();
                    }
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Endpoint para ver la etiqueta física imprimible (ART-07 / RF15)
    @GetMapping("/etiqueta/{id}")
    public String verEtiquetaImprimible(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return equipoService.buscarPorId(id)
                .map(equipo -> {
                    model.addAttribute("equipo", equipo);
                    return "equipos/etiqueta";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Equipo no encontrado para generar etiqueta.");
                    return "redirect:/equipos";
                });
    }
}