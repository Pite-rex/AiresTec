package com.tecnologico.aires.services;

import com.tecnologico.aires.models.Mantenimiento;
import com.tecnologico.aires.models.Reporte;
import com.tecnologico.aires.models.enums.EstadoReporte;
import com.tecnologico.aires.models.enums.TipoMantenimiento;
import com.tecnologico.aires.repositories.EquipoRepository;
import com.tecnologico.aires.repositories.MantenimientoRepository;
import com.tecnologico.aires.repositories.ReporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class MantenimientoService {

    @Autowired
    private MantenimientoRepository mantenimientoRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private ReporteRepository reporteRepository;

    public List<Mantenimiento> listarTodos() {
        return mantenimientoRepository.findAllByOrderByFechaMantenimientoDesc();
    }

    public List<Mantenimiento> listarPorEquipo(Long equipoId) {
        return mantenimientoRepository.findByEquipoIdOrderByFechaMantenimientoDesc(equipoId);
    }

    public List<Mantenimiento> listarPorTipo(TipoMantenimiento tipo) {
        return mantenimientoRepository.findByTipoMantenimientoOrderByFechaMantenimientoDesc(tipo);
    }

    public List<Mantenimiento> listarPorEquipoYTipo(Long equipoId, TipoMantenimiento tipo) {
        return mantenimientoRepository.findByEquipoIdAndTipoMantenimientoOrderByFechaMantenimientoDesc(equipoId, tipo);
    }

    public Optional<Mantenimiento> buscarPorId(Long id) {
        return mantenimientoRepository.findById(id);
    }

    public BigDecimal calcularCostoTotalAcumulado() {
        return mantenimientoRepository.sumCostoTotal();
    }

    public BigDecimal calcularGastoTotal() {
        return calcularCostoTotalAcumulado();
    }

    @Transactional
    public Mantenimiento registrarMantenimiento(Mantenimiento mantenimiento) {
        Mantenimiento guardado = mantenimientoRepository.save(mantenimiento);

        // Actualiza el estado real del equipo según el diagnóstico del técnico
        if (guardado.getEquipo() != null && guardado.getEquipo().getId() != null
                && guardado.getEstadoFinalEquipo() != null) {
            equipoRepository.findById(guardado.getEquipo().getId()).ifPresent(eq -> {
                eq.setEstadoOperativo(guardado.getEstadoFinalEquipo());
                equipoRepository.save(eq);
            });
        }

        // PROC-07: si la intervención cierra un ticket, márcalo como Atendido
        Reporte reporte = guardado.getReporte();
        if (reporte != null && reporte.getId() != null) {
            reporte.setEstadoReporte(EstadoReporte.ATENDIDO);
            reporteRepository.save(reporte);
        }

        return guardado;
    }

    public void eliminar(Long id) {
        mantenimientoRepository.deleteById(id);
    }
}