package com.courrier.backend;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad Pago - Registro de pagos realizados
 * Representa pagos hacia facturas pendientes
 */
@Entity
@Data
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double monto;  // Monto pagado

    @Column(nullable = false)
    private String metodoPago;  // 'TARJETA_CREDITO', 'TRANSFERENCIA', 'EFECTIVO', 'CHEQUE'

    private String estado;  // 'PENDIENTE', 'CONFIRMADO', 'RECHAZADO'

    private LocalDateTime fecha = LocalDateTime.now();

    private String comprobante;  // URL o ID del comprobante de pago

    private String referencia;  // Referencia de la transacción bancaria

    private String descripcion;  // Notas del pago

    // RELACIÓN: Muchos pagos pertenecen a una factura
    @ManyToOne
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    // Getters y Setters (Lombok @Data genera automáticamente)
}
