package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/direcciones")
public class DireccionController {

    @Autowired
    private DireccionService direccionService;

    // 1. GET: Obtener direcciones del usuario (por usuarioId como parámetro)
    @GetMapping
    public ResponseEntity<?> obtenerDireccionesDeUsuario(
            @RequestParam(required = false) Long usuarioId) {
        
        if (usuarioId == null) {
            System.out.println("⚠️ [GET /api/direcciones] No se proporcionó usuarioId");
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "error", "usuarioId es requerido",
                "ejemplo", "GET /api/direcciones?usuarioId=1"
            ));
        }
        
        System.out.println("📍 [GET /api/direcciones?usuarioId=" + usuarioId + "] Obteniendo direcciones del usuario: " + usuarioId);
        try {
            List<Direccion> direcciones = direccionService.obtenerPorUsuario(usuarioId);
            System.out.println("✅ Se encontraron " + direcciones.size() + " direcciones");
            return ResponseEntity.ok(direcciones);
        } catch (RuntimeException e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    // 2. GET: Obtener una dirección por su ID
    @GetMapping("/{id}")
    public ResponseEntity<Direccion> obtenerPorId(@PathVariable Long id) {
        System.out.println("🔎 [GET /api/direcciones/" + id + "] Buscando dirección por ID: " + id);
        return direccionService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. POST: Crear una nueva dirección (MEJORADO - acepta usuarioId en body o query)
    @PostMapping
    public ResponseEntity<?> crearDireccion(
            @RequestBody java.util.Map<String, Object> payload,
            @RequestParam(required = false) Long usuarioId) {
        
        System.out.println("✍️ [POST /api/direcciones] ✅ PETICIÓN RECIBIDA");
        System.out.println("   Query param usuarioId: " + usuarioId);
        System.out.println("   Payload keys: " + payload.keySet());
        
        try {
            // Obtener usuarioId: primero del parámetro, luego del body
            Long userId = usuarioId;
            if (userId == null && payload.containsKey("usuarioId")) {
                userId = Long.valueOf(payload.get("usuarioId").toString());
            }
            
            if (userId == null) {
                System.out.println("❌ Error: No se proporcionó usuarioId");
                return ResponseEntity.badRequest().body(java.util.Map.of(
                    "error", "usuarioId es requerido",
                    "ejemplo1", "POST /api/direcciones?usuarioId=1",
                    "ejemplo2", "POST /api/direcciones con {usuarioId: 1, alias: 'Casa', ...}"
                ));
            }
            
            // Crear objeto Direccion desde el payload
            Direccion direccion = new Direccion();
            direccion.setAlias((String) payload.get("alias"));
            direccion.setCallePrincipal((String) payload.get("callePrincipal"));
            direccion.setCalleSecundaria((String) payload.get("calleSecundaria"));
            direccion.setCiudad((String) payload.get("ciudad"));
            direccion.setTelefono((String) payload.get("telefono"));
            direccion.setReferencia((String) payload.get("referencia"));
            
            if (payload.containsKey("esPrincipal")) {
                direccion.setEsPrincipal(Boolean.valueOf(payload.get("esPrincipal").toString()));
            }
            
            System.out.println("   Creando para usuario ID: " + userId);
            System.out.println("   Datos: " + direccion.getAlias() + " - " + direccion.getCiudad());
            
            Direccion direccionCreada = direccionService.crearDireccion(direccion, userId);
            System.out.println("✅ Dirección creada exitosamente: ID=" + direccionCreada.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(direccionCreada);
            
        } catch (NumberFormatException e) {
            System.out.println("❌ Error: usuarioId debe ser numérico");
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "usuarioId debe ser numérico"));
        } catch (RuntimeException e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.out.println("❌ Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Error interno del servidor"));
        }
    }

    // 4. PUT: Actualizar una dirección
    @PutMapping("/{id}")
    public ResponseEntity<Direccion> actualizarDireccion(
            @PathVariable Long id,
            @RequestBody Direccion direccion) {
        
        System.out.println("✏️ [PUT /api/direcciones/" + id + "] ✅ PETICIÓN RECIBIDA - Actualizando dirección...");
        
        try {
            Direccion direccionActualizada = direccionService.actualizarDireccion(id, direccion);
            System.out.println("✅ Dirección actualizada exitosamente: ID=" + id);
            return ResponseEntity.ok(direccionActualizada);
        } catch (RuntimeException e) {
            System.out.println("❌ Error al actualizar: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // 5. DELETE: Eliminar una dirección
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDireccion(@PathVariable Long id) {
        System.out.println("🗑️ [DELETE /api/direcciones/" + id + "] ✅ PETICIÓN RECIBIDA - Eliminando dirección...");
        
        try {
            direccionService.eliminarDireccion(id);
            System.out.println("✅ Dirección eliminada exitosamente: ID=" + id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            System.out.println("❌ Error al eliminar: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // 6. PUT: Marcar una dirección como principal
    @PutMapping("/{id}/principal")
    public ResponseEntity<Direccion> marcarComoPrincipal(@PathVariable Long id) {
        System.out.println("⭐ [PUT /api/direcciones/" + id + "/principal] Marcando como principal...");
        
        try {
            Direccion direccion = direccionService.marcarComoPrincipal(id);
            System.out.println("✅ Dirección marcada como principal: " + direccion.getAlias());
            return ResponseEntity.ok(direccion);
        } catch (RuntimeException e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
