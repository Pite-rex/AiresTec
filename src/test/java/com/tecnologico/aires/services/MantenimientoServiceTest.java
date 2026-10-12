package com.tecnologico.aires.services;

import com.tecnologico.aires.models.Equipo;
import com.tecnologico.aires.models.Mantenimiento;
import com.tecnologico.aires.models.Reporte;
import com.tecnologico.aires.models.enums.EstadoOperativo;
import com.tecnologico.aires.models.enums.EstadoReporte;
import com.tecnologico.aires.repositories.EquipoRepository;
import com.tecnologico.aires.repositories.MantenimientoRepository;
import com.tecnologico.aires.repositories.ReporteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MantenimientoServiceTest {

    @Mock
    private MantenimientoRepository mantenimientoRepository;

    @Mock
    private EquipoRepository equipoRepository;

    @Mock
    private ReporteRepository reporteRepository;

    @InjectMocks
    private MantenimientoService mantenimientoService;

    @Test
    void registrarMantenimientoActualizaEquipoYCierraReporte() {
        Mantenimiento mantenimiento = new Mantenimiento();
        Equipo referencia = new Equipo();
        referencia.setId(11L);
        mantenimiento.setEquipo(referencia);
        mantenimiento.setEstadoFinalEquipo(EstadoOperativo.OPERATIVO);
        Reporte reporte = new Reporte();
        reporte.setId(12L);
        mantenimiento.setReporte(reporte);
        Equipo equipo = new Equipo();
        when(mantenimientoRepository.save(mantenimiento)).thenReturn(mantenimiento);
        when(equipoRepository.findById(11L)).thenReturn(Optional.of(equipo));

        Mantenimiento resultado = mantenimientoService.registrarMantenimiento(mantenimiento);

        assertSame(mantenimiento, resultado);
        org.junit.jupiter.api.Assertions.assertEquals(EstadoOperativo.OPERATIVO, equipo.getEstadoOperativo());
        org.junit.jupiter.api.Assertions.assertEquals(EstadoReporte.ATENDIDO, reporte.getEstadoReporte());
        verify(equipoRepository).save(equipo);
        verify(reporteRepository).save(reporte);
    }

    @Test
    void registrarMantenimientoSinReporteNoGuardaReporte() {
        Mantenimiento mantenimiento = new Mantenimiento();
        when(mantenimientoRepository.save(mantenimiento)).thenReturn(mantenimiento);

        mantenimientoService.registrarMantenimiento(mantenimiento);

        verifyNoInteractions(equipoRepository, reporteRepository);
    }
}
