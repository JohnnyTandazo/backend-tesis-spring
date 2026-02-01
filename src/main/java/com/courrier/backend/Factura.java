package com.courrier.backend;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad Factura - Documento de cobranza
 * Representa facturas generadas por envíos
 */
@Entity
@Data
@Table(name = "facturas")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double monto;  // Monto a facturar

    @Column(nullable = false)
    private String estado;  // 'PENDIENTE', 'PAGADA', 'VENCIDA', 'ANULADA'

    private String descripcion;  // Ej: "Envío USA-001"

    private LocalDateTime fechaEmision = LocalDateTime.now();

    private LocalDateTime fechaVencimiento;

    private String numeroFactura;  // Ej: FAC-2026-001

    // RELACIÓN: Vinculación con Envío que genera la factura
    @Column(name = "envio_id")
    private Long envioId;  // ID del envío que genera esta factura

    // RELACIÓN: Muchas facturas pertenecen a un usuario
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Getters y Setters (Lombok @Data genera automáticamente)
}
