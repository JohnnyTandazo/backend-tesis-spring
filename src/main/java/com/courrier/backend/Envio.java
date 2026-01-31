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

    // RELACIÓN: Muchos envíos pertenecen a un usuario
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
