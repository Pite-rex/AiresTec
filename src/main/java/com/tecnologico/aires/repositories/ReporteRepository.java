package com.tecnologico.aires.repositories;

import com.tecnologico.aires.models.Reporte;
import com.tecnologico.aires.models.enums.EstadoReporte;
import com.tecnologico.aires.models.enums.Prioridad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Collection;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    // Búsqueda por folio único institucional (PROC-05 / RF16)
    Optional<Reporte> findByFolioReporte(String folioReporte);
    boolean existsByFolioReporte(String folioReporte);

    // Consultas por estado del ticket
    List<Reporte> findByEstadoReporteOrderByFechaReporteDesc(EstadoReporte estadoReporte);

    List<Reporte> findByEquipoIdAndEstadoReporteIn(Long equipoId, Collection<EstadoReporte> estados);

    // Consultas por prioridad
    List<Reporte> findByPrioridadOrderByFechaReporteDesc(Prioridad prioridad);

    // Historial de reportes por equipo
    List<Reporte> findByEquipoIdOrderByFechaReporteDesc(Long equipoId);

    // Consultas de reportes por edificio del equipo
    List<Reporte> findByEquipoEspacioEdificioIdOrderByFechaReporteDesc(Long edificioId);

    // Listado general ordenado por fecha descendente
    List<Reporte> findAllByOrderByFechaReporteDesc();

    // Métricas y conteos para panel de control (RF24)
    long countByEstadoReporte(EstadoReporte estadoReporte);
    long countByPrioridad(Prioridad prioridad);
}