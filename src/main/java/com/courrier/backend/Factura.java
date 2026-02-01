package com.courrier.backend;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

/**
 * Entidad Factura - Documento de cobranza
 * Representa facturas generadas por envíos
 * 
 * JSON SERIALIZATION:
 * - envio: Se incluye completo (para saber qué paquete fue facturado)
 * - usuario: Se ignora (evita ciclos infinitos)
 * - Siempre se incluye envioId
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

    // ========================================
    // RELACIÓN: Vinculación con Envío que genera la factura
    // ========================================
    @Column(name = "envio_id")
    private Long envioId;  // ID del envío - SIEMPRE se serializa en JSON
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "envio_id", insertable = false, updatable = false)
    private Envio envio;  // Relación JPA con Envio - SE INCLUYE COMPLETO EN JSON

    // ========================================
    // RELACIÓN: Muchas facturas pertenecen a un usuario
    // ========================================
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore  // NO se serializa para evitar ciclos infinitos (Usuario -> Facturas -> Usuario)
    private Usuario usuario;

    // Getters y Setters (Lombok @Data genera automáticamente)
}
