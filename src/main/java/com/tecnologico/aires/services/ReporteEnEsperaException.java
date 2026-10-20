package com.tecnologico.aires.services;

import java.time.Duration;

public class ReporteEnEsperaException extends RuntimeException {

    private final Duration espera;

    public ReporteEnEsperaException(Duration espera) {
        super("Ya existe un reporte abierto para este equipo.");
        this.espera = espera;
    }

    public Duration getEspera() {
        return espera;
    }
}