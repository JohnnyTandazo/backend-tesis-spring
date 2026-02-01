package com.courrier.backend;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "direcciones")
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String alias; // Ej: "Casa", "Oficina", "Casa de mamá"

    @Column(nullable = false)
    private String callePrincipal; // Calle principal con numeración

    private String calleSecundaria; // Calle secundaria (intersección)

    @Column(nullable = false)
    private String ciudad; // Ej: "Guayaquil", "Quito"

    @Column(nullable = false)
    private String telefono; // Teléfono de contacto en esa dirección

    private String referencia; // Ej: "Casa color verde, frente al parque"

    @Column(nullable = false)
    private Boolean esPrincipal = false; // Si es la dirección principal del usuario

    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // RELACIÓN: Muchas direcciones pertenecen a un usuario
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
