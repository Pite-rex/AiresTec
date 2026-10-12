package com.tecnologico.aires.services;

import com.tecnologico.aires.models.Edificio;
import com.tecnologico.aires.repositories.EdificioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EdificioService {

    @Autowired
    private EdificioRepository edificioRepository;

    public List<Edificio> listarTodos() {
        return edificioRepository.findAll();
    }

    public Optional<Edificio> buscarPorId(Long id) {
        return edificioRepository.findById(id);
    }

    public boolean existeClave(String clave) {
        return edificioRepository.existsByClaveEdificio(clave);
    }

    public Edificio guardar(Edificio edificio) {
        return edificioRepository.save(edificio);
    }

    public void eliminar(Long id) {
        edificioRepository.deleteById(id);
    }
}