package com.courrier.backend;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface EnvioRepository extends JpaRepository<Envio, Long> {
    // Buscar envíos por el ID del usuario
    List<Envio> findByUsuarioId(Long usuarioId);
    
    // Buscar envíos por el ID del usuario con ordenamiento
    List<Envio> findByUsuarioId(Long usuarioId, Sort sort);
    
    // Buscar por número de tracking
    Envio findByNumeroTracking(String numeroTracking);
    
    /**
     * MÉTODO DIRECTO Y SEGURO: Actualiza el estado del envío directamente en BD
     * Evita problemas de relaciones rotas o lazy loading
     */
    @Modifying
    @Transactional
    @Query("UPDATE Envio e SET e.estado = :estado WHERE e.id = :id")
    void actualizarEstado(@Param("id") Long id, @Param("estado") String estado);
}
