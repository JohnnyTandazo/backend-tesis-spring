package com.courrier.backend;

import lombok.Data;

/**
 * DTO para la creación de envíos.
 * Recibe los datos del Frontend y mapea a la entidad Envio.
 * 
 * PATRÓN SNAPSHOT: Incluye campos de destinatario histórico
 * para capturar la dirección de entrega en el momento del envío.
 */
@Data
public class CrearEnvioRequest {

    private String numeroTracking;      // Ej: USA-001
    private String descripcion;          // Ej: Laptop HP
    private Double pesoLibras;
    private Double valorDeclarado;
    private String estado;               // 'EN_MIAMI', 'EN_TRANSITO', 'ENTREGADO', etc.
    private String categoria;            // A, B, C, etc.
    private Long usuarioId;              // ID del usuario que realiza el envío

    // ========================================
    // PATRÓN SNAPSHOT: Datos del Destinatario
    // ========================================
    // Captura histórica de la dirección de destino
    // para auditoría y recuperación de información
    
    private String destinatarioNombre;
    private String destinatarioCiudad;
    private String destinatarioDireccion;
    private String destinatarioTelefono;

    // Getters y Setters (Lombok @Data genera automáticamente)
}
