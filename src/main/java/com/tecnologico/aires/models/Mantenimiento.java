package com.tecnologico.aires.models;

import com.tecnologico.aires.models.enums.EstadoOperativo;
import com.tecnologico.aires.models.enums.TipoMantenimiento;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "mantenimientos")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Mantenimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Opcional: solo si esta intervención cierra un ticket (PROC-07)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporte_id")
    private Reporte reporte;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_mantenimiento", nullable = false, length = 20)
    private TipoMantenimiento tipoMantenimiento;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "fecha_mantenimiento", nullable = false)
    private LocalDate fechaMantenimiento;

    @Column(name = "tecnico_responsable", nullable = false, length = 150)
    private String tecnicoResponsable;

    @Column(name = "descripcion_trabajo", nullable = false, columnDefinition = "TEXT")
    private String descripcionTrabajo;

    @Column(name = "refacciones_utilizadas", columnDefinition = "TEXT")
    private String refaccionesUtilizadas;

    @Column(name = "carga_gas_gramos")
    private Integer cargaGasGramos;

    @Column(name = "costo_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_final_equipo", nullable = false, length = 30)
    private EstadoOperativo estadoFinalEquipo;
}
