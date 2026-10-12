package com.tecnologico.aires.services;

import com.tecnologico.aires.models.Equipo;
import com.tecnologico.aires.models.enums.EstadoOperativo;
import com.tecnologico.aires.repositories.EquipoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EquipoService {

    @Autowired
    private EquipoRepository equipoRepository;

    public List<Equipo> listarTodos() {
        return equipoRepository.findAll();
    }

    public List<Equipo> listarPorEdificio(Long edificioId) {
        return equipoRepository.findByEspacioEdificioId(edificioId);
    }

    public List<Equipo> listarPorEspacio(Long espacioId) {
        return equipoRepository.findByEspacioId(espacioId);
    }

    public List<Equipo> listarPorEstado(EstadoOperativo estado) {
        return equipoRepository.findByEstadoOperativo(estado);
    }

    public Optional<Equipo> buscarPorId(Long id) {
        return equipoRepository.findById(id);
    }

    public Optional<Equipo> buscarPorCodigo(String codigoEquipo) {
        return equipoRepository.findByCodigoEquipo(codigoEquipo);
    }

    public boolean existeCodigo(String codigoEquipo) {
        return equipoRepository.existsByCodigoEquipo(codigoEquipo);
    }

    public boolean existeNumeroSerie(String numeroSerie) {
        return equipoRepository.existsByNumeroSerie(numeroSerie);
    }

    public Equipo guardar(Equipo equipo) {
        return equipoRepository.save(equipo);
    }

    public void eliminar(Long id) {
        equipoRepository.deleteById(id);
    }

    public void actualizarEstado(Long id, EstadoOperativo nuevoEstado) {
        equipoRepository.findById(id).ifPresent(equipo -> {
            equipo.setEstadoOperativo(nuevoEstado);
            equipoRepository.save(equipo);
        });
    }
}