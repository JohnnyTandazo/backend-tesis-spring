package com.courrier.backend;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

/**
 * Entidad Pago - Registro de pagos realizados
 * Representa pagos hacia facturas pendientes
 * 
 * JSON SERIALIZATION:
 * - factura: Se ignora para evitar ciclos (Pago -> Factura -> Envio -> ...)
 * - facturaId: Se expone en JSON (derivado de factura.id)
 * - monto, fecha, estado: Se incluyen siempre
 */
@Entity
@Data
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double monto;  // Monto pagado - SE SERIALIZA

    @Column(nullable = false)
    private String metodoPago;  // 'TARJETA_CREDITO', 'TRANSFERENCIA', 'EFECTIVO', 'CHEQUE'

    private String estado;  // 'PENDIENTE', 'CONFIRMADO', 'RECHAZADO'

    private LocalDateTime fecha = LocalDateTime.now();  // SE SERIALIZA

    private String comprobante;  // URL o ID del comprobante de pago

    private String referencia;  // Referencia de la transacción bancaria

    private String descripcion;  // Notas del pago

    // ========================================
    // RELACIÓN: Muchos pagos pertenecen a una factura
    // ========================================
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "factura_id", nullable = false)
    @JsonIgnore  // NO se serializa completo para evitar ciclos
    private Factura factura;

    // ========================================
    // GETTER PERSONALIZADO: Expone facturaId en JSON
    // ========================================
    public Long getFacturaId() {
        return factura != null ? factura.getId() : null;
    }

    // Getters y Setters (Lombok @Data genera automáticamente, excepto getFacturaId)
}
