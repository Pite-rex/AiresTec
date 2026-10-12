package com.tecnologico.aires.services;

import com.tecnologico.aires.models.Espacio;
import com.tecnologico.aires.repositories.EspacioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EspacioService {

    @Autowired
    private EspacioRepository espacioRepository;

    public List<Espacio> listarTodos() {
        return espacioRepository.findAll();
    }

    public List<Espacio> listarPorEdificio(Long edificioId) {
        return espacioRepository.findByEdificioId(edificioId);
    }

    public Optional<Espacio> buscarPorId(Long id) {
        return espacioRepository.findById(id);
    }

    public boolean existeClave(String clave) {
        return espacioRepository.existsByClaveEspacio(clave);
    }

    public Espacio guardar(Espacio espacio) {
        return espacioRepository.save(espacio);
    }

    public void eliminar(Long id) {
        espacioRepository.deleteById(id);
    }
}