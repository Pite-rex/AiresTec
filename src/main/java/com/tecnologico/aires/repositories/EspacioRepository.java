package com.tecnologico.aires.repositories;

import com.tecnologico.aires.models.Espacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EspacioRepository extends JpaRepository<Espacio, Long> {
    boolean existsByClaveEspacio(String claveEspacio);
    Optional<Espacio> findByClaveEspacio(String claveEspacio);
    List<Espacio> findByEdificioId(Long edificioId);
}