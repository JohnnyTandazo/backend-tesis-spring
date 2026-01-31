package com.courrier.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EnvioRepository extends JpaRepository<Envio, Long> {
    // Buscar envíos por el ID del usuario
    List<Envio> findByUsuarioId(Long usuarioId);
    
    // Buscar por número de tracking
    Envio findByNumeroTracking(String numeroTracking);
}
