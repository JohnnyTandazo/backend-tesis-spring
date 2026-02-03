package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/envios")
public class EnvioController {

    @Autowired
    private EnvioService envioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ORDEN IMPORTANTE DE RUTAS (específicas antes que genéricas):
    // 1. /detalle/{id}
    // 2. /usuario/{usuarioId}
    // 3. /tracking/{numeroTracking}
    // 4. (GET genérico) - con @RequestParam opcional
    // 5. /{id} - más genérico, va al final

    // GET: Obtener un envío por su ID (/detalle/{id})
    @GetMapping("/detalle/{id}")
    public ResponseEntity<Envio> obtenerEnvioPorId(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioActualId,
            @RequestParam(value = "usuarioActualId", required = false) Long usuarioActualIdParam) {
        
        // Priorizar header, luego query param
        Long usuarioId = usuarioActualId != null ? usuarioActualId : usuarioActualIdParam;
        
        System.out.println("🔎 [GET /api/envios/detalle/" + id + "] PETICIÓN RECIBIDA - Usuario autenticado: " + usuarioId);
        Optional<Envio> envioOpt = envioService.obtenerPorId(id);
        
        if (!envioOpt.isPresent()) {
            System.out.println("❌ Envío NO encontrado para ID: " + id);
            return ResponseEntity.notFound().build();
        }
        
        Envio envio = envioOpt.get();
        
        // 🔒 VERIFICACIÓN IDOR: Comprobar propiedad del recurso
        if (usuarioId != null) {
            Usuario usuarioActual = usuarioRepository.findById(usuarioId).orElse(null);
            
            if (usuarioActual != null) {
                String rol = usuarioActual.getRol().toUpperCase();
                
                // ADMIN y OPERADOR tienen acceso total
                if (rol.equals("ADMIN") || rol.equals("OPERADOR")) {
                    System.out.println("✅ Acceso autorizado: Usuario " + rol);
                    return ResponseEntity.ok(envio);
                }
                
                // CLIENTE: Solo puede ver sus propios envíos
                if (rol.equals("CLIENTE")) {
                    if (!envio.getUsuario().getId().equals(usuarioActual.getId())) {
                        System.out.println("🚫 ACCESO DENEGADO: Cliente " + usuarioId + " intentó acceder a envío de usuario " + envio.getUsuario().getId());
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver este envío");
                    }
                    System.out.println("✅ Acceso autorizado: Envío pertenece al cliente");
                }
            }
        }
        
        System.out.println("✅ Envío encontrado: ID=" + id + ", Tracking=" + envio.getNumeroTracking());
        return ResponseEntity.ok(envio);
    }

