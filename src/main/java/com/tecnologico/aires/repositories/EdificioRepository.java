package com.tecnologico.aires.repositories;

import com.tecnologico.aires.models.Edificio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EdificioRepository extends JpaRepository<Edificio, Long> {
    boolean existsByClaveEdificio(String claveEdificio);
    Optional<Edificio> findByClaveEdificio(String claveEdificio);
}