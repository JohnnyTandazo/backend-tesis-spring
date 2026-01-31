package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/envios")
public class EnvioController {

    @Autowired
    private EnvioService envioService;

    // 1. GET: Obtener todos los envíos
    @GetMapping
    public List<Envio> obtenerTodos() {
        System.out.println("📦 [GET /api/envios] Obteniendo todos los envíos...");
        return envioService.obtenerTodos();
    }

    // 2. GET: Obtener un envío por su ID (ENDPOINT SOLICITADO)
    @GetMapping("/{id}")
    public ResponseEntity<Envio> obtenerEnvioPorId(@PathVariable Long id) {
        System.out.println("🔍 [GET /api/envios/" + id + "] ✅ PETICIÓN RECIBIDA - Buscando envío por ID: " + id);
        Optional<Envio> envio = envioService.obtenerPorId(id);
        
        if (envio.isPresent()) {
            System.out.println("✅ Envío encontrado: ID=" + id + ", Tracking=" + envio.get().getNumeroTracking());
            return ResponseEntity.ok(envio.get());
        } else {
            System.out.println("❌ Envío NO encontrado para ID: " + id);
            return ResponseEntity.notFound().build();
        }
    }

    // 3. GET: Obtener envíos por usuario
    @GetMapping("/usuario/{usuarioId}")
    public List<Envio> obtenerPorUsuario(@PathVariable Long usuarioId) {
        System.out.println("👤 [GET /api/envios/usuario/" + usuarioId + "] Obteniendo envíos del usuario: " + usuarioId);
        return envioService.obtenerPorUsuario(usuarioId);
    }

    // 4. GET: Obtener envío por número de tracking
    @GetMapping("/tracking/{numeroTracking}")
    public ResponseEntity<Envio> obtenerPorTracking(@PathVariable String numeroTracking) {
        System.out.println("📍 [GET /api/envios/tracking/" + numeroTracking + "] Buscando envío por tracking: " + numeroTracking);
        Envio envio = envioService.obtenerPorTracking(numeroTracking);
        
        if (envio != null) {
            System.out.println("✅ Envío encontrado por tracking: " + numeroTracking);
            return ResponseEntity.ok(envio);
        } else {
            System.out.println("❌ Envío NO encontrado para tracking: " + numeroTracking);
            return ResponseEntity.notFound().build();
        }
    }

    // 5. POST: Crear un nuevo envío
    @PostMapping
    public ResponseEntity<Envio> crearEnvio(@RequestBody Envio envio) {
        System.out.println("✍️ [POST /api/envios] ✅ PETICIÓN RECIBIDA - Creando nuevo envío...");
        System.out.println("   Datos: " + envio.getNumeroTracking());
        Envio envioCreado = envioService.crearEnvio(envio);
        System.out.println("✅ Envío creado exitosamente: ID=" + envioCreado.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(envioCreado);
    }

    // 6. PUT: Actualizar un envío
    @PutMapping("/{id}")
    public ResponseEntity<Envio> actualizarEnvio(@PathVariable Long id, @RequestBody Envio envio) {
        System.out.println("✏️ [PUT /api/envios/" + id + "] ✅ PETICIÓN RECIBIDA - Actualizando envío...");
        try {
            Envio envioActualizado = envioService.actualizarEnvio(id, envio);
            System.out.println("✅ Envío actualizado exitosamente: ID=" + id);
            return ResponseEntity.ok(envioActualizado);
        } catch (RuntimeException e) {
            System.out.println("❌ Error al actualizar: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // 7. DELETE: Eliminar un envío
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEnvio(@PathVariable Long id) {
        System.out.println("🗑️ [DELETE /api/envios/" + id + "] ✅ PETICIÓN RECIBIDA - Eliminando envío...");
        try {
            envioService.eliminarEnvio(id);
            System.out.println("✅ Envío eliminado exitosamente: ID=" + id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.out.println("❌ Error al eliminar: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
