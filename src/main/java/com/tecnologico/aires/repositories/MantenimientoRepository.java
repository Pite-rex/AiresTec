package com.tecnologico.aires.repositories;

import com.tecnologico.aires.models.Mantenimiento;
import com.tecnologico.aires.models.enums.TipoMantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Long> {

    List<Mantenimiento> findAllByOrderByFechaMantenimientoDesc();

    List<Mantenimiento> findByEquipoIdOrderByFechaMantenimientoDesc(Long equipoId);

    List<Mantenimiento> findByTipoMantenimientoOrderByFechaMantenimientoDesc(TipoMantenimiento tipo);

    List<Mantenimiento> findByEquipoIdAndTipoMantenimientoOrderByFechaMantenimientoDesc(Long equipoId, TipoMantenimiento tipo);

    @Query("SELECT COALESCE(SUM(m.costoTotal), 0) FROM Mantenimiento m")
    BigDecimal sumCostoTotal();
}