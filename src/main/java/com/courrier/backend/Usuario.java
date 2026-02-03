package com.courrier.backend;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity // Esto le dice a Java: "Convierte esta clase en una tabla SQL"
@Data   // Esto le dice a Lombok: "Crea los Getters y Setters por mí"
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false, length = 255)  // 🔒 BCrypt genera hashes de ~60 caracteres
    private String password;

    private String telefono;

    @Column(nullable = false)
    private String rol; // 'CLIENTE', 'OPERADOR', 'ADMIN'

    private LocalDateTime fechaRegistro = LocalDateTime.now();
}