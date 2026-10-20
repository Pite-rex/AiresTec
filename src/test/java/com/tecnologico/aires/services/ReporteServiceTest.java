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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.List;
import com.tecnologico.aires.models.enums.Prioridad;

import java.util.List;
import com.tecnologico.aires.models.enums.Prioridad;

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
    void registrarReportePublicoRechazaSiHayReporteAbiertoDentroDeLasDosHoras() {
        Equipo equipo = new Equipo();
        equipo.setId(9L);
        equipo.setEstadoOperativo(EstadoOperativo.EN_MANTENIMIENTO);
        Reporte abierto = new Reporte();
        abierto.setEquipo(equipo);
        abierto.setFechaReporte(LocalDateTime.now().minusMinutes(30));
        when(equipoRepository.findById(9L)).thenReturn(Optional.of(equipo));
        when(reporteRepository.findByEquipoIdAndEstadoReporteIn(eq(9L), any()))
                .thenReturn(List.of(abierto));

        ReporteEnEsperaException ex = assertThrows(ReporteEnEsperaException.class,
                () -> reporteService.registrarReportePublico(9L, "Ana", null, null,
                        Prioridad.MEDIA, "No enfría"));

        assertTrue(ex.getEspera().toMinutes() <= 90);
        verify(reporteRepository, never()).save(any());
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

    @Test
    void cambiarEstadoAtendidoNoReactivaSiHayOtroReporteAbierto() {
        Equipo equipo = new Equipo();
        equipo.setId(5L);
        equipo.setEstadoOperativo(EstadoOperativo.EN_MANTENIMIENTO);
        Reporte cerrado = new Reporte();
        cerrado.setEquipo(equipo);
        Reporte otroAbierto = new Reporte();
        otroAbierto.setPrioridad(Prioridad.MEDIA);
        when(reporteRepository.findById(3L)).thenReturn(Optional.of(cerrado));
        when(reporteRepository.save(cerrado)).thenReturn(cerrado);
        when(reporteRepository.findByEquipoIdAndEstadoReporteIn(eq(5L), any()))
                .thenReturn(List.of(otroAbierto));

        reporteService.cambiarEstado(3L, EstadoReporte.ATENDIDO);

        assertEquals(EstadoOperativo.EN_MANTENIMIENTO, equipo.getEstadoOperativo());
    }
}