    // GET: Obtener envíos por usuario (/usuario/{usuarioId})
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Envio>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        System.out.println("👤 [GET /api/envios/usuario/" + usuarioId + "] PETICIÓN RECIBIDA");
        try {
            List<Envio> envios = envioService.obtenerPorUsuario(usuarioId);
            System.out.println("✅ Se encontraron " + envios.size() + " envíos del usuario: " + usuarioId);
            return ResponseEntity.ok(envios);
        } catch (Exception e) {
            System.out.println("⚠️ Error: " + e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    // GET: Obtener envío por número de tracking (/tracking/{numeroTracking})
    @GetMapping("/tracking/{numeroTracking}")
    public ResponseEntity<Envio> obtenerPorTracking(@PathVariable String numeroTracking) {
        System.out.println("📍 [GET /api/envios/tracking/" + numeroTracking + "] PETICIÓN RECIBIDA");
        Envio envio = envioService.obtenerPorTracking(numeroTracking);
        
        if (envio != null) {
            System.out.println("✅ Envío encontrado por tracking: " + numeroTracking);
            return ResponseEntity.ok(envio);
        } else {
            System.out.println("❌ Envío NO encontrado para tracking: " + numeroTracking);
            return ResponseEntity.notFound().build();
        }
    }

    // GET: Obtener envíos con filtro opcional por usuarioId (raíz, con @RequestParam)
    @GetMapping
    public ResponseEntity<List<Envio>> obtenerTodos(@RequestParam(required = false) Long usuarioId) {
        System.out.println("📦 [GET /api/envios] PETICIÓN RECIBIDA - usuarioId: " + usuarioId);
        try {
            List<Envio> envios;
            if (usuarioId != null) {
                envios = envioService.obtenerPorUsuario(usuarioId);
                System.out.println("✅ Se encontraron " + envios.size() + " envíos del usuario: " + usuarioId);
            } else {
                envios = envioService.obtenerTodos();
                System.out.println("✅ Se encontraron " + envios.size() + " envíos en total");
            }
            return ResponseEntity.ok(envios);
        } catch (Exception e) {
            System.out.println("⚠️ Error obteniendo envíos: " + e.getMessage() + ". Retornando lista vacía.");
            return ResponseEntity.ok(List.of());
        }
    }

    // GET: Obtener envío por ID directo (/{id}) - DEBE IR AL FINAL
    @GetMapping("/{id}")
    public ResponseEntity<Envio> obtenerEnvioPorIdDirecto(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioActualId,
            @RequestParam(value = "usuarioActualId", required = false) Long usuarioActualIdParam) {
        
        // Priorizar header, luego query param
        Long usuarioId = usuarioActualId != null ? usuarioActualId : usuarioActualIdParam;
        
        System.out.println("🔎 [GET /api/envios/" + id + "] PETICIÓN RECIBIDA - Usuario autenticado: " + usuarioId);
        Optional<Envio> envioOpt = envioService.obtenerPorId(id);
        
        if (!envioOpt.isPresent()) {
            System.out.println("❌ Envío NO encontrado con ID: " + id);
            return ResponseEntity.notFound().build();
        }
        
        Envio envio = envioOpt.get();
        
        // 🔒 VERIFICACIÓN IDOR: Comprobar propiedad del recurso
        if (usuarioId != null) {
            Usuario usuarioActual = usuarioRepository.findById(usuarioId).orElse(null);
            
            if (usuarioActual != null) {
                String rol = usuarioActual.getRol().toUpperCase();
                
                // ADMIN y OPERADOR tienen acceso total
                if (rol.equals("ADMIN") || rol.equals("OPERADOR")) {
                    System.out.println("✅ Acceso autorizado: Usuario " + rol);
                    return ResponseEntity.ok(envio);
                }
                
                // CLIENTE: Solo puede ver sus propios envíos
                if (rol.equals("CLIENTE")) {
                    if (!envio.getUsuario().getId().equals(usuarioActual.getId())) {
                        System.out.println("🚫 ACCESO DENEGADO: Cliente " + usuarioId + " intentó acceder a envío de usuario " + envio.getUsuario().getId());
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver este envío");
                    }
                    System.out.println("✅ Acceso autorizado: Envío pertenece al cliente");
                }
            }
        }
        
        System.out.println("✅ Envío encontrado: ID=" + id + ", Tracking=" + envio.getNumeroTracking());
        return ResponseEntity.ok(envio);
    }

    // 5. POST: Crear un nuevo envío
    @PostMapping
    public ResponseEntity<Envio> crearEnvio(@RequestBody CrearEnvioRequest request) {
        System.out.println("✍️ [POST /api/envios] ✅ PETICIÓN RECIBIDA - Creando nuevo envío...");
        System.out.println("   Número Tracking: " + request.getNumeroTracking());
        System.out.println("   Destinatario: " + request.getDestinatarioNombre());
        Envio envioCreado = envioService.crearEnvio(request);
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

    // 6b. PUT: Actualizar SOLO el estado de un envío
    @PutMapping("/{id}/estado")
    public ResponseEntity<Envio> actualizarEstado(
            @PathVariable Long id, 
            @RequestParam String nuevoEstado) {
        
        System.out.println("🔄 [PUT /api/envios/" + id + "/estado] ✅ PETICIÓN RECIBIDA");
        System.out.println("   Cambiando estado a: " + nuevoEstado);
        
        try {
            Envio envioActualizado = envioService.actualizarEstado(id, nuevoEstado);
            System.out.println("✅ Estado actualizado exitosamente: ID=" + id + ", Nuevo estado=" + nuevoEstado);
            return ResponseEntity.ok(envioActualizado);
        } catch (RuntimeException e) {
            System.out.println("❌ Error al actualizar estado: " + e.getMessage());
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
