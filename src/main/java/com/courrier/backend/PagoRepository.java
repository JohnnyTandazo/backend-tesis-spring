package com.courrier.backend;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Repositorio para la entidad Pago
 * Proporciona acceso a datos de pagos
 */
public interface PagoRepository extends JpaRepository<Pago, Long> {
    
    // Obtener pagos de una factura
    List<Pago> findByFacturaId(Long facturaId);
    
    // Obtener pagos de una factura con ordenamiento
    List<Pago> findByFacturaId(Long facturaId, Sort sort);
    
    // Obtener pagos confirmados de una factura
    List<Pago> findByFacturaIdAndEstado(Long facturaId, String estado);
    
    /**
     * Obtener todos los pagos de un usuario (a través de sus facturas)
     * JOIN con Factura para filtrar por usuario_id
     */
    @Query("SELECT p FROM Pago p " +
           "JOIN p.factura f " +
           "WHERE f.usuario.id = :usuarioId " +
           "ORDER BY p.fecha DESC")
    List<Pago> findByUsuarioId(@Param("usuarioId") Long usuarioId);
}

