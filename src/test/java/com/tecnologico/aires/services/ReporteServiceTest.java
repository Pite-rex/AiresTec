package com.tecnologico.aires.services;

import com.tecnologico.aires.models.Equipo;
import com.tecnologico.aires.models.Reporte;
import com.tecnologico.aires.models.enums.EstadoOperativo;
import com.tecnologico.aires.models.enums.EstadoReporte;
import com.tecnologico.aires.models.enums.Prioridad;
import com.tecnologico.aires.repositories.EquipoRepository;
import com.tecnologico.aires.repositories.ReporteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ReporteRepository reporteRepository;

    @Mock
    private EquipoRepository equipoRepository;

    @InjectMocks
    private ReporteService reporteService;

    @Test
    void registrarReporteInicializaEstadoFolioYEquipoOperativo() {
        Reporte reporte = new Reporte();
        reporte.setPrioridad(Prioridad.MEDIA);
        Equipo referencia = new Equipo();
        referencia.setId(7L);
        reporte.setEquipo(referencia);
        Equipo equipo = new Equipo();
        equipo.setId(7L);
        equipo.setEstadoOperativo(EstadoOperativo.OPERATIVO);
        when(equipoRepository.findById(7L)).thenReturn(Optional.of(equipo));
        when(reporteRepository.existsByFolioReporte(anyString())).thenReturn(false);
        when(reporteRepository.save(reporte)).thenReturn(reporte);

        Reporte resultado = reporteService.registrarReporte(reporte);

        assertSame(reporte, resultado);
        assertEquals(EstadoReporte.PENDIENTE, reporte.getEstadoReporte());
        assertNotNull(reporte.getFechaReporte());
        assertTrue(reporte.getFolioReporte().matches("REP-\\d{8}-\\d{4}"));
        assertEquals(EstadoOperativo.EN_MANTENIMIENTO, equipo.getEstadoOperativo());
        verify(equipoRepository).save(equipo);
        verify(reporteRepository).save(reporte);
    }

    @Test
    void registrarReporteUrgentePoneEquipoFueraDeServicio() {
        Reporte reporte = new Reporte();
        reporte.setPrioridad(Prioridad.URGENTE);
        Equipo referencia = new Equipo();
        referencia.setId(8L);
        reporte.setEquipo(referencia);
        Equipo equipo = new Equipo();
        equipo.setId(8L);
        equipo.setEstadoOperativo(EstadoOperativo.OPERATIVO);
        when(equipoRepository.findById(8L)).thenReturn(Optional.of(equipo));
        when(reporteRepository.existsByFolioReporte(anyString())).thenReturn(false);
        when(reporteRepository.save(reporte)).thenReturn(reporte);

        reporteService.registrarReporte(reporte);

        assertEquals(EstadoOperativo.FUERA_DE_SERVICIO, equipo.getEstadoOperativo());
    }

    @Test
    void cambiarEstadoAtendidoReactivaElEquipo() {
        Reporte reporte = new Reporte();
        Equipo equipo = new Equipo();
        equipo.setEstadoOperativo(EstadoOperativo.EN_MANTENIMIENTO);
        reporte.setEquipo(equipo);
        when(reporteRepository.findById(3L)).thenReturn(Optional.of(reporte));
        when(reporteRepository.save(reporte)).thenReturn(reporte);

        Reporte resultado = reporteService.cambiarEstado(3L, EstadoReporte.ATENDIDO);

        assertSame(reporte, resultado);
        assertEquals(EstadoReporte.ATENDIDO, reporte.getEstadoReporte());
        assertEquals(EstadoOperativo.OPERATIVO, equipo.getEstadoOperativo());
        verify(equipoRepository).save(equipo);
        verify(reporteRepository).save(reporte);
    }

    @Test
    void cambiarEstadoFallaCuandoElReporteNoExiste() {
        when(reporteRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> reporteService.cambiarEstado(404L, EstadoReporte.ATENDIDO));
        verify(reporteRepository, never()).save(any());
    }
}
