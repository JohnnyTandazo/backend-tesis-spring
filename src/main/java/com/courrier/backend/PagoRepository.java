package com.courrier.backend;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
