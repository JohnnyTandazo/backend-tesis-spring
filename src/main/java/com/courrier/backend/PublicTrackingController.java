package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/public/tracking")
public class PublicTrackingController {

    @Autowired
    private EnvioRepository envioRepository;

    /**
     * GET /api/public/tracking/{trackingNumber}
     * Rastreo publico sin JWT.
     */
    @GetMapping("/{trackingNumber}")
    public ResponseEntity<PublicTrackingDto> obtenerRastreoPublico(@PathVariable String trackingNumber) {
        Envio envio = envioRepository.findByNumeroTracking(trackingNumber);
        if (envio == null) {
            return ResponseEntity.notFound().build();
        }

        String iniciales = construirIniciales(envio.getDestinatarioNombre());
        String ciudad = (envio.getDestinatarioCiudad() != null ? envio.getDestinatarioCiudad() : "N/A");

        List<PublicTrackingDto.EstadoHistorialDto> historial = List.of(
            new PublicTrackingDto.EstadoHistorialDto(envio.getEstado(), envio.getFechaCreacion())
        );

        LocalDateTime fechaEstimada = null;
        if (envio.getFechaCreacion() != null) {
            fechaEstimada = envio.getFechaCreacion().plusDays(7);
        }

        PublicTrackingDto dto = new PublicTrackingDto(
            envio.getEstado(),
            historial,
            envio.getDescripcion(),
            fechaEstimada,
            iniciales,
            ciudad
        );

        return ResponseEntity.ok(dto);
    }

    private String construirIniciales(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "N/A";
        }
        String[] partes = nombre.trim().split("\\s+");
        String primera = partes[0].substring(0, 1).toUpperCase();
        String ultima = partes.length > 1 ? partes[partes.length - 1].substring(0, 1).toUpperCase() : "";
        return primera + ultima;
    }
}
