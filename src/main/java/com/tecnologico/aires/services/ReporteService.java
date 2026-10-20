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
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.EnumSet;
import java.util.Set;

import java.time.Duration;
import java.util.Comparator;

@Service
public class ReporteService {

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    private static final Duration ESPERA_ENTRE_REPORTES = Duration.ofHours(2);

    public record ResultadoPublico(Reporte reporte, boolean actualizado) {
    }

    private static final Set<EstadoReporte> ESTADOS_ACTIVOS = EnumSet.of(EstadoReporte.PENDIENTE,
            EstadoReporte.EN_PROCESO);

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

    private List<Reporte> reportesAbiertos(Long equipoId) {
        return reporteRepository.findByEquipoIdAndEstadoReporteIn(equipoId, ESTADOS_ACTIVOS).stream()
                .sorted(Comparator.comparing(Reporte::getFechaReporte).reversed())
                .toList();
    }

    private Duration esperaRestante(Reporte reporte) {
        Duration transcurrido = Duration.between(reporte.getFechaReporte(), LocalDateTime.now());
        Duration restante = ESPERA_ENTRE_REPORTES.minus(transcurrido);
        return restante.isNegative() ? Duration.ZERO : restante;
    }

    /**
     * Tiempo que falta para poder volver a reportar el equipo; ZERO si el
     * formulario está disponible.
     */
    public Duration esperaParaReportar(Equipo equipo) {
        if (equipo.getEstadoOperativo() == EstadoOperativo.OPERATIVO) {
            return Duration.ZERO; // ya lo repararon
        }
        return reportesAbiertos(equipo.getId()).stream().findFirst()
                .map(this::esperaRestante)
                .orElse(Duration.ZERO);
    }

    private String limpiar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    @Transactional
    public ResultadoPublico registrarReportePublico(Long equipoId, String nombre, String correo,
            String telefono, Prioridad prioridad, String descripcion) {

        if (nombre == null || nombre.isBlank() || descripcion == null || descripcion.isBlank()
                || prioridad == null || equipoId == null) {
            throw new IllegalArgumentException("Nombre, severidad y descripción son obligatorios.");
        }
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new IllegalArgumentException("El equipo seleccionado no existe."));
        if (equipo.getEstadoOperativo() == EstadoOperativo.BAJA) {
            throw new IllegalArgumentException("El equipo está dado de baja y no admite reportes.");
        }

        List<Reporte> abiertos = reportesAbiertos(equipoId);
        if (!abiertos.isEmpty()) {
            if (equipo.getEstadoOperativo() == EstadoOperativo.OPERATIVO) {
                // Ya lo repararon: los reportes viejos salen de la lista y se crea uno nuevo
                abiertos.forEach(r -> r.setEstadoReporte(EstadoReporte.ATENDIDO));
                reporteRepository.saveAll(abiertos);
            } else {
                Reporte vigente = abiertos.get(0);
                Duration espera = esperaRestante(vigente);
                if (!espera.isZero()) {
                    throw new ReporteEnEsperaException(espera);
                }
                // Pasó el tiempo de espera: se actualiza el mismo reporte
                vigente.setNombreSolicitante(nombre.trim());
                vigente.setCorreoSolicitante(limpiar(correo));
                vigente.setTelefonoSolicitante(limpiar(telefono));
                vigente.setPrioridad(prioridad);
                vigente.setDescripcionFalla(descripcion.trim());
                vigente.setFechaReporte(LocalDateTime.now());
                Reporte guardado = reporteRepository.save(vigente);
                sincronizarEstadoEquipo(equipo);
                return new ResultadoPublico(guardado, true);
            }
        }

