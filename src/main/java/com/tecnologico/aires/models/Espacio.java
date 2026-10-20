package com.tecnologico.aires.models;

import com.tecnologico.aires.models.enums.TipoEspacio;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "espacios")
@Data
@NoArgsConstructor
public class Espacio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clave_espacio", unique = true, nullable = false, length = 20)
    private String claveEspacio; // Ej: ESP-001

    @Column(name = "nombre_espacio", nullable = false, length = 100)
    private String nombreEspacio; // Ej: Lab Cómputo 1, Aula 12

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_espacio", nullable = false)
    private TipoEspacio tipoEspacio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edificio_id", nullable = false)
    private Edificio edificio;
}