package com.courrier.backend;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaqueteRepository extends JpaRepository<Paquete, Long> {
    // Buscar paquetes por el ID del dueño (Para que el cliente vea SOLO los suyos)
    List<Paquete> findByUsuarioId(Long usuarioId);
    
    // Buscar por Tracking (Para el rastreo público)
    Paquete findByTrackingNumber(String trackingNumber);
}