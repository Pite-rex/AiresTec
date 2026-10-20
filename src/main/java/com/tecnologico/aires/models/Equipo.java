package com.tecnologico.aires.models;

import com.tecnologico.aires.models.enums.EstadoOperativo;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "equipos")
@Data
@NoArgsConstructor

public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_equipo", unique = true, nullable = false, length = 50)
    private String codigoEquipo; // Código interno o de inventario

    @Column(name = "numero_serie", unique = true, nullable = false, length = 50)
    private String numeroSerie;

    @Column(name = "marca", nullable = false, length = 60)
    private String marca;

    @Column(name = "modelo", nullable = false, length = 60)
    private String modelo;

    @Column(name = "capacidad_btu", nullable = false)
    private Integer capacidadBtu; // Capacidad térmica en BTU

    @Column(name = "tipo_equipo", nullable = false, length = 50)
    private String tipoEquipo; // Minisplit, Paquete, Inverter, Cassette

    @Column(name = "gas_refrigerante", nullable = false, length = 30)
    private String gasRefrigerante; // R-410A, R-32, R-22

    @Column(name = "voltaje", nullable = false, length = 10)
    private String voltaje; // 110V o 220V

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_operativo", nullable = false, length = 30)
    private EstadoOperativo estadoOperativo = EstadoOperativo.OPERATIVO;

    @Column(name = "fecha_instalacion", nullable = false)
    private LocalDate fechaInstalacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espacio_id", nullable = false)
    private Espacio espacio;
}