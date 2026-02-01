package com.courrier.backend;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repositorio para la entidad Factura
 * Proporciona acceso a datos de facturas
 */
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    
    // Obtener todas las facturas de un usuario
    List<Factura> findByUsuarioId(Long usuarioId);
    
    // Obtener todas las facturas de un usuario con ordenamiento
    List<Factura> findByUsuarioId(Long usuarioId, Sort sort);
    
    // Obtener facturas pendientes de un usuario
    List<Factura> findByUsuarioIdAndEstado(Long usuarioId, String estado);
    
    // Obtener facturas pendientes de un usuario con ordenamiento
    List<Factura> findByUsuarioIdAndEstado(Long usuarioId, String estado, Sort sort);
}
