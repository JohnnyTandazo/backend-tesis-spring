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

    // 1. GET: Obtener todas las direcciones
    @GetMapping
    public List<Direccion> obtenerTodas() {
        System.out.println("📍 [GET /api/direcciones] Obteniendo todas las direcciones...");
        return direccionService.obtenerTodas();
    }

    // 2. GET: Obtener una dirección por su ID
    @GetMapping("/{id}")
    public ResponseEntity<Direccion> obtenerPorId(@PathVariable Long id) {
        System.out.println("🔎 [GET /api/direcciones/" + id + "] Buscando dirección por ID: " + id);
        return direccionService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. POST: Crear una nueva dirección
    @PostMapping
    public ResponseEntity<Direccion> crearDireccion(
            @RequestBody Direccion direccion,
            @RequestParam Long usuarioId) {
        
        System.out.println("✍️ [POST /api/direcciones] ✅ PETICIÓN RECIBIDA - Creando dirección para usuario: " + usuarioId);
        System.out.println("   Datos: " + direccion.getAlias() + " - " + direccion.getCiudad());
        
        try {
            Direccion direccionCreada = direccionService.crearDireccion(direccion, usuarioId);
            System.out.println("✅ Dirección creada exitosamente: ID=" + direccionCreada.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(direccionCreada);
        } catch (RuntimeException e) {
            System.out.println("❌ Error al crear dirección: " + e.getMessage());
            return ResponseEntity.badRequest().build();
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