        Reporte nuevo = new Reporte();
        nuevo.setNombreSolicitante(nombre.trim());
        nuevo.setCorreoSolicitante(limpiar(correo));
        nuevo.setTelefonoSolicitante(limpiar(telefono));
        nuevo.setPrioridad(prioridad);
        nuevo.setDescripcionFalla(descripcion.trim());
        nuevo.setEquipo(equipo);
        return new ResultadoPublico(registrarReporte(nuevo), false);
    }

    @Transactional
    public Reporte registrarReporte(Reporte reporte) {
        if (reporte.getEquipo() == null || reporte.getEquipo().getId() == null) {
            throw new IllegalArgumentException("Debes seleccionar un equipo.");
        }
        Equipo equipo = equipoRepository.findById(reporte.getEquipo().getId())
                .orElseThrow(() -> new IllegalArgumentException("El equipo seleccionado no existe."));
        if (equipo.getEstadoOperativo() == EstadoOperativo.BAJA) {
            throw new IllegalArgumentException("El equipo está dado de baja y no admite reportes.");
        }

        reporte.setEquipo(equipo);
        reporte.setFechaReporte(LocalDateTime.now());
        reporte.setEstadoReporte(EstadoReporte.PENDIENTE);
        if (reporte.getFolioReporte() == null || reporte.getFolioReporte().trim().isEmpty()) {
            reporte.setFolioReporte(generarFolioUnico());
        }

        // Misma regla de antes: ALTA/URGENTE -> fuera de servicio; el resto -> en
        // mantenimiento
        if (reporte.getPrioridad() == Prioridad.ALTA || reporte.getPrioridad() == Prioridad.URGENTE) {
            equipo.setEstadoOperativo(EstadoOperativo.FUERA_DE_SERVICIO);
        } else if (equipo.getEstadoOperativo() == EstadoOperativo.OPERATIVO) {
            equipo.setEstadoOperativo(EstadoOperativo.EN_MANTENIMIENTO);
        }
        equipoRepository.save(equipo);

        return reporteRepository.save(reporte);
    }

    @Transactional
    public Reporte cambiarEstado(Long id, EstadoReporte nuevoEstado) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado con ID: " + id));

        reporte.setEstadoReporte(nuevoEstado);
        Reporte guardado = reporteRepository.save(reporte);
        sincronizarEstadoEquipo(reporte.getEquipo());
        return guardado;
    }

    @Transactional
    public void eliminar(Long id) {
        reporteRepository.findById(id).ifPresent(r -> {
            Equipo equipo = r.getEquipo();
            reporteRepository.delete(r);
            reporteRepository.flush();
            sincronizarEstadoEquipo(equipo);
        });
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

    public List<Reporte> buscar(String filtroEstado, Prioridad prioridad) {
        return reporteRepository.findAllByOrderByFechaReporteDesc().stream()
                .filter(r -> coincideConFiltro(filtroEstado, r.getEstadoReporte()))
                .filter(r -> prioridad == null || r.getPrioridad() == prioridad)
                .toList();
    }

    private boolean coincideConFiltro(String filtro, EstadoReporte estado) {
        String f = (filtro == null || filtro.isBlank()) ? "ACTIVOS" : filtro.toUpperCase();
        return switch (f) {
            case "TODOS" -> true;
            case "ACTIVOS" -> ESTADOS_ACTIVOS.contains(estado);
            case "CERRADOS" -> !ESTADOS_ACTIVOS.contains(estado);
            default -> estado.name().equals(f);
        };
    }

    // El estado del equipo depende de los reportes que siguen abiertos
    private void sincronizarEstadoEquipo(Equipo equipo) {
        if (equipo == null || equipo.getEstadoOperativo() == EstadoOperativo.BAJA) {
            return;
        }
        List<Reporte> abiertos = reporteRepository
                .findByEquipoIdAndEstadoReporteIn(equipo.getId(), ESTADOS_ACTIVOS);

        EstadoOperativo nuevo;
        if (abiertos.isEmpty()) {
            nuevo = EstadoOperativo.OPERATIVO;
        } else if (abiertos.stream()
                .anyMatch(r -> r.getPrioridad() == Prioridad.ALTA || r.getPrioridad() == Prioridad.URGENTE)) {
            nuevo = EstadoOperativo.FUERA_DE_SERVICIO;
        } else {
            nuevo = EstadoOperativo.EN_MANTENIMIENTO;
        }
        equipo.setEstadoOperativo(nuevo);
        equipoRepository.save(equipo);
    }
}