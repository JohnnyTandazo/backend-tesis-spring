package com.courrier.backend;

import org.springframework.data.jpa.repository.JpaRepository;

// Esto le da superpoderes a tu código para hablar con MySQL
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Aquí definimos una búsqueda personalizada para el Login
    Usuario findByEmail(String email);
}