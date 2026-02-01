package com.courrier.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Long> {
    
    // Obtener todas las direcciones de un usuario específico
    List<Direccion> findByUsuarioId(Long usuarioId);
    
    // Obtener la dirección principal de un usuario
    Direccion findByUsuarioIdAndEsPrincipalTrue(Long usuarioId);
    
    // Contar cuántas direcciones tiene un usuario
    long countByUsuarioId(Long usuarioId);
}
