package com.tecnologico.aires.repositories;

import com.tecnologico.aires.models.Equipo;
import com.tecnologico.aires.models.enums.EstadoOperativo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    // Validaciones de unicidad (PROC-03 / RF11)
    boolean existsByCodigoEquipo(String codigoEquipo);
    boolean existsByNumeroSerie(String numeroSerie);

    // Búsquedas individuales
    Optional<Equipo> findByCodigoEquipo(String codigoEquipo);
    Optional<Equipo> findByNumeroSerie(String numeroSerie);

    // Consultas por ubicación física
    List<Equipo> findByEspacioId(Long espacioId);
    List<Equipo> findByEspacioEdificioId(Long edificioId);

    // Consultas por estado operativo (Operativo, En Mantenimiento, Fuera de Servicio, Baja)
    List<Equipo> findByEstadoOperativo(EstadoOperativo estadoOperativo);
    List<Equipo> findByEspacioEdificioIdAndEstadoOperativo(Long edificioId, EstadoOperativo estadoOperativo);

    // Conteo para estadísticas y auditoría general por edificio (ART-02 / RF24)
    long countByEspacioEdificioId(Long edificioId);
    long countByEspacioEdificioIdAndEstadoOperativo(Long edificioId, EstadoOperativo estadoOperativo);
    long countByEstadoOperativo(EstadoOperativo estadoOperativo);
}