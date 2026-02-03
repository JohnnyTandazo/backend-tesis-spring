package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;

/**
 * FacturaController - API REST para Facturas
 * Endpoints: GET, POST, PUT, DELETE
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    @Autowired
    private FacturaService facturaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * GET /api/facturas/pendientes?usuarioId={id}
     * Obtener facturas pendientes de un usuario (para dropdown)
     */
    @GetMapping("/pendientes")
    public ResponseEntity<List<Factura>> obtenerPendientes(@RequestParam Long usuarioId) {
        System.out.println("⏳ [GET /api/facturas/pendientes] PETICIÓN RECIBIDA - Usuario: " + usuarioId);
        
        try {
            List<Factura> pendientes = facturaService.obtenerPendientes(usuarioId);
            System.out.println("✅ Se encontraron " + pendientes.size() + " facturas pendientes");
            return ResponseEntity.ok(pendientes);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/facturas/usuario/{usuarioId}
     * Obtener todas las facturas de un usuario
     * Devuelve incluido el envioId en el JSON
     * IMPORTANTE: Este debe ir ANTES de /{id} para evitar conflictos de ruta
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Factura>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        System.out.println("📋 [GET /api/facturas/usuario/" + usuarioId + "] PETICIÓN RECIBIDA");
        
        try {
            List<Factura> facturas = facturaService.obtenerPorUsuario(usuarioId);
            if (facturas == null) {
                return ResponseEntity.ok(List.of());
            }
            System.out.println("✅ Se encontraron " + facturas.size() + " facturas");
            return ResponseEntity.ok(facturas);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * GET /api/facturas/{id}
     * Obtener una factura por ID
     * IMPORTANTE: Este debe ir DESPUÉS de /usuario/{usuarioId}
     * SEGURIDAD: Verifica que el usuario tenga permiso para ver esta factura
     */
    @GetMapping("/{id}")
    public ResponseEntity<Factura> obtenerPorId(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioActualId,
            @RequestParam(value = "usuarioActualId", required = false) Long usuarioActualIdParam) {
        
        // Priorizar header, luego query param
        Long usuarioId = usuarioActualId != null ? usuarioActualId : usuarioActualIdParam;
        
        System.out.println("🔍 [GET /api/facturas/" + id + "] PETICIÓN RECIBIDA - Usuario autenticado: " + usuarioId);
        
        Optional<Factura> facturaOpt = facturaService.obtenerPorId(id);
        if (!facturaOpt.isPresent()) {
            System.out.println("❌ Factura no encontrada");
            return ResponseEntity.notFound().build();
        }
        
        Factura factura = facturaOpt.get();
        
        // 🔒 VERIFICACIÓN IDOR: Comprobar propiedad del recurso
        if (usuarioId != null) {
            Usuario usuarioActual = usuarioRepository.findById(usuarioId).orElse(null);
            
            if (usuarioActual != null) {
                String rol = usuarioActual.getRol().toUpperCase();
                
                // ADMIN y OPERADOR tienen acceso total
                if (rol.equals("ADMIN") || rol.equals("OPERADOR")) {
                    System.out.println("✅ Acceso autorizado: Usuario " + rol);
                    return ResponseEntity.ok(factura);
                }
                
                // CLIENTE: Solo puede ver sus propias facturas
                if (rol.equals("CLIENTE")) {
                    if (!factura.getUsuario().getId().equals(usuarioActual.getId())) {
                        System.out.println("🚫 ACCESO DENEGADO: Cliente " + usuarioId + " intentó acceder a factura de usuario " + factura.getUsuario().getId());
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver esta factura");
                    }
                    System.out.println("✅ Acceso autorizado: Factura pertenece al cliente");
                }
            }
        }
        
        System.out.println("✅ Factura encontrada: " + factura.getNumeroFactura());
        return ResponseEntity.ok(factura);
    }

    /**
     * POST /api/facturas
     * Crear una nueva factura
     */
    @PostMapping
    public ResponseEntity<Factura> crearFactura(@RequestBody Factura factura) {
        System.out.println("✍️ [POST /api/facturas] PETICIÓN RECIBIDA - Factura: " + factura.getNumeroFactura());
        
        try {
            Factura nueva = facturaService.crearFactura(factura);
            System.out.println("✅ Factura creada con ID: " + nueva.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /api/facturas/{id}/estado
     * Actualizar estado de una factura
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<Factura> actualizarEstado(
            @PathVariable Long id,
            @RequestParam String nuevoEstado) {
        System.out.println("🔄 [PUT /api/facturas/" + id + "/estado] Estado: " + nuevoEstado);
        
        try {
            Factura actualizada = facturaService.actualizarEstado(id, nuevoEstado);
            System.out.println("✅ Estado actualizado");
            return ResponseEntity.ok(actualizada);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * DELETE /api/facturas/{id}
     * Eliminar una factura
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFactura(@PathVariable Long id) {
        System.out.println("🗑️ [DELETE /api/facturas/" + id + "] PETICIÓN RECIBIDA");
        
        try {
            facturaService.eliminarFactura(id);
            System.out.println("✅ Factura eliminada");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
