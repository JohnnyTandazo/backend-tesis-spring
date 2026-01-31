package com.courrier.backend;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "paquetes")
public class Paquete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String trackingNumber; // Ej: USA-123456

    private String descripcion; // Ej: Zapatos Nike

    private Double pesoLibras; // Vital para el cálculo 4x4

    private Double precio;     // Vital para aduanas ($400 limit)

    public enum TipoEnvio {
        NACIONAL,
        INTERNACIONAL
    }

    @Enumerated(EnumType.STRING)
    private TipoEnvio tipoEnvio;

    private String estado;     // 'EN_MIAMI', 'EN_TRANSITO', 'ENTREGADO', etc.

    private LocalDateTime fechaCreacion = LocalDateTime.now();

    private String categoria; // A, B, C, etc.

    // RELACIN: Muchos paquetes pueden ser de un solo Usuario
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}