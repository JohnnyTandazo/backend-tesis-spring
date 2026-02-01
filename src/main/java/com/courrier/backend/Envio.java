package com.courrier.backend;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "envios")
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String numeroTracking; // Ej: USA-001

    private String descripcion; // Ej: Laptop HP

    private Double pesoLibras;

    private Double valorDeclarado;

    @Column(nullable = false)
    private String estado; // 'EN_MIAMI', 'EN_TRANSITO', 'ENTREGADO', etc.

    private LocalDateTime fechaCreacion = LocalDateTime.now();

    private LocalDateTime fechaEntrega;

    private String categoria; // A, B, C, etc.

    // ========================================
    // CÁLCULO DE COSTO (Sincronizado con Facturación)
    // ========================================
    // Fórmula: costo = 5.0 (Base) + (peso * 2.0) + (valorDeclarado * 0.01)
    @Column(name = "costo_envio")
    private Double costoEnvio;  // Costo calculado automáticamente al crear envío

    // RELACIÓN: Muchos envíos pertenecen a un usuario
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // ========================================
    // PATRÓN SNAPSHOT: Datos del Destinatario
    // ========================================
    // Captura histórica de la dirección de destino
    // Permite auditoría y recuperación de información
    // incluso si los datos de Dirección se modifican
    
    @Column(name = "destinatario_nombre")
    private String destinatarioNombre;

    @Column(name = "destinatario_ciudad")
    private String destinatarioCiudad;

    @Column(name = "destinatario_direccion", length = 500)
    private String destinatarioDireccion;

    @Column(name = "destinatario_telefono")
    private String destinatarioTelefono;

    // ========================================
    // GETTERS Y SETTERS - SNAPSHOT DESTINATARIO
    // ========================================

    public String getDestinatarioNombre() {
        return destinatarioNombre;
    }

    public void setDestinatarioNombre(String destinatarioNombre) {
        this.destinatarioNombre = destinatarioNombre;
    }

    public String getDestinatarioCiudad() {
        return destinatarioCiudad;
    }

    public void setDestinatarioCiudad(String destinatarioCiudad) {
        this.destinatarioCiudad = destinatarioCiudad;
    }

    public String getDestinatarioDireccion() {
        return destinatarioDireccion;
    }

    public void setDestinatarioDireccion(String destinatarioDireccion) {
        this.destinatarioDireccion = destinatarioDireccion;
    }

    public String getDestinatarioTelefono() {
        return destinatarioTelefono;
    }

    public void setDestinatarioTelefono(String destinatarioTelefono) {
        this.destinatarioTelefono = destinatarioTelefono;
    }
}
