package com.tecnologico.aires.services;

import com.tecnologico.aires.models.Equipo;
import com.tecnologico.aires.models.Reporte;
import com.tecnologico.aires.models.enums.EstadoOperativo;
import com.tecnologico.aires.models.enums.EstadoReporte;
import com.tecnologico.aires.models.enums.Prioridad;
import com.tecnologico.aires.repositories.EquipoRepository;
import com.tecnologico.aires.repositories.ReporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class ReporteService {

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    public List<Reporte> listarTodos() {
        return reporteRepository.findAllByOrderByFechaReporteDesc();
    }

    public List<Reporte> listarPorEstado(EstadoReporte estado) {
        return reporteRepository.findByEstadoReporteOrderByFechaReporteDesc(estado);
    }

    public List<Reporte> listarPorPrioridad(Prioridad prioridad) {
        return reporteRepository.findByPrioridadOrderByFechaReporteDesc(prioridad);
    }

    public List<Reporte> listarPorEquipo(Long equipoId) {
        return reporteRepository.findByEquipoIdOrderByFechaReporteDesc(equipoId);
    }

    public List<Reporte> listarPorEdificio(Long edificioId) {
        return reporteRepository.findByEquipoEspacioEdificioIdOrderByFechaReporteDesc(edificioId);
    }

    public Optional<Reporte> buscarPorId(Long id) {
        return reporteRepository.findById(id);
    }

    public Optional<Reporte> buscarPorFolio(String folioReporte) {
        return reporteRepository.findByFolioReporte(folioReporte);
    }

    @Transactional
    public Reporte registrarReporte(Reporte reporte) {
        // Asignar fecha y hora actual
        reporte.setFechaReporte(LocalDateTime.now());
        
        // Estado inicial obligatorio
        reporte.setEstadoReporte(EstadoReporte.PENDIENTE);

        // Generar folio único institucional si no viene definido
        if (reporte.getFolioReporte() == null || reporte.getFolioReporte().trim().isEmpty()) {
            reporte.setFolioReporte(generarFolioUnico());
        }

        // Actualizar automáticamente el estado del equipo a EN_MANTENIMIENTO
        if (reporte.getEquipo() != null && reporte.getEquipo().getId() != null) {
            equipoRepository.findById(reporte.getEquipo().getId()).ifPresent(eq -> {
                if (reporte.getPrioridad() == Prioridad.ALTA || reporte.getPrioridad() == Prioridad.URGENTE) {
                    eq.setEstadoOperativo(EstadoOperativo.FUERA_DE_SERVICIO);
                } else if (eq.getEstadoOperativo() == EstadoOperativo.OPERATIVO) {
                    eq.setEstadoOperativo(EstadoOperativo.EN_MANTENIMIENTO);
                }
                equipoRepository.save(eq);
            });
        }

        return reporteRepository.save(reporte);
    }

    @Transactional
    public Reporte cambiarEstado(Long id, EstadoReporte nuevoEstado) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado con ID: " + id));

        reporte.setEstadoReporte(nuevoEstado);

        // Si el ticket es resuelto o cancelado, verificar si el equipo vuelve a estar operativo
        if (nuevoEstado == EstadoReporte.ATENDIDO) {
            if (reporte.getEquipo() != null) {
                Equipo eq = reporte.getEquipo();
                eq.setEstadoOperativo(EstadoOperativo.OPERATIVO);
                equipoRepository.save(eq);
            }
        }

        return reporteRepository.save(reporte);
    }

    public void eliminar(Long id) {
        reporteRepository.deleteById(id);
    }

    private String generarFolioUnico() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String folio;
        Random random = new Random();
        do {
            int aleatorio = 1000 + random.nextInt(9000);
            folio = "REP-" + fecha + "-" + aleatorio;
        } while (reporteRepository.existsByFolioReporte(folio));
        return folio;
    }
}