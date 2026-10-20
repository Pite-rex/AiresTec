package com.tecnologico.aires.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Entity
@Table(name = "edificios")
@Data
@NoArgsConstructor

public class Edificio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clave_edificio", unique = true, nullable = false, length = 20)
    private String claveEdificio; // Ej: EDIF-01

    @Column(name = "nombre_edificio", nullable = false, length = 100)
    private String nombreEdificio; // Ej: Edificio A

    @Column(name = "referencia_ubicacion", length = 255)
    private String referenciaUbicacion;

    @OneToMany(mappedBy = "edificio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Espacio> espacios;
}